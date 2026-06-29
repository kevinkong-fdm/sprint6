package com.example.banking.account.application;

import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.api.dto.MovementResponse;
import com.example.banking.account.api.dto.TransferRequest;
import com.example.banking.account.api.dto.TransferResponse;
import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.domain.MovementDirection;
import com.example.banking.account.domain.MovementType;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import com.example.banking.account.infrastructure.BankAccountRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferFundsService {

    private final AccountAuthorizationService accountAuthorizationService;
    private final MovementIdempotencyService movementIdempotencyService;
    private final AccountMovementRepository accountMovementRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountResponseMapper accountResponseMapper;

    public TransferFundsService(
            AccountAuthorizationService accountAuthorizationService,
            MovementIdempotencyService movementIdempotencyService,
            AccountMovementRepository accountMovementRepository,
            BankAccountRepository bankAccountRepository,
            AccountResponseMapper accountResponseMapper
    ) {
        this.accountAuthorizationService = accountAuthorizationService;
        this.movementIdempotencyService = movementIdempotencyService;
        this.accountMovementRepository = accountMovementRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.accountResponseMapper = accountResponseMapper;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request, String correlationId, String actorId) {
        BigDecimal amount = normalizePositive(request.amount());

        String sourceAccountId = request.sourceAccountId().trim();
        String destinationAccountId = request.destinationAccountId().trim();
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new AccountDomainException.TransferPairingException();
        }

        AccountMovementEntity existingDebit = movementIdempotencyService
                .findExisting(sourceAccountId, MovementType.TRANSFER_DEBIT, request.idempotencyKey())
                .orElse(null);
        if (existingDebit != null) {
            AccountMovementEntity existingCredit = accountMovementRepository
                    .findFirstByReferenceIdAndMovementTypeOrderByCreatedAtDesc(
                            existingDebit.getReferenceId(),
                            MovementType.TRANSFER_CREDIT)
                    .orElseThrow(AccountDomainException.TransferValidationException::new);

            return new TransferResponse(
                    existingDebit.getReferenceId(),
                    accountResponseMapper.toMovementResponse(existingDebit),
                    accountResponseMapper.toMovementResponse(existingCredit));
        }

        BankAccountEntity source = accountAuthorizationService.requireOwnedAccount(sourceAccountId, actorId);
        BankAccountEntity destination = accountAuthorizationService.requireOwnedAccount(destinationAccountId, actorId);

        if (!source.canDebit(amount)) {
            throw new AccountDomainException.InsufficientTransferFundsException();
        }

        String transferId = UUID.randomUUID().toString();
        BigDecimal sourceBefore = source.getAvailableBalance();
        BigDecimal destinationBefore = destination.getAvailableBalance();

        source.debit(amount);
        destination.credit(amount);

        bankAccountRepository.save(source);
        bankAccountRepository.save(destination);

        AccountMovementEntity debit = accountMovementRepository.save(AccountMovementEntity.posted(
                source.getAccountId(),
                MovementType.TRANSFER_DEBIT,
                amount,
                MovementDirection.DEBIT,
                sourceBefore,
                source.getAvailableBalance(),
                request.idempotencyKey(),
                correlationId,
                transferId));

        AccountMovementEntity credit = accountMovementRepository.save(AccountMovementEntity.posted(
                destination.getAccountId(),
                MovementType.TRANSFER_CREDIT,
                amount,
                MovementDirection.CREDIT,
                destinationBefore,
                destination.getAvailableBalance(),
                request.idempotencyKey(),
                correlationId,
                transferId));

        MovementResponse sourceResponse = accountResponseMapper.toMovementResponse(debit);
        MovementResponse destinationResponse = accountResponseMapper.toMovementResponse(credit);

        return new TransferResponse(transferId, sourceResponse, destinationResponse);
    }

    private BigDecimal normalizePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountDomainException.TransferValidationException();
        }
        return amount.setScale(4, java.math.RoundingMode.HALF_UP);
    }
}

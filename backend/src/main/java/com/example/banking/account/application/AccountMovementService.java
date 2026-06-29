package com.example.banking.account.application;

import com.example.banking.account.api.dto.DepositRequest;
import com.example.banking.account.api.dto.MovementResponse;
import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.api.dto.WithdrawalRequest;
import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.domain.MovementDirection;
import com.example.banking.account.domain.MovementType;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import com.example.banking.account.infrastructure.BankAccountRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountMovementService {

    private final AccountAuthorizationService accountAuthorizationService;
    private final MovementIdempotencyService movementIdempotencyService;
    private final AccountMovementRepository accountMovementRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountResponseMapper accountResponseMapper;

    public AccountMovementService(
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
    public MovementResponse deposit(String accountId, DepositRequest request, String correlationId, String actorId) {
        BigDecimal amount = normalizePositive(request.amount(), true);
        BankAccountEntity account = accountAuthorizationService.requireOwnedAccount(accountId, actorId);

        AccountMovementEntity idempotent = movementIdempotencyService
                .findExisting(accountId, MovementType.DEPOSIT, request.idempotencyKey())
                .orElse(null);
        if (idempotent != null) {
            return accountResponseMapper.toMovementResponse(idempotent);
        }

        BigDecimal before = account.getAvailableBalance();
        account.credit(amount);
        bankAccountRepository.save(account);

        AccountMovementEntity saved = accountMovementRepository.save(AccountMovementEntity.posted(
                accountId,
                MovementType.DEPOSIT,
                amount,
                MovementDirection.CREDIT,
                before,
                account.getAvailableBalance(),
                request.idempotencyKey(),
                correlationId,
                null));

        return accountResponseMapper.toMovementResponse(saved);
    }

    @Transactional
    public MovementResponse withdraw(String accountId, WithdrawalRequest request, String correlationId, String actorId) {
        BigDecimal amount = normalizePositive(request.amount(), false);
        BankAccountEntity account = accountAuthorizationService.requireOwnedAccount(accountId, actorId);

        AccountMovementEntity idempotent = movementIdempotencyService
                .findExisting(accountId, MovementType.WITHDRAWAL, request.idempotencyKey())
                .orElse(null);
        if (idempotent != null) {
            return accountResponseMapper.toMovementResponse(idempotent);
        }

        if (!account.canDebit(amount)) {
            throw new AccountDomainException.InsufficientWithdrawalFundsException();
        }

        BigDecimal before = account.getAvailableBalance();
        account.debit(amount);
        bankAccountRepository.save(account);

        AccountMovementEntity saved = accountMovementRepository.save(AccountMovementEntity.posted(
                accountId,
                MovementType.WITHDRAWAL,
                amount,
                MovementDirection.DEBIT,
                before,
                account.getAvailableBalance(),
                request.idempotencyKey(),
                correlationId,
                null));

        return accountResponseMapper.toMovementResponse(saved);
    }

    private BigDecimal normalizePositive(BigDecimal amount, boolean isDeposit) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            if (isDeposit) {
                throw new AccountDomainException.DepositValidationException();
            }
            throw new AccountDomainException.WithdrawalValidationException();
        }
        return amount.setScale(4, java.math.RoundingMode.HALF_UP);
    }
}

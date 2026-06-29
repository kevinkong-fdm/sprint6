package com.example.banking.account.application;

import com.example.banking.account.api.dto.DeleteAccountRequest;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.domain.DeleteEligibilityResult;
import com.example.banking.account.domain.MovementStatus;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import com.example.banking.account.infrastructure.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteAccountWorkflowService {

    private final AccountAuthorizationService accountAuthorizationService;
    private final AccountMovementRepository accountMovementRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountAuditService accountAuditService;

    public DeleteAccountWorkflowService(
            AccountAuthorizationService accountAuthorizationService,
            AccountMovementRepository accountMovementRepository,
            BankAccountRepository bankAccountRepository,
            AccountAuditService accountAuditService
    ) {
        this.accountAuthorizationService = accountAuthorizationService;
        this.accountMovementRepository = accountMovementRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.accountAuditService = accountAuditService;
    }

    @Transactional
    public void delete(String accountId, DeleteAccountRequest request, String correlationId, String actorId) {
        BankAccountEntity source = accountAuthorizationService.requireOwnedAccountForDelete(accountId, actorId);

        if (accountMovementRepository.existsByAccountIdAndStatus(accountId, MovementStatus.PENDING)) {
            accountAuditService.recordDeleteDecision(
                    accountId,
                    actorId,
                    request == null ? null : request.closeoutDestinationAccountId(),
                    DeleteEligibilityResult.REJECTED_PENDING_MOVEMENT,
                    "ACCT-DEL-002",
                    correlationId);
            throw new AccountDomainException.DeleteEligibilityException(
                    "Account delete not allowed due to pending movement activity.",
                    409);
        }

        bankAccountRepository.delete(source);
        accountAuditService.recordDeleteDecision(
                accountId,
                actorId,
                request == null ? null : request.closeoutDestinationAccountId(),
                DeleteEligibilityResult.ALLOWED,
                null,
                correlationId);
    }
}

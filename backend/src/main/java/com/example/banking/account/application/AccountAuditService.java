package com.example.banking.account.application;

import com.example.banking.account.domain.AccountDeleteRequestAuditEntity;
import com.example.banking.account.domain.DeleteEligibilityResult;
import com.example.banking.account.infrastructure.AccountDeleteRequestAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccountAuditService {

    private static final Logger log = LoggerFactory.getLogger(AccountAuditService.class);

    private final AccountDeleteRequestAuditRepository accountDeleteRequestAuditRepository;

    public AccountAuditService(AccountDeleteRequestAuditRepository accountDeleteRequestAuditRepository) {
        this.accountDeleteRequestAuditRepository = accountDeleteRequestAuditRepository;
    }

    public void recordDeleteDecision(
            String accountId,
            String actorCustomerId,
            String requestedCloseoutDestinationAccountId,
            DeleteEligibilityResult eligibilityResult,
            String errorCode,
            String correlationId
    ) {
        accountDeleteRequestAuditRepository.save(AccountDeleteRequestAuditEntity.of(
                accountId,
                actorCustomerId,
                requestedCloseoutDestinationAccountId,
                eligibilityResult,
                errorCode,
                correlationId));

        // Log only non-sensitive identifiers to keep audit events safe for local traces.
        log.info(
                "Account delete decision. accountId={} actorCustomerId={} outcome={} errorCode={} correlationId={}",
                accountId,
                actorCustomerId,
                eligibilityResult,
                errorCode,
                correlationId);
    }
}

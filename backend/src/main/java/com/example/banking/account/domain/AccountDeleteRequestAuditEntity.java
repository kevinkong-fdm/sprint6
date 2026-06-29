package com.example.banking.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_delete_request_audit")
public class AccountDeleteRequestAuditEntity {

    @Id
    @Column(name = "delete_audit_id", nullable = false, columnDefinition = "CHAR(36)")
    private String deleteAuditId;

    @Column(name = "account_id", nullable = false, columnDefinition = "CHAR(36)")
    private String accountId;

    @Column(name = "actor_customer_id", nullable = false, columnDefinition = "CHAR(36)")
    private String actorCustomerId;

    @Column(name = "requested_closeout_destination_account_id", columnDefinition = "CHAR(36)")
    private String requestedCloseoutDestinationAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "eligibility_result", nullable = false, length = 64)
    private DeleteEligibilityResult eligibilityResult;

    @Column(name = "error_code", length = 32)
    private String errorCode;

    @Column(name = "correlation_id", nullable = false, length = 64)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static AccountDeleteRequestAuditEntity of(
            String accountId,
            String actorCustomerId,
            String requestedCloseoutDestinationAccountId,
            DeleteEligibilityResult eligibilityResult,
            String errorCode,
            String correlationId
    ) {
        AccountDeleteRequestAuditEntity entity = new AccountDeleteRequestAuditEntity();
        entity.deleteAuditId = UUID.randomUUID().toString();
        entity.accountId = normalizeRequired(accountId);
        entity.actorCustomerId = normalizeRequired(actorCustomerId);
        entity.requestedCloseoutDestinationAccountId = normalizeNullable(requestedCloseoutDestinationAccountId);
        entity.eligibilityResult = eligibilityResult;
        entity.errorCode = normalizeNullable(errorCode);
        entity.correlationId = normalizeRequired(correlationId);
        entity.createdAt = Instant.now();
        return entity;
    }

    private static String normalizeRequired(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}

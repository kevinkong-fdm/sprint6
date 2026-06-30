package com.example.banking.standingorder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "standing_order_execution")
public class StandingOrderExecutionEntity {

    @Id
    @Column(name = "standing_order_execution_id", nullable = false, columnDefinition = "CHAR(36)")
    private String standingOrderExecutionId;

    @Column(name = "standing_order_id", nullable = false, columnDefinition = "CHAR(36)")
    private String standingOrderId;

    @Column(name = "scheduled_for", nullable = false)
    private Instant scheduledFor;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 16)
    private StandingOrderExecutionOutcome outcome;

    @Column(name = "failure_reason_code", length = 32)
    private String failureReasonCode;

    @Column(name = "transfer_reference_id", columnDefinition = "CHAR(36)")
    private String transferReferenceId;

    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    @Column(name = "correlation_id", nullable = false, length = 64)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static StandingOrderExecutionEntity create(
            String standingOrderId,
            Instant scheduledFor,
            StandingOrderExecutionOutcome outcome,
            String failureReasonCode,
            String transferReferenceId,
            String idempotencyKey,
            String correlationId
    ) {
        Instant now = Instant.now();

        StandingOrderExecutionEntity entity = new StandingOrderExecutionEntity();
        entity.standingOrderExecutionId = UUID.randomUUID().toString();
        entity.standingOrderId = normalizeRequired(standingOrderId);
        entity.scheduledFor = scheduledFor == null ? now : scheduledFor;
        entity.triggeredAt = now;
        entity.outcome = outcome;
        entity.failureReasonCode = normalizeNullable(failureReasonCode);
        entity.transferReferenceId = normalizeNullable(transferReferenceId);
        entity.idempotencyKey = normalizeNullable(idempotencyKey);
        entity.correlationId = normalizeRequired(correlationId);
        entity.createdAt = now;
        return entity;
    }

    public String getStandingOrderExecutionId() {
        return standingOrderExecutionId;
    }

    public String getStandingOrderId() {
        return standingOrderId;
    }

    public Instant getScheduledFor() {
        return scheduledFor;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public StandingOrderExecutionOutcome getOutcome() {
        return outcome;
    }

    public String getFailureReasonCode() {
        return failureReasonCode;
    }

    public String getTransferReferenceId() {
        return transferReferenceId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
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

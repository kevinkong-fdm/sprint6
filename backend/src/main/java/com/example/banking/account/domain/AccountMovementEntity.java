package com.example.banking.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "account_movement",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_account_movement_idempotency",
                columnNames = {"account_id", "movement_type", "idempotency_key"}))
public class AccountMovementEntity {

    @Id
    @Column(name = "movement_id", nullable = false, columnDefinition = "CHAR(36)")
    private String movementId;

    @Column(name = "account_id", nullable = false, columnDefinition = "CHAR(36)")
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 32)
    private MovementType movementType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 16)
    private MovementDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private MovementStatus status;

    @Column(name = "balance_before", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    @Column(name = "correlation_id", nullable = false, length = 64)
    private String correlationId;

    @Column(name = "reference_id", columnDefinition = "CHAR(36)")
    private String referenceId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "posted_at")
    private Instant postedAt;

    public static AccountMovementEntity posted(
            String accountId,
            MovementType movementType,
            BigDecimal amount,
            MovementDirection direction,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String idempotencyKey,
            String correlationId,
            String referenceId
    ) {
        Instant now = Instant.now();

        AccountMovementEntity entity = new AccountMovementEntity();
        entity.movementId = UUID.randomUUID().toString();
        entity.accountId = accountId;
        entity.movementType = movementType;
        entity.amount = normalize(amount);
        entity.direction = direction;
        entity.status = MovementStatus.POSTED;
        entity.balanceBefore = normalize(balanceBefore);
        entity.balanceAfter = normalize(balanceAfter);
        entity.idempotencyKey = normalizeNullable(idempotencyKey);
        entity.correlationId = normalizeRequired(correlationId);
        entity.referenceId = normalizeNullable(referenceId);
        entity.createdAt = now;
        entity.postedAt = now;
        return entity;
    }

    public String getMovementId() {
        return movementId;
    }

    public String getAccountId() {
        return accountId;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public MovementDirection getDirection() {
        return direction;
    }

    public MovementStatus getStatus() {
        return status;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    private static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(4);
        }
        return value.setScale(4, java.math.RoundingMode.HALF_UP);
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeRequired(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}

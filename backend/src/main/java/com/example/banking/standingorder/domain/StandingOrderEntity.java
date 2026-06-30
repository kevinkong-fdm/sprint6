package com.example.banking.standingorder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "standing_order")
public class StandingOrderEntity {

    @Id
    @Column(name = "standing_order_id", nullable = false, columnDefinition = "CHAR(36)")
    private String standingOrderId;

    @Column(name = "customer_id", nullable = false, columnDefinition = "CHAR(36)")
    private String customerId;

    @Column(name = "source_account_id", nullable = false, columnDefinition = "CHAR(36)")
    private String sourceAccountId;

    @Column(name = "destination_account_id", nullable = false, columnDefinition = "CHAR(36)")
    private String destinationAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 16)
    private StandingOrderFrequency frequency;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "execution_day_of_week", length = 16)
    private String executionDayOfWeek;

    @Column(name = "execution_day_of_month")
    private Integer executionDayOfMonth;

    @Column(name = "execution_time", nullable = false, length = 5)
    private String executionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private StandingOrderStatus status;

    @Column(name = "timezone_code", nullable = false, length = 16)
    private String timezoneCode;

    @Column(name = "next_execution_at")
    private Instant nextExecutionAt;

    @Column(name = "last_execution_at")
    private Instant lastExecutionAt;

    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static StandingOrderEntity create(
            String customerId,
            String sourceAccountId,
            String destinationAccountId,
            BigDecimal amount,
            StandingOrderFrequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            String executionDayOfWeek,
            Integer executionDayOfMonth,
            String executionTime,
            Instant nextExecutionAt,
            String timezoneCode,
            String idempotencyKey
    ) {
        Instant now = Instant.now();

        StandingOrderEntity entity = new StandingOrderEntity();
        entity.standingOrderId = UUID.randomUUID().toString();
        entity.customerId = normalizeRequired(customerId);
        entity.sourceAccountId = normalizeRequired(sourceAccountId);
        entity.destinationAccountId = normalizeRequired(destinationAccountId);
        entity.amount = scale(amount);
        entity.frequency = frequency;
        entity.startDate = startDate;
        entity.endDate = endDate;
        entity.executionDayOfWeek = normalizeNullable(executionDayOfWeek);
        entity.executionDayOfMonth = executionDayOfMonth;
        entity.executionTime = normalizeRequired(executionTime);
        entity.status = StandingOrderStatus.ACTIVE;
        entity.timezoneCode = normalizeRequired(timezoneCode);
        entity.nextExecutionAt = nextExecutionAt;
        entity.lastExecutionAt = null;
        entity.idempotencyKey = normalizeNullable(idempotencyKey);
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    public void applyUpdate(
            String destinationAccountId,
            BigDecimal amount,
            StandingOrderFrequency frequency,
            LocalDate endDate,
            String executionDayOfWeek,
            Integer executionDayOfMonth,
            String executionTime,
            Instant nextExecutionAt
    ) {
        if (destinationAccountId != null && !destinationAccountId.isBlank()) {
            this.destinationAccountId = destinationAccountId.trim();
        }
        if (amount != null) {
            this.amount = scale(amount);
        }
        if (frequency != null) {
            this.frequency = frequency;
        }
        this.endDate = endDate;
        this.executionDayOfWeek = normalizeNullable(executionDayOfWeek);
        this.executionDayOfMonth = executionDayOfMonth;
        if (executionTime != null && !executionTime.isBlank()) {
            this.executionTime = executionTime.trim();
        }
        this.nextExecutionAt = nextExecutionAt;
        this.updatedAt = Instant.now();
    }

    public void pause() {
        this.status = StandingOrderStatus.PAUSED;
        this.updatedAt = Instant.now();
    }

    public void resume(Instant nextExecutionAt) {
        this.status = StandingOrderStatus.ACTIVE;
        this.nextExecutionAt = nextExecutionAt;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = StandingOrderStatus.CANCELED;
        this.nextExecutionAt = null;
        this.updatedAt = Instant.now();
    }

    public void markExecuted(Instant executedAt, Instant nextExecutionAt) {
        this.lastExecutionAt = executedAt;
        this.nextExecutionAt = nextExecutionAt;
        this.updatedAt = Instant.now();
    }

    public String getStandingOrderId() {
        return standingOrderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public StandingOrderFrequency getFrequency() {
        return frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getExecutionDayOfWeek() {
        return executionDayOfWeek;
    }

    public Integer getExecutionDayOfMonth() {
        return executionDayOfMonth;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public StandingOrderStatus getStatus() {
        return status;
    }

    public String getTimezoneCode() {
        return timezoneCode;
    }

    public Instant getNextExecutionAt() {
        return nextExecutionAt;
    }

    public Instant getLastExecutionAt() {
        return lastExecutionAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(4);
        }
        return value.setScale(4, java.math.RoundingMode.HALF_UP);
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

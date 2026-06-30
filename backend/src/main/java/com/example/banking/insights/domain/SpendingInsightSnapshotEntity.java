package com.example.banking.insights.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "spending_insight_snapshot")
public class SpendingInsightSnapshotEntity {

    @Id
    @Column(name = "insight_snapshot_id", nullable = false, columnDefinition = "CHAR(36)")
    private String insightSnapshotId;

    @Column(name = "customer_id", nullable = false, columnDefinition = "CHAR(36)")
    private String customerId;

    @Column(name = "account_id", columnDefinition = "CHAR(36)")
    private String accountId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "comparison_mode", nullable = false, length = 32)
    private ComparisonMode comparisonMode;

    @Column(name = "insufficient_data", nullable = false)
    private boolean insufficientData;

    @Enumerated(EnumType.STRING)
    @Column(name = "insufficiency_reason", nullable = false, length = 64)
    private InsufficiencyReason insufficiencyReason;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "correlation_id", nullable = false, length = 64)
    private String correlationId;

    public static SpendingInsightSnapshotEntity create(
            String customerId,
            String accountId,
            LocalDate periodStart,
            LocalDate periodEnd,
            ComparisonMode comparisonMode,
            boolean insufficientData,
            InsufficiencyReason insufficiencyReason,
            String correlationId
    ) {
        SpendingInsightSnapshotEntity entity = new SpendingInsightSnapshotEntity();
        entity.insightSnapshotId = UUID.randomUUID().toString();
        entity.customerId = normalizeRequired(customerId);
        entity.accountId = normalizeNullable(accountId);
        entity.periodStart = periodStart;
        entity.periodEnd = periodEnd;
        entity.comparisonMode = comparisonMode;
        entity.insufficientData = insufficientData;
        entity.insufficiencyReason = insufficiencyReason;
        entity.generatedAt = Instant.now();
        entity.correlationId = normalizeRequired(correlationId);
        return entity;
    }

    public String getInsightSnapshotId() {
        return insightSnapshotId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getAccountId() {
        return accountId;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public ComparisonMode getComparisonMode() {
        return comparisonMode;
    }

    public boolean isInsufficientData() {
        return insufficientData;
    }

    public InsufficiencyReason getInsufficiencyReason() {
        return insufficiencyReason;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public String getCorrelationId() {
        return correlationId;
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

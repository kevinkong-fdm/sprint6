package com.example.banking.insights.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "spending_category_metric")
public class SpendingCategoryMetricEntity {

    @Id
    @Column(name = "spending_category_metric_id", nullable = false, columnDefinition = "CHAR(36)")
    private String spendingCategoryMetricId;

    @Column(name = "insight_snapshot_id", nullable = false, columnDefinition = "CHAR(36)")
    private String insightSnapshotId;

    @Column(name = "category_code", nullable = false, length = 64)
    private String categoryCode;

    @Column(name = "current_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentTotal;

    @Column(name = "previous_total", precision = 19, scale = 4)
    private BigDecimal previousTotal;

    @Column(name = "delta_amount", precision = 19, scale = 4)
    private BigDecimal deltaAmount;

    @Column(name = "delta_percent", precision = 9, scale = 4)
    private BigDecimal deltaPercent;

    public static SpendingCategoryMetricEntity create(
            String insightSnapshotId,
            String categoryCode,
            BigDecimal currentTotal,
            BigDecimal previousTotal,
            BigDecimal deltaAmount,
            BigDecimal deltaPercent
    ) {
        SpendingCategoryMetricEntity entity = new SpendingCategoryMetricEntity();
        entity.spendingCategoryMetricId = UUID.randomUUID().toString();
        entity.insightSnapshotId = normalizeRequired(insightSnapshotId);
        entity.categoryCode = normalizeRequired(categoryCode);
        entity.currentTotal = scale(currentTotal);
        entity.previousTotal = scaleNullable(previousTotal);
        entity.deltaAmount = scaleNullable(deltaAmount);
        entity.deltaPercent = scaleNullable(deltaPercent);
        return entity;
    }

    public String getSpendingCategoryMetricId() {
        return spendingCategoryMetricId;
    }

    public String getInsightSnapshotId() {
        return insightSnapshotId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public BigDecimal getCurrentTotal() {
        return currentTotal;
    }

    public BigDecimal getPreviousTotal() {
        return previousTotal;
    }

    public BigDecimal getDeltaAmount() {
        return deltaAmount;
    }

    public BigDecimal getDeltaPercent() {
        return deltaPercent;
    }

    private static String normalizeRequired(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(4);
        }
        return value.setScale(4, java.math.RoundingMode.HALF_UP);
    }

    private static BigDecimal scaleNullable(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(4, java.math.RoundingMode.HALF_UP);
    }
}

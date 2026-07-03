package com.example.banking.insights.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class SpendingInsightSnapshotEntityTest {

    @Test
    void shouldCreateAndNormalizeSnapshotFields() {
        SpendingInsightSnapshotEntity entity = SpendingInsightSnapshotEntity.create(
                " customer-1 ",
                " account-1 ",
                LocalDate.parse("2026-06-01"),
                LocalDate.parse("2026-06-30"),
                ComparisonMode.PREVIOUS_PERIOD,
                false,
                InsufficiencyReason.NONE,
                " corr-1 ");

        assertNotNull(entity.getInsightSnapshotId());
        assertEquals("customer-1", entity.getCustomerId());
        assertEquals("account-1", entity.getAccountId());
        assertEquals(LocalDate.parse("2026-06-01"), entity.getPeriodStart());
        assertEquals(LocalDate.parse("2026-06-30"), entity.getPeriodEnd());
        assertEquals(ComparisonMode.PREVIOUS_PERIOD, entity.getComparisonMode());
        assertEquals(false, entity.isInsufficientData());
        assertEquals(InsufficiencyReason.NONE, entity.getInsufficiencyReason());
        assertEquals("corr-1", entity.getCorrelationId());
        assertNotNull(entity.getGeneratedAt());
    }

    @Test
    void shouldNormalizeNullAndBlankInputs() {
        SpendingInsightSnapshotEntity entity = SpendingInsightSnapshotEntity.create(
                null,
                "   ",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-31"),
                ComparisonMode.NONE,
                true,
                InsufficiencyReason.INSUFFICIENT_HISTORY,
                null);

        assertNotNull(entity.getInsightSnapshotId());
        assertEquals("", entity.getCustomerId());
        assertNull(entity.getAccountId());
        assertEquals(LocalDate.parse("2026-01-01"), entity.getPeriodStart());
        assertEquals(LocalDate.parse("2026-01-31"), entity.getPeriodEnd());
        assertEquals(ComparisonMode.NONE, entity.getComparisonMode());
        assertEquals(true, entity.isInsufficientData());
        assertEquals(InsufficiencyReason.INSUFFICIENT_HISTORY, entity.getInsufficiencyReason());
        assertEquals("", entity.getCorrelationId());
        assertNotNull(entity.getGeneratedAt());
    }
}

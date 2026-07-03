package com.example.banking.insights.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SpendingCategoryMetricEntityTest {

    @Test
    void shouldCreateAndNormalizeMetricFields() {
        SpendingCategoryMetricEntity entity = SpendingCategoryMetricEntity.create(
                " snapshot-1 ",
                " groceries ",
                new BigDecimal("12.34567"),
                new BigDecimal("4.44444"),
                new BigDecimal("7.90126"),
                new BigDecimal("123.45678"));

        assertNotNull(entity.getSpendingCategoryMetricId());
        assertEquals("snapshot-1", entity.getInsightSnapshotId());
        assertEquals("groceries", entity.getCategoryCode());
        assertEquals(new BigDecimal("12.3457"), entity.getCurrentTotal());
        assertEquals(new BigDecimal("4.4444"), entity.getPreviousTotal());
        assertEquals(new BigDecimal("7.9013"), entity.getDeltaAmount());
        assertEquals(new BigDecimal("123.4568"), entity.getDeltaPercent());
    }

    @Test
    void shouldApplyDefaultValuesForNullInputs() {
        SpendingCategoryMetricEntity entity = SpendingCategoryMetricEntity.create(
                null,
                null,
                null,
                null,
                null,
                null);

        assertNotNull(entity.getSpendingCategoryMetricId());
        assertEquals("", entity.getInsightSnapshotId());
        assertEquals("", entity.getCategoryCode());
        assertEquals(new BigDecimal("0.0000"), entity.getCurrentTotal());
        assertNull(entity.getPreviousTotal());
        assertNull(entity.getDeltaAmount());
        assertNull(entity.getDeltaPercent());
    }
}

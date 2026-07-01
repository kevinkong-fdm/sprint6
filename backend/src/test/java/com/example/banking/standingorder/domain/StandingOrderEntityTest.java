package com.example.banking.standingorder.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class StandingOrderEntityTest {

    @Test
    void shouldCreateAndNormalizeStandingOrderFields() {
        StandingOrderEntity entity = StandingOrderEntity.create(
                " cust-1 ",
                " src-1 ",
                " dst-1 ",
                new BigDecimal("12.34567"),
                StandingOrderFrequency.DAILY,
                LocalDate.parse("2026-06-01"),
                null,
                "   ",
                null,
                " 09:00 ",
                Instant.parse("2026-06-02T00:00:00Z"),
                " AEST ",
                " ");

        assertNotNull(entity.getStandingOrderId());
        assertEquals("cust-1", entity.getCustomerId());
        assertEquals("src-1", entity.getSourceAccountId());
        assertEquals("dst-1", entity.getDestinationAccountId());
        assertEquals(new BigDecimal("12.3457"), entity.getAmount());
        assertNull(entity.getExecutionDayOfWeek());
        assertNull(entity.getIdempotencyKey());
        assertEquals("AEST", entity.getTimezoneCode());
        assertEquals(StandingOrderStatus.ACTIVE, entity.getStatus());
    }

    @Test
    void shouldApplyUpdatesAndLifecycleTransitions() {
        StandingOrderEntity entity = StandingOrderEntity.create(
                "cust-1",
                "src-1",
                "dst-1",
                new BigDecimal("10.0000"),
                StandingOrderFrequency.DAILY,
                LocalDate.parse("2026-06-01"),
                null,
                null,
                null,
                "09:00",
                Instant.parse("2026-06-02T00:00:00Z"),
                "AEST",
                "idem-1");

        Instant firstNext = Instant.parse("2026-06-03T00:00:00Z");
        entity.applyUpdate(
                " dst-2 ",
                new BigDecimal("20.0000"),
                StandingOrderFrequency.WEEKLY,
                LocalDate.parse("2026-07-01"),
                " MONDAY ",
                null,
                "08:30",
                firstNext);

        assertEquals("dst-2", entity.getDestinationAccountId());
        assertEquals(new BigDecimal("20.0000"), entity.getAmount());
        assertEquals(StandingOrderFrequency.WEEKLY, entity.getFrequency());
        assertEquals("MONDAY", entity.getExecutionDayOfWeek());
        assertEquals("08:30", entity.getExecutionTime());
        assertEquals(firstNext, entity.getNextExecutionAt());

        entity.pause();
        assertEquals(StandingOrderStatus.PAUSED, entity.getStatus());

        Instant resumedNext = Instant.parse("2026-06-10T00:00:00Z");
        entity.resume(resumedNext);
        assertEquals(StandingOrderStatus.ACTIVE, entity.getStatus());
        assertEquals(resumedNext, entity.getNextExecutionAt());

        Instant executedAt = Instant.parse("2026-06-11T00:00:00Z");
        Instant nextAfterRun = Instant.parse("2026-06-12T00:00:00Z");
        entity.markExecuted(executedAt, nextAfterRun);
        assertEquals(executedAt, entity.getLastExecutionAt());
        assertEquals(nextAfterRun, entity.getNextExecutionAt());

        entity.cancel();
        assertEquals(StandingOrderStatus.CANCELED, entity.getStatus());
        assertNull(entity.getNextExecutionAt());
    }
}

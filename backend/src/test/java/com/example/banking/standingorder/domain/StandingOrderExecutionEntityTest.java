package com.example.banking.standingorder.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class StandingOrderExecutionEntityTest {

    @Test
    void shouldCreateAndNormalizeExecutionEntity() {
        StandingOrderExecutionEntity entity = StandingOrderExecutionEntity.create(
                " so-1 ",
                null,
                StandingOrderExecutionOutcome.FAILED,
                "  SO-EXE-001  ",
                "  ",
                " idem-1 ",
                " corr-1 ");

        assertNotNull(entity.getStandingOrderExecutionId());
        assertEquals("so-1", entity.getStandingOrderId());
        assertNotNull(entity.getScheduledFor());
        assertNotNull(entity.getTriggeredAt());
        assertNotNull(entity.getCreatedAt());
        assertEquals(StandingOrderExecutionOutcome.FAILED, entity.getOutcome());
        assertEquals("SO-EXE-001", entity.getFailureReasonCode());
        assertNull(entity.getTransferReferenceId());
        assertEquals("idem-1", entity.getIdempotencyKey());
        assertEquals("corr-1", entity.getCorrelationId());
    }

    @Test
    void shouldKeepProvidedScheduledTimestamp() {
        Instant scheduledFor = Instant.parse("2026-06-05T00:00:00Z");
        StandingOrderExecutionEntity entity = StandingOrderExecutionEntity.create(
                "so-1",
                scheduledFor,
                StandingOrderExecutionOutcome.SUCCESS,
                null,
                "trf-1",
                null,
                "corr-1");

        assertEquals(scheduledFor, entity.getScheduledFor());
        assertEquals("trf-1", entity.getTransferReferenceId());
    }
}

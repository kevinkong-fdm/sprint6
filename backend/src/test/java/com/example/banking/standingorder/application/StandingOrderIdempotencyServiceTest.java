package com.example.banking.standingorder.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionOutcome;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import com.example.banking.standingorder.infrastructure.StandingOrderExecutionRepository;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StandingOrderIdempotencyServiceTest {

    @Mock
    private StandingOrderRepository standingOrderRepository;

    @Mock
    private StandingOrderExecutionRepository standingOrderExecutionRepository;

    private StandingOrderIdempotencyService service;

    @BeforeEach
    void setUp() {
        service = new StandingOrderIdempotencyService(standingOrderRepository, standingOrderExecutionRepository);
    }

    @Test
    void shouldNormalizeIdempotencyKey() {
        assertNull(service.normalizeKey(null));
        assertNull(service.normalizeKey("   "));
        assertEquals("idem-1", service.normalizeKey("  idem-1  "));
    }

    @Test
    void shouldFindExistingCreateByNormalizedKey() {
        StandingOrderEntity existing = StandingOrderEntity.create(
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

        when(standingOrderRepository.findFirstByCustomerIdAndIdempotencyKeyOrderByCreatedAtDesc("cust-1", "idem-1"))
                .thenReturn(Optional.of(existing));

        Optional<StandingOrderEntity> result = service.findExistingCreate("cust-1", "  idem-1  ");

        assertTrue(result.isPresent());
        verify(standingOrderRepository).findFirstByCustomerIdAndIdempotencyKeyOrderByCreatedAtDesc("cust-1", "idem-1");
    }

    @Test
    void shouldReturnEmptyCreateLookupWhenKeyMissing() {
        Optional<StandingOrderEntity> result = service.findExistingCreate("cust-1", " ");
        assertFalse(result.isPresent());
    }

    @Test
    void shouldFindExistingTriggerByNormalizedKey() {
        StandingOrderExecutionEntity existing = StandingOrderExecutionEntity.create(
                "so-1",
                Instant.parse("2026-06-02T00:00:00Z"),
                StandingOrderExecutionOutcome.SUCCESS,
                null,
                "trf-1",
                "idem-1",
                "corr-1");

        when(standingOrderExecutionRepository.findFirstByStandingOrderIdAndIdempotencyKeyOrderByCreatedAtDesc("so-1", "idem-1"))
                .thenReturn(Optional.of(existing));

        Optional<StandingOrderExecutionEntity> result = service.findExistingTrigger("so-1", " idem-1 ");

        assertTrue(result.isPresent());
        verify(standingOrderExecutionRepository)
                .findFirstByStandingOrderIdAndIdempotencyKeyOrderByCreatedAtDesc("so-1", "idem-1");
    }

    @Test
    void shouldReturnEmptyTriggerLookupWhenKeyMissing() {
        Optional<StandingOrderExecutionEntity> result = service.findExistingTrigger("so-1", null);
        assertFalse(result.isPresent());
    }
}

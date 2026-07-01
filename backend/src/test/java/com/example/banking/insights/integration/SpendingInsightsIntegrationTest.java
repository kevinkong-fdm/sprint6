package com.example.banking.insights.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.MovementDirection;
import com.example.banking.account.domain.MovementType;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import com.example.banking.insights.application.InsightsAggregationService;
import com.example.banking.insights.domain.ComparisonMode;
import com.example.banking.insights.domain.InsufficiencyReason;
import com.example.banking.insights.domain.SpendingCategoryMetricEntity;
import com.example.banking.insights.domain.SpendingInsightSnapshotEntity;
import com.example.banking.insights.infrastructure.SpendingCategoryMetricRepository;
import com.example.banking.insights.infrastructure.SpendingInsightSnapshotRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpendingInsightsIntegrationTest {

    @Mock
    private AccountMovementRepository accountMovementRepository;

    @Mock
    private SpendingInsightSnapshotRepository spendingInsightSnapshotRepository;

    @Mock
    private SpendingCategoryMetricRepository spendingCategoryMetricRepository;

    private InsightsAggregationService insightsAggregationService;

    @BeforeEach
    void setUp() {
        insightsAggregationService = new InsightsAggregationService(
                accountMovementRepository,
                spendingInsightSnapshotRepository,
                spendingCategoryMetricRepository);

        when(spendingInsightSnapshotRepository.save(any(SpendingInsightSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, SpendingInsightSnapshotEntity.class));
        when(spendingCategoryMetricRepository.save(any(SpendingCategoryMetricEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, SpendingCategoryMetricEntity.class));
    }

    @Test
    void shouldCalculateCategoryAndTotalDeltas() {
        LocalDate periodStart = LocalDate.parse("2026-06-01");
        LocalDate periodEnd = LocalDate.parse("2026-06-30");

        List<AccountMovementEntity> current = List.of(
                movement("acc-1", MovementType.WITHDRAWAL, new BigDecimal("30.0000"), Instant.parse("2026-06-05T10:00:00Z")),
                movement("acc-1", MovementType.TRANSFER_DEBIT, new BigDecimal("20.0000"), Instant.parse("2026-06-08T10:00:00Z")));

        List<AccountMovementEntity> previous = List.of(
                movement("acc-1", MovementType.WITHDRAWAL, new BigDecimal("10.0000"), Instant.parse("2026-05-05T10:00:00Z")),
                movement("acc-1", MovementType.TRANSFER_DEBIT, new BigDecimal("25.0000"), Instant.parse("2026-05-08T10:00:00Z")));

        when(accountMovementRepository.findByAccountIdInAndDirectionAndPostedAtGreaterThanEqualAndPostedAtLessThan(
                any(),
                any(),
                any(),
                any())).thenReturn(current, previous);

        InsightsAggregationService.InsightsAggregationResult result = insightsAggregationService.aggregateAndPersist(
                "cust-1",
                "acc-1",
                List.of("acc-1"),
                periodStart,
                periodEnd,
                ComparisonMode.PREVIOUS_PERIOD,
                "corr-1",
                ZoneId.of("Australia/Brisbane"));

        assertFalse(result.snapshot().isInsufficientData());
        assertEquals(InsufficiencyReason.NONE, result.snapshot().getInsufficiencyReason());
        assertEquals(new BigDecimal("50.0000"), result.totals().currentTotal());
        assertEquals(new BigDecimal("35.0000"), result.totals().previousTotal());
        assertEquals(new BigDecimal("15.0000"), result.totals().deltaAmount());
        assertEquals(new BigDecimal("42.8571"), result.totals().deltaPercent());
        assertEquals(2, result.categories().size());

        verify(spendingInsightSnapshotRepository).save(any(SpendingInsightSnapshotEntity.class));
        verify(spendingCategoryMetricRepository, atLeast(2)).save(any(SpendingCategoryMetricEntity.class));
    }

    @Test
    void shouldReturnInsufficientHistoryWhenPreviousPeriodMissing() {
        LocalDate periodStart = LocalDate.parse("2026-06-01");
        LocalDate periodEnd = LocalDate.parse("2026-06-30");

        List<AccountMovementEntity> current = List.of(
                movement("acc-1", MovementType.WITHDRAWAL, new BigDecimal("12.0000"), Instant.parse("2026-06-05T10:00:00Z")));

        when(accountMovementRepository.findByAccountIdInAndDirectionAndPostedAtGreaterThanEqualAndPostedAtLessThan(
                any(),
                any(),
                any(),
                any())).thenReturn(current, List.of());

        InsightsAggregationService.InsightsAggregationResult result = insightsAggregationService.aggregateAndPersist(
                "cust-1",
                "acc-1",
                List.of("acc-1"),
                periodStart,
                periodEnd,
                ComparisonMode.PREVIOUS_PERIOD,
                "corr-2",
                ZoneId.of("Australia/Brisbane"));

        assertTrue(result.snapshot().isInsufficientData());
        assertEquals(InsufficiencyReason.INSUFFICIENT_HISTORY, result.snapshot().getInsufficiencyReason());
        assertEquals(new BigDecimal("12.0000"), result.totals().currentTotal());
        assertEquals(new BigDecimal("0.0000"), result.totals().previousTotal());
        assertNotNull(result.totals().deltaAmount());
        assertNull(result.totals().deltaPercent());
    }

    private AccountMovementEntity movement(
            String accountId,
            MovementType movementType,
            BigDecimal amount,
            Instant postedAt
    ) {
        AccountMovementEntity entity = AccountMovementEntity.posted(
                accountId,
                movementType,
                amount,
                MovementDirection.DEBIT,
                new BigDecimal("100.0000"),
                new BigDecimal("80.0000"),
                "idem-1",
                "corr-1",
                "ref-1");
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "postedAt", postedAt);
        return entity;
    }
}

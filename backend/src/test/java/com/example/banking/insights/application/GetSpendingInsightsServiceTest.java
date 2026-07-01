package com.example.banking.insights.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import com.example.banking.insights.api.dto.SpendingInsightsSingleResponse;
import com.example.banking.insights.domain.ComparisonMode;
import com.example.banking.insights.domain.InsufficiencyReason;
import com.example.banking.insights.domain.SpendingInsightSnapshotEntity;
import com.example.banking.standingorder.application.PlatformTimezoneService;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSpendingInsightsServiceTest {

    @Mock
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private PlatformTimezoneService platformTimezoneService;

    @Mock
    private InsightsAggregationService insightsAggregationService;

    @Mock
    private InsightsAuditService insightsAuditService;

    private GetSpendingInsightsService service;

    @BeforeEach
    void setUp() {
        service = new GetSpendingInsightsService(
                standingOrderAuthorizationService,
                bankAccountRepository,
                platformTimezoneService,
                insightsAggregationService,
                insightsAuditService);
    }

    @Test
    void shouldReturnInsightsForOwnedSingleAccount() {
        LocalDate periodStart = LocalDate.parse("2026-06-01");
        LocalDate periodEnd = LocalDate.parse("2026-06-30");

        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account("acc-1", "cust-1")));
        when(platformTimezoneService.zoneId()).thenReturn(ZoneId.of("Australia/Brisbane"));
        when(insightsAggregationService.aggregateAndPersist(
                eq("cust-1"),
                eq("acc-1"),
                eq(List.of("acc-1")),
                eq(periodStart),
                eq(periodEnd),
                eq(ComparisonMode.PREVIOUS_PERIOD),
                eq("corr-1"),
                any()))
                .thenReturn(aggregationResult(periodStart, periodEnd, false, InsufficiencyReason.NONE));

        SpendingInsightsSingleResponse response = service.getInsights(
                periodStart,
                periodEnd,
                "PREVIOUS_PERIOD",
                "acc-1",
                "actor-1",
                "corr-1");

        assertEquals("PREVIOUS_PERIOD", response.data().comparisonMode());
        assertEquals(false, response.data().insufficientData());
        assertEquals(1, response.data().categories().size());
        assertEquals("TRANSFER", response.data().categories().get(0).category());

        verify(insightsAuditService).auditRequest(
                "cust-1",
                "acc-1",
                periodStart,
                periodEnd,
                "PREVIOUS_PERIOD",
                "corr-1",
                false);
    }

    @Test
    void shouldReturnInsightsAcrossAllOwnedAccounts() {
        LocalDate periodStart = LocalDate.parse("2026-06-01");
        LocalDate periodEnd = LocalDate.parse("2026-06-30");

        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(bankAccountRepository.findByCustomerId("cust-1"))
                .thenReturn(List.of(account("acc-1", "cust-1"), account("acc-2", "cust-1")));
        when(platformTimezoneService.zoneId()).thenReturn(ZoneId.of("Australia/Brisbane"));
        when(insightsAggregationService.aggregateAndPersist(
                eq("cust-1"),
                eq(null),
                eq(List.of("acc-1", "acc-2")),
                eq(periodStart),
                eq(periodEnd),
                eq(ComparisonMode.NONE),
                eq("corr-2"),
                any()))
                .thenReturn(aggregationResult(periodStart, periodEnd, true, InsufficiencyReason.INSUFFICIENT_HISTORY));

        SpendingInsightsSingleResponse response = service.getInsights(
                periodStart,
                periodEnd,
                "NONE",
                null,
                "actor-1",
                "corr-2");

        assertEquals("NONE", response.data().comparisonMode());
        assertEquals(true, response.data().insufficientData());
        assertEquals("INSUFFICIENT_HISTORY", response.data().insufficiencyReason());
    }

    @Test
    void shouldRejectInvalidPeriod() {
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");

        assertThrows(
                StandingOrderDomainException.InsightsValidationException.class,
                () -> service.getInsights(
                        LocalDate.parse("2026-06-30"),
                        LocalDate.parse("2026-06-01"),
                        "PREVIOUS_PERIOD",
                        null,
                        "actor-1",
                        "corr-1"));
    }

    @Test
    void shouldRejectInvalidComparisonMode() {
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");

        assertThrows(
                StandingOrderDomainException.InsightsValidationException.class,
                () -> service.getInsights(
                        LocalDate.parse("2026-06-01"),
                        LocalDate.parse("2026-06-30"),
                        "UNKNOWN",
                        null,
                        "actor-1",
                        "corr-1"));
    }

    @Test
    void shouldRejectForbiddenSingleAccount() {
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account("acc-1", "other-customer")));

        assertThrows(
                StandingOrderDomainException.AccessForbiddenException.class,
                () -> service.getInsights(
                        LocalDate.parse("2026-06-01"),
                        LocalDate.parse("2026-06-30"),
                        "PREVIOUS_PERIOD",
                        "acc-1",
                        "actor-1",
                        "corr-1"));
    }

    @Test
    void shouldRejectMissingSingleAccount() {
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.empty());

        assertThrows(
                StandingOrderDomainException.AccessForbiddenException.class,
                () -> service.getInsights(
                        LocalDate.parse("2026-06-01"),
                        LocalDate.parse("2026-06-30"),
                        "PREVIOUS_PERIOD",
                        "acc-1",
                        "actor-1",
                        "corr-1"));
    }

    private BankAccountEntity account(String accountId, String customerId) {
        BankAccountEntity entity = BankAccountEntity.create(customerId, AccountType.CHECKING, "Main");
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "accountId", accountId);
        return entity;
    }

    private InsightsAggregationService.InsightsAggregationResult aggregationResult(
            LocalDate periodStart,
            LocalDate periodEnd,
            boolean insufficientData,
            InsufficiencyReason reason
    ) {
        SpendingInsightSnapshotEntity snapshot = SpendingInsightSnapshotEntity.create(
                "cust-1",
                insufficientData ? null : "acc-1",
                periodStart,
                periodEnd,
                insufficientData ? ComparisonMode.NONE : ComparisonMode.PREVIOUS_PERIOD,
                insufficientData,
                reason,
                "corr-1");

        List<InsightsAggregationService.CategoryAggregation> categories = List.of(
                new InsightsAggregationService.CategoryAggregation(
                        "TRANSFER",
                        new BigDecimal("20.0000"),
                        insufficientData ? null : new BigDecimal("10.0000"),
                        insufficientData ? null : new BigDecimal("10.0000"),
                        insufficientData ? null : new BigDecimal("100.0000")));

        InsightsAggregationService.TotalsAggregation totals = new InsightsAggregationService.TotalsAggregation(
                new BigDecimal("20.0000"),
                insufficientData ? null : new BigDecimal("10.0000"),
                insufficientData ? null : new BigDecimal("10.0000"),
                insufficientData ? null : new BigDecimal("100.0000"));

        return new InsightsAggregationService.InsightsAggregationResult(snapshot, categories, totals);
    }
}

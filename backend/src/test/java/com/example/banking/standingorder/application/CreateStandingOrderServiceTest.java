package com.example.banking.standingorder.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.standingorder.api.dto.CreateStandingOrderRequest;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateStandingOrderServiceTest {

    @Mock
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @Mock
    private PlatformTimezoneService platformTimezoneService;

    @Mock
    private StandingOrderRepository standingOrderRepository;

    @Mock
    private StandingOrderIdempotencyService standingOrderIdempotencyService;

    @Mock
    private StandingOrderAuditService standingOrderAuditService;

    private CreateStandingOrderService service;

    @BeforeEach
    void setUp() {
        service = new CreateStandingOrderService(
                standingOrderAuthorizationService,
                platformTimezoneService,
                standingOrderRepository,
                standingOrderIdempotencyService,
                standingOrderAuditService);
    }

    @Test
    void shouldReturnExistingStandingOrderWhenIdempotent() {
        CreateStandingOrderRequest request = request("DAILY", LocalDate.parse("2026-06-01"), null, null, null);
        StandingOrderEntity existing = sampleStandingOrder("so-existing");

        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderIdempotencyService.normalizeKey("idem-1")).thenReturn("idem-1");
        when(standingOrderIdempotencyService.findExistingCreate("cust-1", "idem-1"))
                .thenReturn(Optional.of(existing));

        StandingOrderEntity result = service.create(request, "idem-1", "actor-1", "corr-1");

        assertSame(existing, result);
        verify(standingOrderRepository, never()).save(any(StandingOrderEntity.class));
    }

    @Test
    void shouldCreateStandingOrderAndPublishLifecycleAudit() {
        CreateStandingOrderRequest request = request("MONTHLY", LocalDate.parse("2026-06-01"), null, null, null);

        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedSourceAccount("src-1", "cust-1"))
                .thenReturn(BankAccountEntity.create("cust-1", AccountType.CHECKING, "Source"));
        when(standingOrderAuthorizationService.requireOwnedDestinationAccount("dst-1", "cust-1"))
                .thenReturn(BankAccountEntity.create("cust-1", AccountType.SAVINGS, "Destination"));
        when(standingOrderIdempotencyService.normalizeKey(null)).thenReturn(null);
        when(platformTimezoneService.parseDayOfWeek("MONDAY")).thenReturn(DayOfWeek.MONDAY);
        when(platformTimezoneService.parseExecutionTime("09:00")).thenReturn(LocalTime.of(9, 0));
        when(platformTimezoneService.computeNextExecution(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any())).thenReturn(Instant.parse("2026-06-02T00:00:00Z"));
        when(platformTimezoneService.timezoneCode()).thenReturn("AEST");
        when(standingOrderRepository.save(any(StandingOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, StandingOrderEntity.class));

        StandingOrderEntity result = service.create(request, null, "actor-1", "corr-1");

        assertEquals("cust-1", result.getCustomerId());
        assertEquals("src-1", result.getSourceAccountId());
        assertEquals("dst-1", result.getDestinationAccountId());

        ArgumentCaptor<StandingOrderEntity> savedCaptor = ArgumentCaptor.forClass(StandingOrderEntity.class);
        verify(standingOrderRepository).save(savedCaptor.capture());
        assertEquals(1, savedCaptor.getValue().getExecutionDayOfMonth());

        verify(standingOrderAuditService).auditLifecycle(result.getStandingOrderId(), "cust-1", "CREATE", "corr-1");
    }

    @Test
    void shouldRejectExternalBankDestination() {
        CreateStandingOrderRequest request = request(
                "DAILY",
                LocalDate.parse("2026-06-01"),
                null,
                null,
                Map.of("bankCode", "123-456"));
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");

        assertThrows(
                StandingOrderDomainException.DestinationInternalOnlyException.class,
                () -> service.create(request, null, "actor-1", "corr-1"));
    }

    @Test
    void shouldRejectInvalidFrequency() {
        CreateStandingOrderRequest request = request("YEARLY", LocalDate.parse("2026-06-01"), null, null, null);
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");

        assertThrows(
                StandingOrderDomainException.CreateValidationException.class,
                () -> service.create(request, null, "actor-1", "corr-1"));
    }

    @Test
    void shouldRejectInvalidScheduleRange() {
        CreateStandingOrderRequest request = request(
                "DAILY",
                LocalDate.parse("2026-06-10"),
                LocalDate.parse("2026-06-01"),
                null,
                null);
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");

        assertThrows(
                StandingOrderDomainException.ScheduleValidationException.class,
                () -> service.create(request, null, "actor-1", "corr-1"));
    }

    @Test
    void shouldRejectInvalidMonthlyExecutionDay() {
        CreateStandingOrderRequest request = request("MONTHLY", LocalDate.parse("2026-06-01"), null, 30, null);
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");

        assertThrows(
                StandingOrderDomainException.ScheduleValidationException.class,
                () -> service.create(request, null, "actor-1", "corr-1"));
    }

    @Test
    void shouldRejectWhenNextExecutionCannotBeComputed() {
        CreateStandingOrderRequest request = request("DAILY", LocalDate.parse("2026-06-01"), null, null, null);
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedSourceAccount("src-1", "cust-1"))
                .thenReturn(BankAccountEntity.create("cust-1", AccountType.CHECKING, "Source"));
        when(standingOrderAuthorizationService.requireOwnedDestinationAccount("dst-1", "cust-1"))
                .thenReturn(BankAccountEntity.create("cust-1", AccountType.SAVINGS, "Destination"));
        when(platformTimezoneService.parseDayOfWeek("MONDAY")).thenReturn(DayOfWeek.MONDAY);
        when(platformTimezoneService.parseExecutionTime("09:00")).thenReturn(LocalTime.of(9, 0));
        when(platformTimezoneService.computeNextExecution(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any())).thenReturn(null);

        assertThrows(
                StandingOrderDomainException.ScheduleValidationException.class,
                () -> service.create(request, null, "actor-1", "corr-1"));
    }

    private CreateStandingOrderRequest request(
            String frequency,
            LocalDate startDate,
            LocalDate endDate,
            Integer executionDayOfMonth,
            Map<String, Object> externalBankDetails
    ) {
        return new CreateStandingOrderRequest(
                "src-1",
                "dst-1",
                new BigDecimal("10.0000"),
                frequency,
                startDate,
                endDate,
                "MONDAY",
                executionDayOfMonth,
                "09:00",
                externalBankDetails);
    }

    private StandingOrderEntity sampleStandingOrder(String standingOrderId) {
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
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "standingOrderId", standingOrderId);
        return entity;
    }
}

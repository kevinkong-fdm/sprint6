package com.example.banking.standingorder.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.notification.application.StandingOrderNotificationService;
import com.example.banking.standingorder.api.dto.UpdateStandingOrderRequest;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateStandingOrderServiceTest {

    @Mock
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @Mock
    private PlatformTimezoneService platformTimezoneService;

    @Mock
    private StandingOrderRepository standingOrderRepository;

    @Mock
    private StandingOrderNotificationService standingOrderNotificationService;

    @Mock
    private StandingOrderAuditService standingOrderAuditService;

    private UpdateStandingOrderService service;

    @BeforeEach
    void setUp() {
        service = new UpdateStandingOrderService(
                standingOrderAuthorizationService,
                platformTimezoneService,
                standingOrderRepository,
                standingOrderNotificationService,
                standingOrderAuditService);
    }

    @Test
    void shouldRejectNoOpUpdate() {
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1"))
                .thenReturn(standingOrder("ACTIVE"));

        UpdateStandingOrderRequest request = new UpdateStandingOrderRequest(null, null, null, null, null, null, null);

        assertThrows(
                StandingOrderDomainException.UpdateValidationException.class,
                () -> service.update("so-1", request, "actor-1", "corr-1"));
    }

    @Test
    void shouldRejectCanceledStandingOrderUpdate() {
        StandingOrderEntity canceled = standingOrder("CANCELED");
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1"))
                .thenReturn(canceled);

        UpdateStandingOrderRequest request = new UpdateStandingOrderRequest("dst-2", null, null, null, null, null, null);

        assertThrows(
                StandingOrderDomainException.ImmutableFieldUpdateException.class,
                () -> service.update("so-1", request, "actor-1", "corr-1"));
    }

    @Test
    void shouldRejectInvalidFrequency() {
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1"))
                .thenReturn(standingOrder("ACTIVE"));

        UpdateStandingOrderRequest request = new UpdateStandingOrderRequest(null, null, "YEARLY", null, null, null, null);

        assertThrows(
                StandingOrderDomainException.UpdateValidationException.class,
                () -> service.update("so-1", request, "actor-1", "corr-1"));
    }

    @Test
    void shouldRejectInvalidMonthlyDay() {
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1"))
                .thenReturn(standingOrder("ACTIVE"));

        UpdateStandingOrderRequest request = new UpdateStandingOrderRequest(null, null, "MONTHLY", null, null, 30, "08:30");

        assertThrows(
                StandingOrderDomainException.ScheduleValidationException.class,
                () -> service.update("so-1", request, "actor-1", "corr-1"));
    }

    @Test
    void shouldRejectWhenNoNextExecutionForActiveSchedule() {
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1"))
                .thenReturn(standingOrder("ACTIVE"));
        when(platformTimezoneService.parseExecutionTime("08:30")).thenReturn(LocalTime.of(8, 30));
        when(platformTimezoneService.computeNextExecution(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any())).thenReturn(null);

        UpdateStandingOrderRequest request = new UpdateStandingOrderRequest(null, null, "DAILY", null, null, null, "08:30");

        assertThrows(
                StandingOrderDomainException.ScheduleValidationException.class,
                () -> service.update("so-1", request, "actor-1", "corr-1"));
    }

    @Test
    void shouldUpdateStandingOrderAndPublishEvents() {
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1"))
                .thenReturn(standingOrder("ACTIVE"));
        when(standingOrderAuthorizationService.requireOwnedDestinationAccount("dst-2", "cust-1"))
                .thenReturn(BankAccountEntity.create("cust-1", AccountType.SAVINGS, "Destination"));
        when(platformTimezoneService.parseDayOfWeek("MONDAY")).thenReturn(DayOfWeek.MONDAY);
        when(platformTimezoneService.parseExecutionTime("08:30")).thenReturn(LocalTime.of(8, 30));
        when(platformTimezoneService.computeNextExecution(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any())).thenReturn(Instant.parse("2026-06-03T00:00:00Z"));
        when(standingOrderRepository.save(any(StandingOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, StandingOrderEntity.class));

        UpdateStandingOrderRequest request = new UpdateStandingOrderRequest(
                " dst-2 ",
                new BigDecimal("22.0000"),
                "WEEKLY",
                LocalDate.parse("2026-07-01"),
                "MONDAY",
                null,
                "08:30");

        StandingOrderEntity updated = service.update("so-1", request, "actor-1", "corr-1");

        assertEquals("dst-2", updated.getDestinationAccountId());
        assertEquals(new BigDecimal("22.0000"), updated.getAmount());
        assertEquals(StandingOrderFrequency.WEEKLY, updated.getFrequency());
        verify(standingOrderNotificationService).publishLifecycleUpdate(updated, "UPDATE", "corr-1");
        verify(standingOrderAuditService).auditLifecycle(updated.getStandingOrderId(), "cust-1", "UPDATE", "corr-1");
    }

    private StandingOrderEntity standingOrder(String status) {
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
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "standingOrderId", "so-1");

        if ("CANCELED".equals(status)) {
            entity.cancel();
        } else if ("PAUSED".equals(status)) {
            entity.pause();
        }

        return entity;
    }
}

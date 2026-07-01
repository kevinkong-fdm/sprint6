package com.example.banking.standingorder.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import com.example.banking.standingorder.domain.StandingOrderStatus;
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
class StandingOrderLifecycleServiceTest {

    @Mock
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @Mock
    private PlatformTimezoneService platformTimezoneService;

    @Mock
    private StandingOrderRepository standingOrderRepository;

    @Mock
    private StandingOrderAuditService standingOrderAuditService;

    private StandingOrderLifecycleService service;

    @BeforeEach
    void setUp() {
        service = new StandingOrderLifecycleService(
                standingOrderAuthorizationService,
                platformTimezoneService,
                standingOrderRepository,
                standingOrderAuditService);
    }

    @Test
    void shouldPauseActiveStandingOrder() {
        StandingOrderEntity standingOrder = standingOrder(StandingOrderStatus.ACTIVE);
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1")).thenReturn(standingOrder);
        when(standingOrderRepository.save(any(StandingOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, StandingOrderEntity.class));

        StandingOrderEntity paused = service.pause("so-1", "actor-1", "corr-1");

        assertEquals(StandingOrderStatus.PAUSED, paused.getStatus());
        verify(standingOrderAuditService).auditLifecycle("so-1", "cust-1", "PAUSE", "corr-1");
    }

    @Test
    void shouldResumePausedStandingOrder() {
        StandingOrderEntity paused = standingOrder(StandingOrderStatus.PAUSED);
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1")).thenReturn(paused);
        when(platformTimezoneService.parseDayOfWeek("MONDAY")).thenReturn(DayOfWeek.MONDAY);
        when(platformTimezoneService.parseExecutionTime("09:00")).thenReturn(LocalTime.of(9, 0));
        when(platformTimezoneService.computeNextExecution(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any())).thenReturn(Instant.parse("2026-06-09T00:00:00Z"));
        when(standingOrderRepository.save(any(StandingOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, StandingOrderEntity.class));

        StandingOrderEntity resumed = service.resume("so-1", "actor-1", "corr-1");

        assertEquals(StandingOrderStatus.ACTIVE, resumed.getStatus());
        assertEquals(Instant.parse("2026-06-09T00:00:00Z"), resumed.getNextExecutionAt());
        verify(standingOrderAuditService).auditLifecycle("so-1", "cust-1", "RESUME", "corr-1");
    }

    @Test
    void shouldCancelStandingOrder() {
        StandingOrderEntity standingOrder = standingOrder(StandingOrderStatus.ACTIVE);
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1")).thenReturn(standingOrder);
        when(standingOrderRepository.save(any(StandingOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, StandingOrderEntity.class));

        StandingOrderEntity canceled = service.cancel("so-1", "actor-1", "corr-1");

        assertEquals(StandingOrderStatus.CANCELED, canceled.getStatus());
        assertEquals(null, canceled.getNextExecutionAt());
        verify(standingOrderAuditService).auditLifecycle("so-1", "cust-1", "CANCEL", "corr-1");
    }

    @Test
    void shouldRejectPauseAndResumeForCanceledStandingOrder() {
        StandingOrderEntity canceled = standingOrder(StandingOrderStatus.CANCELED);
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1")).thenReturn(canceled);

        assertThrows(
                StandingOrderDomainException.ImmutableFieldUpdateException.class,
                () -> service.pause("so-1", "actor-1", "corr-1"));

        assertThrows(
                StandingOrderDomainException.ImmutableFieldUpdateException.class,
                () -> service.resume("so-1", "actor-1", "corr-1"));
    }

    private StandingOrderEntity standingOrder(StandingOrderStatus status) {
        StandingOrderEntity entity = StandingOrderEntity.create(
                "cust-1",
                "src-1",
                "dst-1",
                new BigDecimal("10.0000"),
                StandingOrderFrequency.WEEKLY,
                LocalDate.parse("2026-06-01"),
                null,
                "MONDAY",
                null,
                "09:00",
                Instant.parse("2026-06-02T00:00:00Z"),
                "AEST",
                "idem-1");

        org.springframework.test.util.ReflectionTestUtils.setField(entity, "standingOrderId", "so-1");

        if (status == StandingOrderStatus.PAUSED) {
            entity.pause();
        } else if (status == StandingOrderStatus.CANCELED) {
            entity.cancel();
        }

        return entity;
    }
}

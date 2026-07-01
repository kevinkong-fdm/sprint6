package com.example.banking.notification.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.notification.application.NotificationDispatchService;
import com.example.banking.notification.application.StandingOrderNotificationService;
import com.example.banking.notification.domain.NotificationDispatchStatus;
import com.example.banking.notification.domain.NotificationEventEntity;
import com.example.banking.notification.domain.NotificationEventType;
import com.example.banking.notification.domain.NotificationPreferenceEntity;
import com.example.banking.notification.infrastructure.NotificationEventRepository;
import com.example.banking.notification.infrastructure.NotificationPreferenceRepository;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchIntegrationTest {

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private NotificationEventRepository notificationEventRepository;

    private NotificationDispatchService notificationDispatchService;
    private StandingOrderNotificationService standingOrderNotificationService;

    @BeforeEach
    void setUp() {
        notificationDispatchService = new NotificationDispatchService(notificationEventRepository);
        standingOrderNotificationService = new StandingOrderNotificationService(
                notificationPreferenceRepository,
                notificationEventRepository,
                notificationDispatchService);
    }

    @Test
    void shouldGenerateAndDispatchLifecycleNotification() {
        StandingOrderEntity standingOrder = standingOrder("so-1");
        when(notificationEventRepository.save(any(NotificationEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, NotificationEventEntity.class));

        when(notificationPreferenceRepository.findById("cust-1"))
                .thenReturn(Optional.of(NotificationPreferenceEntity.systemDefault("cust-1")));
        when(notificationEventRepository.findByDedupeKey(any())).thenReturn(Optional.empty());

        standingOrderNotificationService.publishLifecycleUpdate(standingOrder, "CREATE", "corr-1");

        ArgumentCaptor<NotificationEventEntity> captor = ArgumentCaptor.forClass(NotificationEventEntity.class);
        verify(notificationEventRepository, atLeast(2)).save(captor.capture());

        List<NotificationEventEntity> saves = captor.getAllValues();
        NotificationEventEntity dispatched = saves.get(saves.size() - 1);
        assertEquals(NotificationDispatchStatus.SENT, dispatched.getDispatchStatus());
        assertEquals(1, dispatched.getDispatchAttemptCount());
        assertEquals(NotificationEventType.LIFECYCLE_UPDATED, dispatched.getEventType());
    }

    @Test
    void shouldSkipWhenLifecycleDedupeExists() {
        StandingOrderEntity standingOrder = standingOrder("so-1");

        when(notificationPreferenceRepository.findById("cust-1"))
                .thenReturn(Optional.of(NotificationPreferenceEntity.systemDefault("cust-1")));
        when(notificationEventRepository.findByDedupeKey(any()))
                .thenReturn(Optional.of(NotificationEventEntity.pending(
                        "cust-1",
                        "so-1",
                        null,
                        NotificationEventType.LIFECYCLE_UPDATED,
                        "title",
                        "message",
                        "so-1:LIFECYCLE:CREATE:corr-1",
                        "corr-1")));

        standingOrderNotificationService.publishLifecycleUpdate(standingOrder, "CREATE", "corr-1");

        verify(notificationEventRepository, never()).save(any(NotificationEventEntity.class));
    }

    @Test
    void shouldFailDispatchAfterRetryLimitForInvalidPayload() {
        when(notificationEventRepository.save(any(NotificationEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, NotificationEventEntity.class));

        NotificationEventEntity invalid = NotificationEventEntity.pending(
                "cust-1",
                "so-1",
                null,
                NotificationEventType.LIFECYCLE_UPDATED,
                "   ",
                "   ",
                "dedupe-1",
                "corr-1");

        assertThrows(
                StandingOrderDomainException.NotificationDispatchFailedException.class,
                () -> notificationDispatchService.dispatch(invalid));

        verify(notificationEventRepository, atLeast(4)).save(any(NotificationEventEntity.class));
    }

    private StandingOrderEntity standingOrder(String standingOrderId) {
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

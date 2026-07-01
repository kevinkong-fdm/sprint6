package com.example.banking.notification.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.banking.notification.api.dto.NotificationListResponse;
import com.example.banking.notification.domain.NotificationEventEntity;
import com.example.banking.notification.domain.NotificationEventType;
import com.example.banking.notification.infrastructure.NotificationEventRepository;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ListNotificationsServiceTest {

    @Mock
    private StandingOrderAuthorizationService authorizationService;

    @Mock
    private NotificationEventRepository notificationEventRepository;

    private ListNotificationsService service;

    @BeforeEach
    void setUp() {
        service = new ListNotificationsService(authorizationService, notificationEventRepository);
    }

    @Test
    void shouldListNotificationsWithPagingAndFilters() {
        NotificationEventEntity event = NotificationEventEntity.pending(
                "cust-1",
                "so-1",
                "exe-1",
                NotificationEventType.EXECUTION_SUCCESS,
                "Success",
            "Done",
            "dedupe-1",
                "corr-1");
        event.markSent();

        when(authorizationService.requireActorId(any())).thenReturn("cust-1");
        when(notificationEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));

        NotificationListResponse response = service.list("cust-1", "EXECUTION_SUCCESS", "SENT", 1, 20, "corr-1");

        assertEquals(1, response.data().items().size());
        assertEquals("EXECUTION_SUCCESS", response.data().items().get(0).eventType());
        assertEquals("SENT", response.data().items().get(0).dispatchStatus());
    }

    @Test
    void shouldDefaultAndNormalizePaging() {
        when(authorizationService.requireActorId(any())).thenReturn("cust-1");
        when(notificationEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        NotificationListResponse response = service.list("cust-1", null, null, -1, 9999, "corr-1");

        assertEquals(1, response.data().page());
        assertEquals(200, response.data().size());
        assertEquals(0, response.data().items().size());
    }

    @Test
    void shouldRejectInvalidFilters() {
        when(authorizationService.requireActorId(any())).thenReturn("cust-1");

        assertThrows(StandingOrderDomainException.CreateValidationException.class,
                () -> service.list("cust-1", "invalid", null, 1, 20, "corr-1"));

        assertThrows(StandingOrderDomainException.CreateValidationException.class,
                () -> service.list("cust-1", null, "unknown", 1, 20, "corr-1"));
    }
}

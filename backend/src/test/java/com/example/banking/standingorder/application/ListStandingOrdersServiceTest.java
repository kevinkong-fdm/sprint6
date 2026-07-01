package com.example.banking.standingorder.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.banking.standingorder.api.dto.StandingOrderResponseMapper;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import com.example.banking.standingorder.domain.StandingOrderStatus;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class ListStandingOrdersServiceTest {

    @Mock
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @Mock
    private StandingOrderRepository standingOrderRepository;

    private ListStandingOrdersService service;

    @BeforeEach
    void setUp() {
        service = new ListStandingOrdersService(
                standingOrderAuthorizationService,
                standingOrderRepository,
                new StandingOrderResponseMapper());
    }

    @Test
    void shouldListStandingOrdersWithPagingNormalization() {
        StandingOrderEntity entity = standingOrder(StandingOrderStatus.ACTIVE);
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderRepository.findByCustomerId(any(), any())).thenReturn(new PageImpl<>(List.of(entity)));

        var response = service.list("actor-1", null, -1, 999, "corr-1");

        assertEquals(1, response.data().items().size());
        assertEquals("ACTIVE", response.data().items().get(0).status());
        assertEquals(1, response.data().page());
        assertEquals(200, response.data().size());
    }

    @Test
    void shouldFilterStandingOrdersByStatus() {
        StandingOrderEntity paused = standingOrder(StandingOrderStatus.PAUSED);
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderRepository.findByCustomerIdAndStatus(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(paused)));

        var response = service.list("actor-1", "paused", 1, 20, "corr-1");

        assertEquals(1, response.data().items().size());
        assertEquals("PAUSED", response.data().items().get(0).status());
    }

    @Test
    void shouldRejectInvalidStatusFilter() {
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");

        assertThrows(
                StandingOrderDomainException.CreateValidationException.class,
                () -> service.list("actor-1", "bad-status", 1, 20, "corr-1"));
    }

    private StandingOrderEntity standingOrder(StandingOrderStatus status) {
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

        if (status == StandingOrderStatus.PAUSED) {
            entity.pause();
        } else if (status == StandingOrderStatus.CANCELED) {
            entity.cancel();
        }

        return entity;
    }
}

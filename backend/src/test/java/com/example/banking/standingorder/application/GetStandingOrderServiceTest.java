package com.example.banking.standingorder.application;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.standingorder.api.dto.StandingOrderResponseMapper;
import com.example.banking.standingorder.api.dto.StandingOrderSingleResponse;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetStandingOrderServiceTest {

    @Mock
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @Mock
    private StandingOrderResponseMapper standingOrderResponseMapper;

    private GetStandingOrderService service;

    @BeforeEach
    void setUp() {
        service = new GetStandingOrderService(standingOrderAuthorizationService, standingOrderResponseMapper);
    }

    @Test
    void shouldReturnOwnedStandingOrderResponse() {
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

        StandingOrderSingleResponse mapped = new StandingOrderSingleResponse("corr-1", Instant.now(), null);

        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1")).thenReturn(entity);
        when(standingOrderResponseMapper.toSingleResponse(entity, "corr-1")).thenReturn(mapped);

        StandingOrderSingleResponse result = service.getById("so-1", "actor-1", "corr-1");

        assertSame(mapped, result);
        verify(standingOrderAuthorizationService).requireOwnedStandingOrder("so-1", "cust-1");
        verify(standingOrderResponseMapper).toSingleResponse(entity, "corr-1");
    }
}

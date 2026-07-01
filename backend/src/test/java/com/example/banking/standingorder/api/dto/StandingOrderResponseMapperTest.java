package com.example.banking.standingorder.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionOutcome;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class StandingOrderResponseMapperTest {

    private final StandingOrderResponseMapper mapper = new StandingOrderResponseMapper();

    @Test
    void shouldMapStandingOrderEntityToResponses() {
        StandingOrderEntity entity = StandingOrderEntity.create(
                "cust-1",
                "src-1",
                "dst-1",
                new BigDecimal("12.3456"),
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

        StandingOrderResponse dto = mapper.toStandingOrderResponse(entity);
        StandingOrderSingleResponse single = mapper.toSingleResponse(entity, "corr-1");
        StandingOrderListResponse list = mapper.toListResponse(List.of(dto), 1, 20, 1, "corr-1");

        assertEquals("so-1", dto.standingOrderId());
        assertEquals("12.3456", dto.amount());
        assertEquals("DAILY", dto.frequency());
        assertEquals("corr-1", single.correlationId());
        assertEquals(1, list.data().items().size());
        assertEquals(1, list.data().total());
    }

    @Test
    void shouldMapExecutionEntityToResponses() {
        StandingOrderExecutionEntity execution = StandingOrderExecutionEntity.create(
                "so-1",
                Instant.parse("2026-06-02T00:00:00Z"),
                StandingOrderExecutionOutcome.SUCCESS,
                null,
                "trf-1",
                "idem-1",
                "corr-1");

        StandingOrderExecutionResponse executionDto = mapper.toStandingOrderExecutionResponse(execution);
        StandingOrderExecutionSingleResponse single = mapper.toExecutionSingleResponse(execution, "corr-1");
        StandingOrderExecutionListResponse list = mapper.toExecutionListResponse(List.of(executionDto), 1, 20, 1, "corr-1");

        assertEquals("SUCCESS", executionDto.outcome());
        assertEquals("trf-1", executionDto.transferReferenceId());
        assertEquals("corr-1", single.correlationId());
        assertEquals(1, list.data().items().size());
        assertEquals(1, list.data().total());
    }
}

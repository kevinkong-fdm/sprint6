package com.example.banking.standingorder.api.dto;

import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StandingOrderResponseMapper {

    public StandingOrderResponse toStandingOrderResponse(StandingOrderEntity entity) {
        return new StandingOrderResponse(
                entity.getStandingOrderId(),
                entity.getCustomerId(),
                entity.getSourceAccountId(),
                entity.getDestinationAccountId(),
                asMoney(entity.getAmount()),
                entity.getFrequency().name(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getExecutionDayOfWeek(),
                entity.getExecutionDayOfMonth(),
                entity.getExecutionTime(),
                entity.getStatus().name(),
                entity.getTimezoneCode(),
                entity.getNextExecutionAt(),
                entity.getLastExecutionAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public StandingOrderExecutionResponse toStandingOrderExecutionResponse(StandingOrderExecutionEntity entity) {
        return new StandingOrderExecutionResponse(
                entity.getStandingOrderExecutionId(),
                entity.getStandingOrderId(),
                entity.getScheduledFor(),
                entity.getTriggeredAt(),
                entity.getOutcome().name(),
                entity.getFailureReasonCode(),
                entity.getTransferReferenceId(),
                entity.getCorrelationId());
    }

    public StandingOrderSingleResponse toSingleResponse(StandingOrderEntity entity, String correlationId) {
        return new StandingOrderSingleResponse(correlationId, Instant.now(), toStandingOrderResponse(entity));
    }

    public StandingOrderListResponse toListResponse(
            List<StandingOrderResponse> items,
            int page,
            int size,
            long total,
            String correlationId
    ) {
        return new StandingOrderListResponse(
                correlationId,
                Instant.now(),
                new StandingOrderListData(items, page, size, total));
    }

    public StandingOrderExecutionSingleResponse toExecutionSingleResponse(
            StandingOrderExecutionEntity entity,
            String correlationId
    ) {
        return new StandingOrderExecutionSingleResponse(correlationId, Instant.now(), toStandingOrderExecutionResponse(entity));
    }

    public StandingOrderExecutionListResponse toExecutionListResponse(
            List<StandingOrderExecutionResponse> items,
            int page,
            int size,
            long total,
            String correlationId
    ) {
        return new StandingOrderExecutionListResponse(
                correlationId,
                Instant.now(),
                new StandingOrderExecutionListData(items, page, size, total));
    }

    private String asMoney(BigDecimal amount) {
        if (amount == null) {
            return "0.0000";
        }
        return amount.setScale(4, RoundingMode.HALF_UP).toPlainString();
    }
}

package com.example.banking.standingorder.api.dto;

import java.time.Instant;
import java.time.LocalDate;

public record StandingOrderResponse(
        String standingOrderId,
        String customerId,
        String sourceAccountId,
        String destinationAccountId,
        String amount,
        String frequency,
        LocalDate startDate,
        LocalDate endDate,
        String executionDayOfWeek,
        Integer executionDayOfMonth,
        String executionTime,
        String status,
        String timezone,
        Instant nextExecutionAt,
        Instant lastExecutionAt,
        Instant createdAt,
        Instant updatedAt
) {
}

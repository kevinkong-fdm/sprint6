package com.example.banking.standingorder.api.dto;

import java.time.Instant;

public record StandingOrderExecutionResponse(
        String standingOrderExecutionId,
        String standingOrderId,
        Instant scheduledFor,
        Instant triggeredAt,
        String outcome,
        String failureReasonCode,
        String transferReferenceId,
        String correlationId
) {
}

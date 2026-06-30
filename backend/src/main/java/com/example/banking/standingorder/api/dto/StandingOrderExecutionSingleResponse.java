package com.example.banking.standingorder.api.dto;

import java.time.Instant;

public record StandingOrderExecutionSingleResponse(
        String correlationId,
        Instant timestamp,
        StandingOrderExecutionResponse data
) {
}

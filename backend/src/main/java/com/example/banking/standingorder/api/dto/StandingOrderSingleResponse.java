package com.example.banking.standingorder.api.dto;

import java.time.Instant;

public record StandingOrderSingleResponse(
        String correlationId,
        Instant timestamp,
        StandingOrderResponse data
) {
}

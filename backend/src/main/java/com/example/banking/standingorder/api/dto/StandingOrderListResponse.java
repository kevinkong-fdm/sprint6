package com.example.banking.standingorder.api.dto;

import java.time.Instant;

public record StandingOrderListResponse(
        String correlationId,
        Instant timestamp,
        StandingOrderListData data
) {
}

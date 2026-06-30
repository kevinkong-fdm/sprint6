package com.example.banking.standingorder.api.dto;

import java.time.Instant;

public record StandingOrderExecutionListResponse(
        String correlationId,
        Instant timestamp,
        StandingOrderExecutionListData data
) {
}

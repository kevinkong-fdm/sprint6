package com.example.banking.insights.api.dto;

import java.time.Instant;

public record SpendingInsightsSingleResponse(
        String correlationId,
        Instant timestamp,
        SpendingInsightsDataResponse data
) {
}

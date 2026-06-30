package com.example.banking.insights.api.dto;

public record SpendingInsightsTotalsResponse(
        String currentTotal,
        String previousTotal,
        String deltaAmount,
        Double deltaPercent
) {
}

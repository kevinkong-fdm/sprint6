package com.example.banking.insights.api.dto;

public record SpendingInsightsCategoryResponse(
        String category,
        String currentTotal,
        String previousTotal,
        String deltaAmount,
        Double deltaPercent
) {
}

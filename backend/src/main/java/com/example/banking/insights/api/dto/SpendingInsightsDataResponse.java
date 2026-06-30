package com.example.banking.insights.api.dto;

import java.time.LocalDate;
import java.util.List;

public record SpendingInsightsDataResponse(
        LocalDate periodStart,
        LocalDate periodEnd,
        String comparisonMode,
        boolean insufficientData,
        String insufficiencyReason,
        List<SpendingInsightsCategoryResponse> categories,
        SpendingInsightsTotalsResponse totals
) {
}

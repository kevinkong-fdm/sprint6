package com.example.banking.statement.api.dto;

import java.time.Instant;
import java.util.List;

public record MonthlyStatementListResponse(
        String correlationId,
        Instant timestamp,
        List<MonthlyStatementResponse> data
) {
}

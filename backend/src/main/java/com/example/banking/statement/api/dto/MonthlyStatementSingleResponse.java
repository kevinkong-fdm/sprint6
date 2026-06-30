package com.example.banking.statement.api.dto;

import java.time.Instant;

public record MonthlyStatementSingleResponse(
        String correlationId,
        Instant timestamp,
        MonthlyStatementResponse data
) {
}

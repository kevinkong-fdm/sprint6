package com.example.banking.statement.api.dto;

import java.time.Instant;
import java.util.List;

public record MonthlyStatementResponse(
        String accountId,
        String customerId,
        String month,
        String timezone,
        String openingBalance,
        String closingBalance,
        String totalDebits,
        String totalCredits,
        List<StatementLineItemResponse> lineItems,
        Instant generatedAt
) {
}

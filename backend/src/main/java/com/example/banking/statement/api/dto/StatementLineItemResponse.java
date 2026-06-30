package com.example.banking.statement.api.dto;

import java.time.Instant;

public record StatementLineItemResponse(
        String transactionId,
        Instant postedAt,
        String entryType,
        String amount,
        String balanceAfter,
        String description
) {
}

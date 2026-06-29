package com.example.banking.account.api.dto;

import java.time.Instant;

public record TransactionHistoryItemResponse(
        String movementId,
        String movementType,
        String amount,
        String balanceAfter,
        String status,
        Instant createdAt,
        Instant postedAt,
        String referenceId
) {
}

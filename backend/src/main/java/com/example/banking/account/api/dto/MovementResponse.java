package com.example.banking.account.api.dto;

import java.time.Instant;

public record MovementResponse(
        String movementId,
        String accountId,
        String movementType,
        String amount,
        String balanceAfter,
        Instant postedAt
) {
}

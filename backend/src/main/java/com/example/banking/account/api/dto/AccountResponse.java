package com.example.banking.account.api.dto;

import java.time.Instant;

public record AccountResponse(
        String accountId,
        String customerId,
        String accountType,
        String nickname,
        String currencyCode,
        String availableBalance,
        String ledgerBalance,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}

package com.example.banking.account.api.dto;

import jakarta.validation.constraints.Size;

public record DeleteAccountRequest(
        String closeoutDestinationAccountId,
        @Size(min = 1, max = 120) String idempotencyKey
) {
}

package com.example.banking.account.api.dto;

import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(
        @Size(min = 1, max = 80) String nickname,
        String accountType,
        @Size(min = 3, max = 3) String currencyCode,
        String status
) {
}

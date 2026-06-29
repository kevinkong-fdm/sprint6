package com.example.banking.account.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank String accountType,
        @Size(min = 1, max = 80) String nickname
) {
}

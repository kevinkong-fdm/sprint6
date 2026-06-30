package com.example.banking.account.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank String accountType,
        @NotBlank @Size(max = 80) String nickname
) {
}

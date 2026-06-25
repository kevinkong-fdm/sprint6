package com.example.banking.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TokenRefreshRequest(
        @NotBlank @Size(min = 32, max = 4096) String refreshToken
) {
}

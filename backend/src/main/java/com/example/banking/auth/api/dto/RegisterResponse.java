package com.example.banking.auth.api.dto;

import java.time.Instant;

public record RegisterResponse(
        String userId,
        String email,
        Instant createdAt
) {
}

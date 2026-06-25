package com.example.banking.auth.api;

import java.time.Instant;

public record ErrorResponse(
        String errorCode,
        String message,
        String correlationId,
        Instant timestamp
) {
    public static ErrorResponse of(String errorCode, String message, String correlationId) {
        return new ErrorResponse(errorCode, message, correlationId, Instant.now());
    }
}

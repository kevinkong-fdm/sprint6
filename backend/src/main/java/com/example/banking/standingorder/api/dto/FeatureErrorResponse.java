package com.example.banking.standingorder.api.dto;

import java.time.Instant;

public record FeatureErrorResponse(
        String errorCode,
        String message,
        String correlationId,
        Instant timestamp
) {
    public static FeatureErrorResponse of(String errorCode, String message, String correlationId) {
        return new FeatureErrorResponse(errorCode, message, correlationId == null ? "" : correlationId, Instant.now());
    }
}

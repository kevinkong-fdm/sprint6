package com.example.banking.account.api.dto;

public record AccountErrorResponse(
        String errorCode,
        String message,
        String correlationId
) {
    public static AccountErrorResponse of(String errorCode, String message, String correlationId) {
        return new AccountErrorResponse(errorCode, message, correlationId == null ? "" : correlationId);
    }
}

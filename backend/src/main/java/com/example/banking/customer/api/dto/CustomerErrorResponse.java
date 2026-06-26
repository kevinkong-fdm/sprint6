package com.example.banking.customer.api.dto;

public record CustomerErrorResponse(
        String errorCode,
        String message,
        String correlationId
) {
    public static CustomerErrorResponse of(String errorCode, String message, String correlationId) {
        return new CustomerErrorResponse(errorCode, message, correlationId);
    }
}

package com.example.banking.auth.application;

public class DomainException extends RuntimeException {
    private final String errorCode;
    private final int status;

    public DomainException(String errorCode, String message, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getStatus() {
        return status;
    }
}

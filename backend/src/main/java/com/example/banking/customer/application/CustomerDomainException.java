package com.example.banking.customer.application;

public class CustomerDomainException extends RuntimeException {

    private final String errorCode;
    private final int status;

    protected CustomerDomainException(String errorCode, String message, int status) {
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

    public static final class CreateValidationException extends CustomerDomainException {
        public CreateValidationException(String message) {
            super("CUST-CRT-001", message, 400);
        }
    }

    public static final class DuplicateIdentityException extends CustomerDomainException {
        public DuplicateIdentityException(String message) {
            super("CUST-CRT-002", message, 409);
        }
    }

    public static final class GetNotFoundException extends CustomerDomainException {
        public GetNotFoundException(String message) {
            super("CUST-GET-001", message, 404);
        }
    }

    public static final class UpdateValidationException extends CustomerDomainException {
        public UpdateValidationException(String message) {
            super("CUST-UPD-001", message, 400);
        }
    }

    public static final class ImmutableFieldUpdateException extends CustomerDomainException {
        public ImmutableFieldUpdateException(String message) {
            super("CUST-UPD-003", message, 422);
        }
    }

    public static final class DeleteNotFoundException extends CustomerDomainException {
        public DeleteNotFoundException(String message) {
            super("CUST-DEL-001", message, 404);
        }
    }

    public static final class CascadeDeleteFailureException extends CustomerDomainException {
        public CascadeDeleteFailureException(String message) {
            super("CUST-DEL-002", message, 409);
        }
    }
}

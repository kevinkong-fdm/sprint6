package com.example.banking.account.application;

public class AccountDomainException extends RuntimeException {

    private final String errorCode;
    private final int status;

    protected AccountDomainException(String errorCode, String message, int status) {
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

    public static final class AuthenticationRequiredException extends AccountDomainException {
        public AuthenticationRequiredException() {
            super("AUTH-ACC-001", "Authentication required or session invalid.", 401);
        }
    }

    public static final class AccessForbiddenException extends AccountDomainException {
        public AccessForbiddenException() {
            super("AUTH-ACC-002", "Account access forbidden for authenticated caller.", 403);
        }
    }

    public static final class CreateValidationException extends AccountDomainException {
        public CreateValidationException(String message) {
            super("ACCT-CRT-001", message, 400);
        }
    }

    public static final class UnsupportedAccountTypeException extends AccountDomainException {
        public UnsupportedAccountTypeException() {
            super("ACCT-CRT-002", "Unsupported account type.", 400);
        }
    }

    public static final class AccountNotFoundException extends AccountDomainException {
        public AccountNotFoundException() {
            super("ACCT-GET-001", "Account not found.", 404);
        }
    }

    public static final class UpdateValidationException extends AccountDomainException {
        public UpdateValidationException(String message) {
            super("ACCT-UPD-001", message, 400);
        }
    }

    public static final class ImmutableFieldUpdateException extends AccountDomainException {
        public ImmutableFieldUpdateException() {
            super("ACCT-UPD-002", "Immutable account field update attempted.", 422);
        }
    }

    public static final class DeleteNotFoundException extends AccountDomainException {
        public DeleteNotFoundException() {
            super("ACCT-DEL-001", "Account not found for delete operation.", 404);
        }
    }

    public static final class DeleteEligibilityException extends AccountDomainException {
        public DeleteEligibilityException(String message, int status) {
            super("ACCT-DEL-002", message, status);
        }
    }

    public static final class DepositValidationException extends AccountDomainException {
        public DepositValidationException() {
            super("TXN-DEP-001", "Deposit amount validation failed.", 400);
        }
    }

    public static final class WithdrawalValidationException extends AccountDomainException {
        public WithdrawalValidationException() {
            super("TXN-WDR-001", "Withdrawal amount validation failed.", 400);
        }
    }

    public static final class InsufficientWithdrawalFundsException extends AccountDomainException {
        public InsufficientWithdrawalFundsException() {
            super("TXN-WDR-002", "Insufficient funds for withdrawal.", 409);
        }
    }

    public static final class TransferValidationException extends AccountDomainException {
        public TransferValidationException() {
            super("TXN-TRF-001", "Transfer amount validation failed.", 400);
        }
    }

    public static final class TransferPairingException extends AccountDomainException {
        public TransferPairingException() {
            super("TXN-TRF-002", "Invalid transfer account pairing.", 409);
        }
    }

    public static final class InsufficientTransferFundsException extends AccountDomainException {
        public InsufficientTransferFundsException() {
            super("TXN-TRF-003", "Insufficient funds for transfer.", 409);
        }
    }

    public static final class HistoryValidationException extends AccountDomainException {
        public HistoryValidationException() {
            super("TXN-HIS-001", "Transaction history filter validation failed.", 400);
        }
    }
}

package com.example.banking.standingorder.application;

public class StandingOrderDomainException extends RuntimeException {

    private final String errorCode;
    private final int status;

    protected StandingOrderDomainException(String errorCode, String message, int status) {
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

    public static final class AuthenticationRequiredException extends StandingOrderDomainException {
        public AuthenticationRequiredException() {
            super("AUTH-FEAT-001", "Authentication required or session invalid.", 401);
        }
    }

    public static final class AccessForbiddenException extends StandingOrderDomainException {
        public AccessForbiddenException() {
            super("AUTH-FEAT-002", "Feature access forbidden for authenticated caller.", 403);
        }
    }

    public static final class CreateValidationException extends StandingOrderDomainException {
        public CreateValidationException(String message) {
            super("SO-SET-001", message, 400);
        }
    }

    public static final class ScheduleValidationException extends StandingOrderDomainException {
        public ScheduleValidationException() {
            this("Standing-order schedule configuration invalid.");
        }

        public ScheduleValidationException(String message) {
            super(
                    "SO-SET-002",
                    message == null || message.isBlank() ? "Standing-order schedule configuration invalid." : message,
                    400);
        }
    }

    public static final class SourceUnauthorizedException extends StandingOrderDomainException {
        public SourceUnauthorizedException() {
            super("SO-SET-003", "Standing-order source account unauthorized.", 403);
        }
    }

    public static final class DestinationInternalOnlyException extends StandingOrderDomainException {
        public DestinationInternalOnlyException() {
            super("SO-SET-004", "Standing-order destination must be an internal platform account.", 409);
        }
    }

    public static final class DestinationOwnershipMismatchException extends StandingOrderDomainException {
        public DestinationOwnershipMismatchException() {
            super("SO-SET-005", "Standing-order destination account is not owned by the authenticated customer.", 409);
        }
    }

    public static final class UpdateValidationException extends StandingOrderDomainException {
        public UpdateValidationException(String message) {
            super("SO-UPD-001", message, 400);
        }
    }

    public static final class ImmutableFieldUpdateException extends StandingOrderDomainException {
        public ImmutableFieldUpdateException() {
            super("SO-UPD-002", "Cannot update a canceled standing order.", 422);
        }
    }

    public static final class StandingOrderNotFoundException extends StandingOrderDomainException {
        public StandingOrderNotFoundException() {
            super("SO-DEL-001", "Standing-order not found.", 404);
        }
    }

    public static final class ExecutionInsufficientFundsException extends StandingOrderDomainException {
        public ExecutionInsufficientFundsException() {
            super("SO-EXE-001", "Standing-order execution failed due to insufficient funds.", 409);
        }
    }

    public static final class ExecutionSkippedException extends StandingOrderDomainException {
        public ExecutionSkippedException() {
            this("Standing-order execution skipped due to source account state.");
        }

        public ExecutionSkippedException(String message) {
            super(
                    "SO-EXE-002",
                    message == null || message.isBlank()
                            ? "Standing-order execution skipped due to source account state."
                            : message,
                    409);
        }
    }

    public static final class StatementValidationException extends StandingOrderDomainException {
        public StatementValidationException() {
            this("Monthly statement period validation failed.");
        }

        public StatementValidationException(String message) {
            super(
                    "STMT-001",
                    message == null || message.isBlank() ? "Monthly statement period validation failed." : message,
                    400);
        }
    }

    public static final class StatementUnavailableException extends StandingOrderDomainException {
        public StatementUnavailableException() {
            this("Monthly statement unavailable for requested account or period.");
        }

        public StatementUnavailableException(String message) {
            super(
                    "STMT-002",
                    message == null || message.isBlank()
                            ? "Monthly statement unavailable for requested account or period."
                            : message,
                    404);
        }
    }

    public static final class InsightsValidationException extends StandingOrderDomainException {
        public InsightsValidationException() {
            super("INS-001", "Spending-insight filter validation failed.", 400);
        }
    }

    public static final class UnexpectedServiceException extends StandingOrderDomainException {
        public UnexpectedServiceException() {
            super("SYS-FEAT-001", "Unexpected feature service error.", 500);
        }
    }
}

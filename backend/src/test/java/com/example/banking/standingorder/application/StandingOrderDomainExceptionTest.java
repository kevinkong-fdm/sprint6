package com.example.banking.standingorder.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StandingOrderDomainExceptionTest {

    @Test
    void shouldExposeExpectedErrorCodesAndStatuses() {
        assertException(new StandingOrderDomainException.AuthenticationRequiredException(), "AUTH-FEAT-001", 401);
        assertException(new StandingOrderDomainException.AccessForbiddenException(), "AUTH-FEAT-002", 403);
        assertException(new StandingOrderDomainException.CreateValidationException("x"), "SO-SET-001", 400);
        assertException(new StandingOrderDomainException.ScheduleValidationException(), "SO-SET-002", 400);
        assertException(new StandingOrderDomainException.SourceUnauthorizedException(), "SO-SET-003", 403);
        assertException(new StandingOrderDomainException.DestinationInternalOnlyException(), "SO-SET-004", 409);
        assertException(new StandingOrderDomainException.DestinationOwnershipMismatchException(), "SO-SET-005", 409);
        assertException(new StandingOrderDomainException.UpdateValidationException("x"), "SO-UPD-001", 400);
        assertException(new StandingOrderDomainException.ImmutableFieldUpdateException(), "SO-UPD-002", 422);
        assertException(new StandingOrderDomainException.StandingOrderNotFoundException(), "SO-DEL-001", 404);
        assertException(new StandingOrderDomainException.ExecutionInsufficientFundsException(), "SO-EXE-001", 409);
        assertException(new StandingOrderDomainException.ExecutionSkippedException(), "SO-EXE-002", 409);
        assertException(new StandingOrderDomainException.StatementValidationException(), "STMT-001", 400);
        assertException(new StandingOrderDomainException.StatementUnavailableException(), "STMT-002", 404);
        assertException(new StandingOrderDomainException.InsightsValidationException(), "INS-001", 400);
        assertException(new StandingOrderDomainException.UnexpectedServiceException(), "SYS-FEAT-001", 500);
    }

    @Test
    void shouldUseFallbackMessagesForBlankCustomMessages() {
        assertEquals(
                "Standing-order schedule configuration invalid.",
                new StandingOrderDomainException.ScheduleValidationException(" ").getMessage());

        assertEquals(
                "Standing-order execution skipped due to source account state.",
                new StandingOrderDomainException.ExecutionSkippedException(" ").getMessage());

        assertEquals(
                "Monthly statement period validation failed.",
                new StandingOrderDomainException.StatementValidationException(" ").getMessage());

        assertEquals(
                "Monthly statement unavailable for requested account or period.",
                new StandingOrderDomainException.StatementUnavailableException(" ").getMessage());
    }

    private void assertException(StandingOrderDomainException ex, String errorCode, int status) {
        assertEquals(errorCode, ex.getErrorCode());
        assertEquals(status, ex.getStatus());
    }
}

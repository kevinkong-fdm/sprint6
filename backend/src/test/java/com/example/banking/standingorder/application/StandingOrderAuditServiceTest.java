package com.example.banking.standingorder.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class StandingOrderAuditServiceTest {

    private final StandingOrderAuditService service = new StandingOrderAuditService();

    @Test
    void shouldAuditLifecycleWithoutThrowing() {
        assertDoesNotThrow(() -> service.auditLifecycle(" so-1 ", " ", " UPDATE ", null));
    }
}

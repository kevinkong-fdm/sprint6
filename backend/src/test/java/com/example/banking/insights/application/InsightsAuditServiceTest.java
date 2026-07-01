package com.example.banking.insights.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class InsightsAuditServiceTest {

    private final InsightsAuditService service = new InsightsAuditService();

    @Test
    void shouldAuditInsightsRequestWithoutThrowing() {
        assertDoesNotThrow(() -> service.auditRequest(
                " actor-1 ",
                " ",
                LocalDate.parse("2026-06-01"),
                LocalDate.parse("2026-06-30"),
                " PREVIOUS_PERIOD ",
                null,
                true));
    }
}

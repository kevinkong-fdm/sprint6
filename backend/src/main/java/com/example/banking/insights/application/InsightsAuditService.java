package com.example.banking.insights.application;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InsightsAuditService {

    private static final Logger log = LoggerFactory.getLogger(InsightsAuditService.class);

    public void auditRequest(
            String actorId,
            String accountId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String comparisonMode,
            String correlationId,
            boolean insufficientData
    ) {
        log.info(
                "insights-audit actorId={} accountScope={} periodStart={} periodEnd={} comparisonMode={} insufficientData={} correlationId={}",
                safe(actorId),
                accountId == null || accountId.isBlank() ? "all-owned" : "single-account",
                periodStart,
                periodEnd,
                safe(comparisonMode),
                insufficientData,
                safe(correlationId));
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "n/a";
        }
        return value.trim();
    }
}

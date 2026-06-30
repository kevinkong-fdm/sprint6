package com.example.banking.standingorder.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StandingOrderAuditService {

    private static final Logger log = LoggerFactory.getLogger(StandingOrderAuditService.class);

    public void auditLifecycle(String standingOrderId, String actorId, String action, String correlationId) {
        log.info(
                "standing-order-audit action={} standingOrderId={} actorId={} correlationId={}",
                safe(action),
                safe(standingOrderId),
                safe(actorId),
                safe(correlationId));
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "n/a";
        }
        return value.trim();
    }
}

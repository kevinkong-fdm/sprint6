package com.example.banking.standingorder.application;

import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderStatus;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StandingOrderLifecycleService {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final PlatformTimezoneService platformTimezoneService;
    private final StandingOrderRepository standingOrderRepository;
    private final StandingOrderAuditService standingOrderAuditService;

    public StandingOrderLifecycleService(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            PlatformTimezoneService platformTimezoneService,
            StandingOrderRepository standingOrderRepository,
            StandingOrderAuditService standingOrderAuditService
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.platformTimezoneService = platformTimezoneService;
        this.standingOrderRepository = standingOrderRepository;
        this.standingOrderAuditService = standingOrderAuditService;
    }

    @Transactional
    public StandingOrderEntity pause(String standingOrderId, String actorId, String correlationId) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);
        StandingOrderEntity standingOrder = standingOrderAuthorizationService.requireOwnedStandingOrder(standingOrderId, resolvedActorId);

        if (standingOrder.getStatus() == StandingOrderStatus.CANCELED) {
            throw new StandingOrderDomainException.ImmutableFieldUpdateException();
        }

        if (standingOrder.getStatus() != StandingOrderStatus.PAUSED) {
            standingOrder.pause();
        }

        StandingOrderEntity saved = standingOrderRepository.save(standingOrder);
        standingOrderAuditService.auditLifecycle(saved.getStandingOrderId(), resolvedActorId, "PAUSE", correlationId);
        return saved;
    }

    @Transactional
    public StandingOrderEntity resume(String standingOrderId, String actorId, String correlationId) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);
        StandingOrderEntity standingOrder = standingOrderAuthorizationService.requireOwnedStandingOrder(standingOrderId, resolvedActorId);

        if (standingOrder.getStatus() == StandingOrderStatus.CANCELED) {
            throw new StandingOrderDomainException.ImmutableFieldUpdateException();
        }

        if (standingOrder.getStatus() == StandingOrderStatus.PAUSED) {
            standingOrder.resume(platformTimezoneService.computeNextExecution(
                    standingOrder.getFrequency(),
                    standingOrder.getStartDate(),
                    standingOrder.getEndDate(),
                    platformTimezoneService.parseDayOfWeek(standingOrder.getExecutionDayOfWeek()),
                    standingOrder.getExecutionDayOfMonth(),
                    platformTimezoneService.parseExecutionTime(standingOrder.getExecutionTime()),
                    java.time.Instant.now()));
        }

        StandingOrderEntity saved = standingOrderRepository.save(standingOrder);
        standingOrderAuditService.auditLifecycle(saved.getStandingOrderId(), resolvedActorId, "RESUME", correlationId);
        return saved;
    }

    @Transactional
    public StandingOrderEntity cancel(String standingOrderId, String actorId, String correlationId) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);
        StandingOrderEntity standingOrder = standingOrderAuthorizationService.requireOwnedStandingOrder(standingOrderId, resolvedActorId);

        if (standingOrder.getStatus() != StandingOrderStatus.CANCELED) {
            standingOrder.cancel();
        }

        StandingOrderEntity saved = standingOrderRepository.save(standingOrder);
        standingOrderAuditService.auditLifecycle(saved.getStandingOrderId(), resolvedActorId, "CANCEL", correlationId);
        return saved;
    }
}

package com.example.banking.standingorder.application;

import com.example.banking.notification.application.StandingOrderNotificationService;
import com.example.banking.standingorder.api.dto.UpdateStandingOrderRequest;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import com.example.banking.standingorder.domain.StandingOrderStatus;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateStandingOrderService {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final PlatformTimezoneService platformTimezoneService;
    private final StandingOrderRepository standingOrderRepository;
    private final StandingOrderNotificationService standingOrderNotificationService;
    private final StandingOrderAuditService standingOrderAuditService;

    public UpdateStandingOrderService(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            PlatformTimezoneService platformTimezoneService,
            StandingOrderRepository standingOrderRepository,
            StandingOrderNotificationService standingOrderNotificationService,
            StandingOrderAuditService standingOrderAuditService
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.platformTimezoneService = platformTimezoneService;
        this.standingOrderRepository = standingOrderRepository;
        this.standingOrderNotificationService = standingOrderNotificationService;
        this.standingOrderAuditService = standingOrderAuditService;
    }

    @Transactional
    public StandingOrderEntity update(
            String standingOrderId,
            UpdateStandingOrderRequest request,
            String actorId,
            String correlationId
    ) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);
        StandingOrderEntity standingOrder = standingOrderAuthorizationService.requireOwnedStandingOrder(standingOrderId, resolvedActorId);

        if (standingOrder.getStatus() == StandingOrderStatus.CANCELED) {
            throw new StandingOrderDomainException.ImmutableFieldUpdateException();
        }

        if (isNoOp(request)) {
            throw new StandingOrderDomainException.UpdateValidationException(
                    "Provide at least one field to update: destination account, amount, frequency, end date, weekday, day of month, or execution time.");
        }

        if (request.destinationAccountId() != null && !request.destinationAccountId().isBlank()) {
            standingOrderAuthorizationService.requireOwnedDestinationAccount(request.destinationAccountId().trim(), resolvedActorId);
        }

        StandingOrderFrequency frequency = request.frequency() == null
                ? standingOrder.getFrequency()
                : StandingOrderFrequency.from(request.frequency());

        if (frequency == null) {
            throw new StandingOrderDomainException.UpdateValidationException(
                "Frequency must be one of DAILY, WEEKLY, or MONTHLY.");
        }

        LocalDate endDate = request.endDate() == null ? standingOrder.getEndDate() : request.endDate();
        if (endDate != null && endDate.isBefore(standingOrder.getStartDate())) {
            throw new StandingOrderDomainException.ScheduleValidationException(
                "End date cannot be earlier than start date.");
        }

        DayOfWeek dayOfWeek = request.executionDayOfWeek() == null
                ? platformTimezoneService.parseDayOfWeek(standingOrder.getExecutionDayOfWeek())
                : platformTimezoneService.parseDayOfWeek(request.executionDayOfWeek());

        Integer dayOfMonth = request.executionDayOfMonth() == null
                ? standingOrder.getExecutionDayOfMonth()
                : request.executionDayOfMonth();

        if (frequency == StandingOrderFrequency.MONTHLY) {
            if (dayOfMonth == null || dayOfMonth < 1 || dayOfMonth > 28) {
                throw new StandingOrderDomainException.ScheduleValidationException(
                        "Monthly standing orders require execution day of month between 1 and 28.");
            }
        } else {
            dayOfMonth = null;
        }

        LocalTime executionTime = request.executionTime() == null
                ? platformTimezoneService.parseExecutionTime(standingOrder.getExecutionTime())
                : platformTimezoneService.parseExecutionTime(request.executionTime());

        Instant nextExecutionAt = standingOrder.getStatus() == StandingOrderStatus.ACTIVE
                ? platformTimezoneService.computeNextExecution(
                        frequency,
                        standingOrder.getStartDate(),
                        endDate,
                        dayOfWeek,
                        dayOfMonth,
                        executionTime,
                        Instant.now())
                : standingOrder.getNextExecutionAt();

        if (standingOrder.getStatus() == StandingOrderStatus.ACTIVE && nextExecutionAt == null) {
            throw new StandingOrderDomainException.ScheduleValidationException(
                    "Updated schedule produces no valid next execution. Review frequency, end date, and execution timing.");
        }

        standingOrder.applyUpdate(
                request.destinationAccountId() == null ? null : request.destinationAccountId().trim(),
                request.amount(),
                frequency,
                endDate,
                dayOfWeek == null ? null : dayOfWeek.name(),
                dayOfMonth,
                executionTime.toString(),
                nextExecutionAt);

        StandingOrderEntity saved = standingOrderRepository.save(standingOrder);
        standingOrderNotificationService.publishLifecycleUpdate(saved, "UPDATE", correlationId);
        standingOrderAuditService.auditLifecycle(saved.getStandingOrderId(), resolvedActorId, "UPDATE", correlationId);
        return saved;
    }

    private boolean isNoOp(UpdateStandingOrderRequest request) {
        return request.destinationAccountId() == null
                && request.amount() == null
                && request.frequency() == null
                && request.endDate() == null
                && request.executionDayOfWeek() == null
                && request.executionDayOfMonth() == null
                && request.executionTime() == null;
    }
}

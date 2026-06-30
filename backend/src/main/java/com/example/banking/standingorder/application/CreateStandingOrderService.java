package com.example.banking.standingorder.application;

import com.example.banking.notification.application.StandingOrderNotificationService;
import com.example.banking.standingorder.api.dto.CreateStandingOrderRequest;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateStandingOrderService {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final PlatformTimezoneService platformTimezoneService;
    private final StandingOrderRepository standingOrderRepository;
    private final StandingOrderIdempotencyService standingOrderIdempotencyService;
    private final StandingOrderNotificationService standingOrderNotificationService;
    private final StandingOrderAuditService standingOrderAuditService;

    public CreateStandingOrderService(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            PlatformTimezoneService platformTimezoneService,
            StandingOrderRepository standingOrderRepository,
            StandingOrderIdempotencyService standingOrderIdempotencyService,
            StandingOrderNotificationService standingOrderNotificationService,
            StandingOrderAuditService standingOrderAuditService
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.platformTimezoneService = platformTimezoneService;
        this.standingOrderRepository = standingOrderRepository;
        this.standingOrderIdempotencyService = standingOrderIdempotencyService;
        this.standingOrderNotificationService = standingOrderNotificationService;
        this.standingOrderAuditService = standingOrderAuditService;
    }

    @Transactional
    public StandingOrderEntity create(
            CreateStandingOrderRequest request,
            String idempotencyKey,
            String actorId,
            String correlationId
    ) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);

        if (request.externalBankDetails() != null && !request.externalBankDetails().isEmpty()) {
            throw new StandingOrderDomainException.DestinationInternalOnlyException();
        }

        StandingOrderFrequency frequency = StandingOrderFrequency.from(request.frequency());
        if (frequency == null) {
            throw new StandingOrderDomainException.CreateValidationException("Standing-order setup validation failed.");
        }

        LocalDate startDate = request.startDate();
        if (startDate == null) {
            throw new StandingOrderDomainException.CreateValidationException("Standing-order setup validation failed.");
        }

        LocalDate endDate = request.endDate();
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new StandingOrderDomainException.ScheduleValidationException();
        }

        if (request.sourceAccountId().trim().equals(request.destinationAccountId().trim())) {
            throw new StandingOrderDomainException.CreateValidationException("Standing-order setup validation failed.");
        }

        String normalizedIdempotencyKey = standingOrderIdempotencyService.normalizeKey(idempotencyKey);
        if (normalizedIdempotencyKey != null) {
            StandingOrderEntity existing = standingOrderIdempotencyService
                    .findExistingCreate(resolvedActorId, normalizedIdempotencyKey)
                    .orElse(null);
            if (existing != null) {
                return existing;
            }
        }

        standingOrderAuthorizationService.requireOwnedSourceAccount(request.sourceAccountId().trim(), resolvedActorId);
        standingOrderAuthorizationService.requireOwnedDestinationAccount(request.destinationAccountId().trim(), resolvedActorId);

        DayOfWeek executionDayOfWeek = platformTimezoneService.parseDayOfWeek(request.executionDayOfWeek());
        Integer executionDayOfMonth = normalizeExecutionDayOfMonth(request.executionDayOfMonth(), frequency);
        LocalTime executionTime = platformTimezoneService.parseExecutionTime(request.executionTime());

        Instant nextExecutionAt = platformTimezoneService.computeNextExecution(
                frequency,
                startDate,
                endDate,
                executionDayOfWeek,
                executionDayOfMonth,
                executionTime,
                Instant.now());

        if (nextExecutionAt == null) {
            throw new StandingOrderDomainException.ScheduleValidationException();
        }

        StandingOrderEntity saved = standingOrderRepository.save(StandingOrderEntity.create(
                resolvedActorId,
                request.sourceAccountId().trim(),
                request.destinationAccountId().trim(),
                request.amount(),
                frequency,
                startDate,
                endDate,
                executionDayOfWeek == null ? null : executionDayOfWeek.name(),
                executionDayOfMonth,
                executionTime.toString(),
                nextExecutionAt,
                platformTimezoneService.timezoneCode(),
                normalizedIdempotencyKey));

        standingOrderNotificationService.publishLifecycleUpdate(saved, "CREATE", correlationId);
        standingOrderAuditService.auditLifecycle(saved.getStandingOrderId(), resolvedActorId, "CREATE", correlationId);

        return saved;
    }

    private Integer normalizeExecutionDayOfMonth(Integer executionDayOfMonth, StandingOrderFrequency frequency) {
        if (frequency != StandingOrderFrequency.MONTHLY) {
            return null;
        }

        if (executionDayOfMonth == null) {
            return 1;
        }
        if (executionDayOfMonth < 1 || executionDayOfMonth > 28) {
            throw new StandingOrderDomainException.ScheduleValidationException();
        }
        return executionDayOfMonth;
    }
}

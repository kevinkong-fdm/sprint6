package com.example.banking.standingorder.application;

import com.example.banking.account.api.dto.TransferRequest;
import com.example.banking.account.api.dto.TransferResponse;
import com.example.banking.account.application.TransferFundsService;
import com.example.banking.account.domain.AccountStatus;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.standingorder.api.dto.StandingOrderExecutionListResponse;
import com.example.banking.standingorder.api.dto.StandingOrderExecutionResponse;
import com.example.banking.standingorder.api.dto.StandingOrderResponseMapper;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionOutcome;
import com.example.banking.standingorder.domain.StandingOrderStatus;
import com.example.banking.standingorder.infrastructure.StandingOrderExecutionRepository;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StandingOrderExecutionService {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final StandingOrderIdempotencyService standingOrderIdempotencyService;
    private final PlatformTimezoneService platformTimezoneService;
    private final TransferFundsService transferFundsService;
    private final StandingOrderExecutionRepository standingOrderExecutionRepository;
    private final StandingOrderRepository standingOrderRepository;
    private final StandingOrderResponseMapper standingOrderResponseMapper;
    private final StandingOrderAuditService standingOrderAuditService;

    public StandingOrderExecutionService(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            StandingOrderIdempotencyService standingOrderIdempotencyService,
            PlatformTimezoneService platformTimezoneService,
            TransferFundsService transferFundsService,
            StandingOrderExecutionRepository standingOrderExecutionRepository,
            StandingOrderRepository standingOrderRepository,
            StandingOrderResponseMapper standingOrderResponseMapper,
            StandingOrderAuditService standingOrderAuditService
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.standingOrderIdempotencyService = standingOrderIdempotencyService;
        this.platformTimezoneService = platformTimezoneService;
        this.transferFundsService = transferFundsService;
        this.standingOrderExecutionRepository = standingOrderExecutionRepository;
        this.standingOrderRepository = standingOrderRepository;
        this.standingOrderResponseMapper = standingOrderResponseMapper;
        this.standingOrderAuditService = standingOrderAuditService;
    }

    @Transactional
    public StandingOrderExecutionEntity trigger(
            String standingOrderId,
            String idempotencyKey,
            String actorId,
            String correlationId
    ) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);
        StandingOrderEntity standingOrder = standingOrderAuthorizationService.requireOwnedStandingOrder(standingOrderId, resolvedActorId);

        String normalizedIdempotencyKey = standingOrderIdempotencyService.normalizeKey(idempotencyKey);
        if (normalizedIdempotencyKey != null) {
            StandingOrderExecutionEntity existing = standingOrderIdempotencyService
                    .findExistingTrigger(standingOrderId, normalizedIdempotencyKey)
                    .orElse(null);
            if (existing != null) {
                return existing;
            }
        }

        Instant scheduledFor = standingOrder.getNextExecutionAt() == null ? Instant.now() : standingOrder.getNextExecutionAt();

        if (standingOrder.getStatus() != StandingOrderStatus.ACTIVE) {
            StandingOrderExecutionEntity execution = standingOrderExecutionRepository.save(StandingOrderExecutionEntity.create(
                    standingOrder.getStandingOrderId(),
                    scheduledFor,
                    StandingOrderExecutionOutcome.SKIPPED,
                    "SO-EXE-002",
                    null,
                    normalizedIdempotencyKey,
                    correlationId));
            standingOrderAuditService.auditLifecycle(standingOrder.getStandingOrderId(), resolvedActorId, "EXECUTION_SKIPPED", correlationId);
                    throw new StandingOrderDomainException.ExecutionSkippedException(
                        "Execution skipped because this standing order is not active. Resume it before triggering execution.");
        }

        BankAccountEntity source = standingOrderAuthorizationService.requireOwnedSourceAccount(
                standingOrder.getSourceAccountId(),
                resolvedActorId);

        if (source.getStatus() != AccountStatus.ACTIVE) {
            StandingOrderExecutionEntity execution = standingOrderExecutionRepository.save(StandingOrderExecutionEntity.create(
                    standingOrder.getStandingOrderId(),
                    scheduledFor,
                    StandingOrderExecutionOutcome.SKIPPED,
                    "SO-EXE-002",
                    null,
                    normalizedIdempotencyKey,
                    correlationId));
            standingOrderAuditService.auditLifecycle(standingOrder.getStandingOrderId(), resolvedActorId, "EXECUTION_SKIPPED", correlationId);
                    throw new StandingOrderDomainException.ExecutionSkippedException(
                        "Execution skipped because the source account is not active.");
        }

        if (!source.canDebit(standingOrder.getAmount())) {
            StandingOrderExecutionEntity execution = standingOrderExecutionRepository.save(StandingOrderExecutionEntity.create(
                    standingOrder.getStandingOrderId(),
                    scheduledFor,
                    StandingOrderExecutionOutcome.FAILED,
                    "SO-EXE-001",
                    null,
                    normalizedIdempotencyKey,
                    correlationId));
            standingOrder.markExecuted(Instant.now(), platformTimezoneService.computeNextExecutionAfterRun(standingOrder, Instant.now()));
            standingOrderRepository.save(standingOrder);
            standingOrderAuditService.auditLifecycle(standingOrder.getStandingOrderId(), resolvedActorId, "EXECUTION_FAILED", correlationId);
            throw new StandingOrderDomainException.ExecutionInsufficientFundsException();
        }

        TransferResponse transferResponse = transferFundsService.transfer(
                new TransferRequest(
                        standingOrder.getSourceAccountId(),
                        standingOrder.getDestinationAccountId(),
                        standingOrder.getAmount(),
                        normalizedIdempotencyKey),
                correlationId,
                resolvedActorId);

        StandingOrderExecutionEntity execution = standingOrderExecutionRepository.save(StandingOrderExecutionEntity.create(
                standingOrder.getStandingOrderId(),
                scheduledFor,
                StandingOrderExecutionOutcome.SUCCESS,
                null,
                transferResponse.transferId(),
                normalizedIdempotencyKey,
                correlationId));

        standingOrder.markExecuted(Instant.now(), platformTimezoneService.computeNextExecutionAfterRun(standingOrder, Instant.now()));
        standingOrderRepository.save(standingOrder);
        standingOrderAuditService.auditLifecycle(standingOrder.getStandingOrderId(), resolvedActorId, "EXECUTION_SUCCESS", correlationId);

        return execution;
    }

    public StandingOrderExecutionListResponse listExecutions(
            String standingOrderId,
            String outcome,
            int page,
            int size,
            String actorId,
            String correlationId
    ) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);
        standingOrderAuthorizationService.requireOwnedStandingOrder(standingOrderId, resolvedActorId);

        StandingOrderExecutionOutcome resolvedOutcome = parseOutcome(outcome);
        int resolvedPage = Math.max(1, page);
        int resolvedSize = Math.min(200, Math.max(1, size));

        PageRequest pageRequest = PageRequest.of(
                resolvedPage - 1,
                resolvedSize,
                Sort.by(Sort.Direction.DESC, "triggeredAt", "standingOrderExecutionId"));

        Page<StandingOrderExecutionEntity> result;
        if (resolvedOutcome == null) {
            result = standingOrderExecutionRepository.findByStandingOrderId(standingOrderId, pageRequest);
        } else {
            result = standingOrderExecutionRepository.findByStandingOrderIdAndOutcome(standingOrderId, resolvedOutcome, pageRequest);
        }

        List<StandingOrderExecutionResponse> items = new ArrayList<>();
        for (StandingOrderExecutionEntity entity : result.getContent()) {
            items.add(standingOrderResponseMapper.toStandingOrderExecutionResponse(entity));
        }

        return standingOrderResponseMapper.toExecutionListResponse(
                items,
                resolvedPage,
                resolvedSize,
                result.getTotalElements(),
                correlationId);
    }

    private StandingOrderExecutionOutcome parseOutcome(String outcome) {
        if (outcome == null || outcome.isBlank()) {
            return null;
        }
        try {
            return StandingOrderExecutionOutcome.valueOf(outcome.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new StandingOrderDomainException.UpdateValidationException("Standing-order update validation failed.");
        }
    }
}

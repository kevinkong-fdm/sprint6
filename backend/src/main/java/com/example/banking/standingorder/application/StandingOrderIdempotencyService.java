package com.example.banking.standingorder.application;

import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionEntity;
import com.example.banking.standingorder.infrastructure.StandingOrderExecutionRepository;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class StandingOrderIdempotencyService {

    private final StandingOrderRepository standingOrderRepository;
    private final StandingOrderExecutionRepository standingOrderExecutionRepository;

    public StandingOrderIdempotencyService(
            StandingOrderRepository standingOrderRepository,
            StandingOrderExecutionRepository standingOrderExecutionRepository
    ) {
        this.standingOrderRepository = standingOrderRepository;
        this.standingOrderExecutionRepository = standingOrderExecutionRepository;
    }

    public Optional<StandingOrderEntity> findExistingCreate(String customerId, String idempotencyKey) {
        String normalizedKey = normalizeKey(idempotencyKey);
        if (normalizedKey == null) {
            return Optional.empty();
        }

        return standingOrderRepository.findFirstByCustomerIdAndIdempotencyKeyOrderByCreatedAtDesc(
                customerId,
                normalizedKey);
    }

    public Optional<StandingOrderExecutionEntity> findExistingTrigger(String standingOrderId, String idempotencyKey) {
        String normalizedKey = normalizeKey(idempotencyKey);
        if (normalizedKey == null) {
            return Optional.empty();
        }

        return standingOrderExecutionRepository.findFirstByStandingOrderIdAndIdempotencyKeyOrderByCreatedAtDesc(
                standingOrderId,
                normalizedKey);
    }

    public String normalizeKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        String normalized = idempotencyKey.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}

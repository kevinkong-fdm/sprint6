package com.example.banking.customer.application;

import com.example.banking.customer.domain.CustomerLifecycleEventEntity;
import com.example.banking.customer.domain.LifecycleAction;
import com.example.banking.customer.domain.LifecycleOutcome;
import com.example.banking.customer.infrastructure.CustomerLifecycleEventRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CustomerLifecycleEventService {

    private final CustomerLifecycleEventRepository customerLifecycleEventRepository;

    public CustomerLifecycleEventService(CustomerLifecycleEventRepository customerLifecycleEventRepository) {
        this.customerLifecycleEventRepository = customerLifecycleEventRepository;
    }

    public void recordSuccess(String customerId, LifecycleAction action, String actorId, String correlationId, String metadataJson) {
        customerLifecycleEventRepository.save(CustomerLifecycleEventEntity.of(
                customerId,
                action,
                LifecycleOutcome.SUCCESS,
                null,
                actorId,
                effectiveCorrelationId(correlationId),
                metadataJson));
    }

    public void recordFailure(
            String customerId,
            LifecycleAction action,
            String actorId,
            String correlationId,
            String errorCode,
            String metadataJson
    ) {
        customerLifecycleEventRepository.save(CustomerLifecycleEventEntity.of(
                customerId,
                action,
                LifecycleOutcome.FAILURE,
                errorCode,
                actorId,
                effectiveCorrelationId(correlationId),
                metadataJson));
    }

    private String effectiveCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return correlationId;
    }
}

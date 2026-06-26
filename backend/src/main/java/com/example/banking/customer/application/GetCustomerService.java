package com.example.banking.customer.application;

import com.example.banking.customer.api.dto.CustomerResponse;
import com.example.banking.customer.api.dto.CustomerResponseMapper;
import com.example.banking.customer.domain.CustomerProfileEntity;
import com.example.banking.customer.domain.LifecycleAction;
import com.example.banking.customer.infrastructure.CustomerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCustomerService {

    private final CustomerProfileRepository customerProfileRepository;
    private final CustomerResponseMapper customerResponseMapper;
    private final CustomerLifecycleEventService customerLifecycleEventService;

    public GetCustomerService(
            CustomerProfileRepository customerProfileRepository,
            CustomerResponseMapper customerResponseMapper,
            CustomerLifecycleEventService customerLifecycleEventService
    ) {
        this.customerProfileRepository = customerProfileRepository;
        this.customerResponseMapper = customerResponseMapper;
        this.customerLifecycleEventService = customerLifecycleEventService;
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(String customerId, String correlationId, String actorId) {
        CustomerProfileEntity profile = customerProfileRepository.findDetailedByCustomerId(customerId)
                .orElseThrow(() -> {
                    customerLifecycleEventService.recordFailure(
                            customerId,
                            LifecycleAction.GET,
                            effectiveActor(actorId),
                            correlationId,
                            "CUST-GET-001",
                            "{}");
                    return new CustomerDomainException.GetNotFoundException("Customer not found.");
                });

        customerLifecycleEventService.recordSuccess(
                customerId,
                LifecycleAction.GET,
                effectiveActor(actorId),
                correlationId,
                "{}");
        return customerResponseMapper.toResponse(profile);
    }

    private String effectiveActor(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            return "operator";
        }
        return actorId;
    }
}

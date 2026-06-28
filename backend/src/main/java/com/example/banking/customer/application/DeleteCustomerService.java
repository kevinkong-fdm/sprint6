package com.example.banking.customer.application;

import com.example.banking.auth.application.DeleteAccountService;
import com.example.banking.customer.domain.CustomerProfileEntity;
import com.example.banking.customer.domain.LifecycleAction;
import com.example.banking.customer.infrastructure.CustomerProfileRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteCustomerService {

    private final CustomerProfileRepository customerProfileRepository;
    private final CustomerLifecycleEventService customerLifecycleEventService;
    private final DeleteAccountService deleteAccountService;

    public DeleteCustomerService(
            CustomerProfileRepository customerProfileRepository,
            CustomerLifecycleEventService customerLifecycleEventService,
            DeleteAccountService deleteAccountService
    ) {
        this.customerProfileRepository = customerProfileRepository;
        this.customerLifecycleEventService = customerLifecycleEventService;
        this.deleteAccountService = deleteAccountService;
    }

    @Transactional
    public void delete(String customerId, String correlationId, String actorId) {
        CustomerProfileEntity profile = customerProfileRepository.findDetailedByCustomerId(customerId)
                .orElseThrow(() -> {
                    customerLifecycleEventService.recordFailure(
                            customerId,
                            LifecycleAction.DELETE,
                            effectiveActor(actorId),
                            correlationId,
                            "CUST-DEL-001",
                            "{}");
                    return new CustomerDomainException.DeleteNotFoundException("Customer not found for delete operation.");
                });

        try {
            deleteAccountService.deleteAccountWithAuthData(
                    profile.getCustomerId(),
                    profile.getEmailNormalized());
            customerLifecycleEventService.recordSuccess(
                    customerId,
                    LifecycleAction.DELETE,
                    effectiveActor(actorId),
                    correlationId,
                    "{}");
        } catch (DataAccessException | IllegalStateException ex) {
            customerLifecycleEventService.recordFailure(
                    customerId,
                    LifecycleAction.DELETE,
                    effectiveActor(actorId),
                    correlationId,
                    "CUST-DEL-002",
                    "{}");
            throw new CustomerDomainException.CascadeDeleteFailureException(
                    "Customer hard delete failed due to cascading dependency deletion error.");
        }
    }

    private String effectiveActor(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            return "operator";
        }
        return actorId;
    }
}

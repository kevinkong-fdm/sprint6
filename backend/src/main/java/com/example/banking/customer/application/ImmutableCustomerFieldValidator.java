package com.example.banking.customer.application;

import com.example.banking.customer.api.dto.CustomerUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class ImmutableCustomerFieldValidator {

    public void validate(CustomerUpdateRequest request) {
        if (request.email() != null || request.dateOfBirth() != null || request.customerId() != null) {
            throw new CustomerDomainException.ImmutableFieldUpdateException(
                    "Immutable customer field update attempted.");
        }
    }
}

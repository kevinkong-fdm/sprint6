package com.example.banking.customer.application;

import com.example.banking.customer.domain.CustomerProfileEntity;
import com.example.banking.customer.infrastructure.CustomerProfileRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomerCascadeDeleteCoordinator {

    private final CustomerProfileRepository customerProfileRepository;

    public CustomerCascadeDeleteCoordinator(CustomerProfileRepository customerProfileRepository) {
        this.customerProfileRepository = customerProfileRepository;
    }

    public void deleteCustomer(CustomerProfileEntity customerProfile) {
        customerProfileRepository.delete(customerProfile);
        customerProfileRepository.flush();
    }
}

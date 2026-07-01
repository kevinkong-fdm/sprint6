package com.example.banking.customer.application;

import static org.mockito.Mockito.verify;

import com.example.banking.customer.domain.CustomerProfileEntity;
import com.example.banking.customer.infrastructure.CustomerProfileRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerCascadeDeleteCoordinatorTest {

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    private CustomerCascadeDeleteCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator = new CustomerCascadeDeleteCoordinator(customerProfileRepository);
    }

    @Test
    void shouldDeleteAndFlushProfile() {
        CustomerProfileEntity profile = CustomerProfileEntity.newProfileWithId(
                "cust-1",
                "alice@example.com",
                "alice@example.com",
                "Alice",
                "Doe",
                null,
                LocalDate.parse("1990-01-01"),
                null,
                "actor-1");

        coordinator.deleteCustomer(profile);

        verify(customerProfileRepository).delete(profile);
        verify(customerProfileRepository).flush();
    }
}

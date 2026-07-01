package com.example.banking.customer.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.customer.api.dto.CustomerResponse;
import com.example.banking.customer.api.dto.CustomerResponseMapper;
import com.example.banking.customer.domain.CustomerProfileEntity;
import com.example.banking.customer.domain.LifecycleAction;
import com.example.banking.customer.infrastructure.CustomerProfileRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCustomerServiceTest {

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @Mock
    private CustomerResponseMapper customerResponseMapper;

    @Mock
    private CustomerLifecycleEventService customerLifecycleEventService;

    private GetCustomerService service;

    @BeforeEach
    void setUp() {
        service = new GetCustomerService(
                customerProfileRepository,
                customerResponseMapper,
                customerLifecycleEventService);
    }

    @Test
    void shouldRecordFailureWhenCustomerMissing() {
        when(customerProfileRepository.findDetailedByCustomerId("cust-1")).thenReturn(Optional.empty());

        CustomerDomainException.GetNotFoundException ex = assertThrows(
                CustomerDomainException.GetNotFoundException.class,
                () -> service.getById("cust-1", "corr-1", " "));

        assertEquals("CUST-GET-001", ex.getErrorCode());
        verify(customerLifecycleEventService).recordFailure(
                eq("cust-1"),
                eq(LifecycleAction.GET),
                eq("operator"),
                eq("corr-1"),
                eq("CUST-GET-001"),
                eq("{}"));
    }

    @Test
    void shouldReturnMappedCustomerAndRecordSuccess() {
        CustomerProfileEntity profile = CustomerProfileEntity.newProfileWithId(
                "cust-1",
                "alice@example.com",
                "alice@example.com",
                "Alice",
                "Doe",
                "0411",
                LocalDate.parse("1990-01-01"),
                "en",
                "seed");

        CustomerResponse mapped = new CustomerResponse(
                "cust-1",
                "alice@example.com",
                "Alice",
                "Doe",
                "0411",
                LocalDate.parse("1990-01-01"),
                "en",
                List.of(),
                List.of(),
                profile.getCreatedAt(),
                profile.getUpdatedAt());

        when(customerProfileRepository.findDetailedByCustomerId("cust-1")).thenReturn(Optional.of(profile));
        when(customerResponseMapper.toResponse(profile)).thenReturn(mapped);

        CustomerResponse response = service.getById("cust-1", "corr-2", "actor-1");

        assertEquals("cust-1", response.customerId());
        verify(customerLifecycleEventService).recordSuccess(
                eq("cust-1"),
                eq(LifecycleAction.GET),
                eq("actor-1"),
                eq("corr-2"),
                eq("{}"));
    }
}

package com.example.banking.customer.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.customer.domain.CustomerLifecycleEventEntity;
import com.example.banking.customer.domain.LifecycleAction;
import com.example.banking.customer.infrastructure.CustomerLifecycleEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CustomerLifecycleEventServiceTest {

    @Mock
    private CustomerLifecycleEventRepository customerLifecycleEventRepository;

    private CustomerLifecycleEventService service;

    @BeforeEach
    void setUp() {
        service = new CustomerLifecycleEventService(customerLifecycleEventRepository);
    }

    @Test
    void shouldRecordSuccessWithGeneratedCorrelationWhenMissing() {
        when(customerLifecycleEventRepository.save(org.mockito.ArgumentMatchers.any(CustomerLifecycleEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, CustomerLifecycleEventEntity.class));

        service.recordSuccess("cust-1", LifecycleAction.GET, "actor-1", " ", null);

        ArgumentCaptor<CustomerLifecycleEventEntity> captor = ArgumentCaptor.forClass(CustomerLifecycleEventEntity.class);
        verify(customerLifecycleEventRepository).save(captor.capture());

        String correlationId = (String) ReflectionTestUtils.getField(captor.getValue(), "correlationId");
        assertFalse(correlationId.isBlank());
    }

    @Test
    void shouldRecordFailureWithProvidedCorrelation() {
        when(customerLifecycleEventRepository.save(org.mockito.ArgumentMatchers.any(CustomerLifecycleEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, CustomerLifecycleEventEntity.class));

        service.recordFailure("cust-1", LifecycleAction.UPDATE, "actor-1", "corr-1", "CUST-UPD-001", "{}");

        ArgumentCaptor<CustomerLifecycleEventEntity> captor = ArgumentCaptor.forClass(CustomerLifecycleEventEntity.class);
        verify(customerLifecycleEventRepository).save(captor.capture());

        assertEquals("corr-1", ReflectionTestUtils.getField(captor.getValue(), "correlationId"));
        assertEquals("CUST-UPD-001", ReflectionTestUtils.getField(captor.getValue(), "errorCode"));
    }
}

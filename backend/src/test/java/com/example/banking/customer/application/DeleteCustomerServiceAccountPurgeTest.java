package com.example.banking.customer.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.auth.application.DeleteAccountService;
import com.example.banking.customer.domain.CustomerProfileEntity;
import com.example.banking.customer.infrastructure.CustomerProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessResourceFailureException;

class DeleteCustomerServiceAccountPurgeTest {

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @Mock
    private CustomerLifecycleEventService customerLifecycleEventService;

    @Mock
    private DeleteAccountService deleteAccountService;

    private DeleteCustomerService deleteCustomerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deleteCustomerService = new DeleteCustomerService(
                customerProfileRepository,
                customerLifecycleEventService,
                deleteAccountService);
    }

    @Test
    void shouldPurgeAccountAndDeleteCustomerOnSuccess() {
        String customerId = "11111111-1111-1111-1111-111111111111";
        String email = "alice@example.com";
        CustomerProfileEntity profile = CustomerProfileEntity.newProfileWithId(
                customerId,
                email,
                CustomerEmailNormalizer.normalize(email),
                "Alice",
                "Example",
                null,
                null,
                null,
                "operator");

        when(customerProfileRepository.findDetailedByCustomerId(customerId)).thenReturn(Optional.of(profile));

        deleteCustomerService.delete(customerId, "corr-1", "actor-1");

        InOrder inOrder = inOrder(deleteAccountService, customerLifecycleEventService);
        inOrder.verify(deleteAccountService).deleteAccountWithAuthData(customerId, CustomerEmailNormalizer.normalize(email));
        inOrder.verify(customerLifecycleEventService).recordSuccess(eq(customerId), any(), any(), any(), any());
    }

    @Test
    void shouldThrowCascadeFailureWhenAccountPurgeFails() {
        String customerId = "11111111-1111-1111-1111-111111111111";
        String email = "alice@example.com";
        CustomerProfileEntity profile = CustomerProfileEntity.newProfileWithId(
                customerId,
                email,
                CustomerEmailNormalizer.normalize(email),
                "Alice",
                "Example",
                null,
                null,
                null,
                "operator");

        when(customerProfileRepository.findDetailedByCustomerId(customerId)).thenReturn(Optional.of(profile));
        doThrow(new DataAccessResourceFailureException("boom"))
                .when(deleteAccountService)
                .deleteAccountWithAuthData(customerId, CustomerEmailNormalizer.normalize(email));

        assertThrows(CustomerDomainException.CascadeDeleteFailureException.class,
                () -> deleteCustomerService.delete(customerId, "corr-1", "actor-1"));

        verify(customerLifecycleEventService).recordFailure(eq(customerId), any(), any(), any(), any(), any());
    }
}

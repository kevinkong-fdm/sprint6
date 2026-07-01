package com.example.banking.customer.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.customer.api.dto.AddressInput;
import com.example.banking.customer.api.dto.ContactPreferenceInput;
import com.example.banking.customer.api.dto.CustomerCreateRequest;
import com.example.banking.customer.api.dto.CustomerResponse;
import com.example.banking.customer.api.dto.CustomerResponseMapper;
import com.example.banking.customer.domain.AddressType;
import com.example.banking.customer.domain.ContactChannel;
import com.example.banking.customer.domain.CustomerProfileEntity;
import com.example.banking.customer.domain.LifecycleAction;
import com.example.banking.customer.infrastructure.CustomerProfileRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class CreateCustomerServiceTest {

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @Mock
    private CustomerResponseMapper customerResponseMapper;

    @Mock
    private CustomerLifecycleEventService customerLifecycleEventService;

    private CreateCustomerService service;

    @BeforeEach
    void setUp() {
        service = new CreateCustomerService(
                customerProfileRepository,
                customerResponseMapper,
                customerLifecycleEventService);
    }

    @Test
    void shouldRejectBlankNormalizedEmail() {
        CustomerCreateRequest request = request("   ");

        CustomerDomainException.CreateValidationException ex = assertThrows(
                CustomerDomainException.CreateValidationException.class,
                () -> service.create(request, "corr-1", "actor-1"));

        assertEquals("CUST-CRT-001", ex.getErrorCode());
    }

    @Test
    void shouldRejectDuplicateIdentity() {
        CustomerCreateRequest request = request("alice@example.com");
        when(customerProfileRepository.existsByEmailNormalized("alice@example.com")).thenReturn(true);

        CustomerDomainException.DuplicateIdentityException ex = assertThrows(
                CustomerDomainException.DuplicateIdentityException.class,
                () -> service.create(request, "corr-1", "actor-1"));

        assertEquals("CUST-CRT-002", ex.getErrorCode());
    }

    @Test
    void shouldRejectBlankCustomerIdForExplicitCreate() {
        CustomerCreateRequest request = request("alice@example.com");

        CustomerDomainException.CreateValidationException ex = assertThrows(
                CustomerDomainException.CreateValidationException.class,
                () -> service.createWithCustomerId("   ", request, "corr-1", "actor-1"));

        assertEquals("CUST-CRT-001", ex.getErrorCode());
    }

    @Test
    void shouldCreateProfileAndRecordSuccess() {
        CustomerCreateRequest request = request("alice@example.com");

        when(customerProfileRepository.existsByEmailNormalized("alice@example.com")).thenReturn(false);
        when(customerProfileRepository.save(any(CustomerProfileEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, CustomerProfileEntity.class));

        CustomerResponse mappedResponse = new CustomerResponse(
                "cust-1",
                "alice@example.com",
                "Alice",
                "Doe",
                "0412345678",
                LocalDate.parse("1990-01-01"),
                "en",
                List.of(),
                List.of(),
                null,
                null);
        when(customerResponseMapper.toResponse(any(CustomerProfileEntity.class))).thenReturn(mappedResponse);

        CustomerResponse response = service.create(request, "corr-1", "   ");

        assertEquals("cust-1", response.customerId());

        ArgumentCaptor<CustomerProfileEntity> profileCaptor = ArgumentCaptor.forClass(CustomerProfileEntity.class);
        verify(customerProfileRepository).save(profileCaptor.capture());
        CustomerProfileEntity saved = profileCaptor.getValue();
        assertEquals("alice@example.com", saved.getEmailNormalized());
        assertEquals(1, saved.getAddresses().size());
        assertEquals(1, saved.getContactPreferences().size());

        verify(customerLifecycleEventService).recordSuccess(
                eq(saved.getCustomerId()),
                eq(LifecycleAction.CREATE),
                eq("operator"),
                eq("corr-1"),
                eq("{}"));
    }

    @Test
    void shouldMapDataIntegrityViolationToDuplicateIdentity() {
        CustomerCreateRequest request = request("alice@example.com");
        when(customerProfileRepository.existsByEmailNormalized("alice@example.com")).thenReturn(false);
        when(customerProfileRepository.save(any(CustomerProfileEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        CustomerDomainException.DuplicateIdentityException ex = assertThrows(
                CustomerDomainException.DuplicateIdentityException.class,
                () -> service.createWithCustomerId("cust-1", request, "corr-1", "actor-1"));

        assertEquals("CUST-CRT-002", ex.getErrorCode());
        verify(customerLifecycleEventService).recordFailure(
                eq("cust-1"),
                eq(LifecycleAction.CREATE),
                eq("actor-1"),
                eq("corr-1"),
                eq("CUST-CRT-002"),
                eq("{}"));
    }

    private CustomerCreateRequest request(String email) {
        return new CustomerCreateRequest(
                email,
                "Alice",
                "Doe",
                "0412345678",
                LocalDate.parse("1990-01-01"),
                "en",
                List.of(new AddressInput(
                        AddressType.HOME,
                        " 1 Main St ",
                        " ",
                        " Sydney ",
                        " NSW ",
                        "2000",
                        "au")),
                List.of(new ContactPreferenceInput(ContactChannel.EMAIL, true)));
    }
}

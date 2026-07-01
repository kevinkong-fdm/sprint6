package com.example.banking.customer.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.customer.api.dto.AddressInput;
import com.example.banking.customer.api.dto.ContactPreferenceInput;
import com.example.banking.customer.api.dto.CustomerResponse;
import com.example.banking.customer.api.dto.CustomerResponseMapper;
import com.example.banking.customer.api.dto.CustomerUpdateRequest;
import com.example.banking.customer.domain.AddressType;
import com.example.banking.customer.domain.ContactChannel;
import com.example.banking.customer.domain.CustomerAddressEntity;
import com.example.banking.customer.domain.CustomerContactPreferenceEntity;
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
class UpdateCustomerServiceTest {

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @Mock
    private ImmutableCustomerFieldValidator immutableCustomerFieldValidator;

    @Mock
    private CustomerResponseMapper customerResponseMapper;

    @Mock
    private CustomerLifecycleEventService customerLifecycleEventService;

    private UpdateCustomerService service;

    @BeforeEach
    void setUp() {
        service = new UpdateCustomerService(
                customerProfileRepository,
                immutableCustomerFieldValidator,
                customerResponseMapper,
                customerLifecycleEventService);
    }

    @Test
    void shouldRejectWhenNoMutableFieldsProvided() {
        CustomerUpdateRequest request = new CustomerUpdateRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        CustomerDomainException.UpdateValidationException ex = assertThrows(
                CustomerDomainException.UpdateValidationException.class,
                () -> service.update("cust-1", request, "corr-1", "actor-1"));

        assertEquals("CUST-UPD-001", ex.getErrorCode());
        verify(immutableCustomerFieldValidator).validate(request);
    }

    @Test
    void shouldRecordFailureAndThrowWhenProfileMissing() {
        CustomerUpdateRequest request = new CustomerUpdateRequest(
                "Alice",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        when(customerProfileRepository.findDetailedByCustomerId("cust-1")).thenReturn(Optional.empty());

        CustomerDomainException.GetNotFoundException ex = assertThrows(
                CustomerDomainException.GetNotFoundException.class,
                () -> service.update("cust-1", request, "corr-2", " "));

        assertEquals("CUST-GET-001", ex.getErrorCode());
        verify(customerLifecycleEventService).recordFailure(
                eq("cust-1"),
                eq(LifecycleAction.UPDATE),
                eq("operator"),
                eq("corr-2"),
                eq("CUST-GET-001"),
                eq("{}"));
    }

    @Test
    void shouldUpdateProfileAndMapResponse() {
        CustomerProfileEntity profile = CustomerProfileEntity.newProfileWithId(
                "cust-1",
                "alice@example.com",
                "alice@example.com",
                "Alice",
                "Old",
                "0400000000",
                LocalDate.parse("1990-01-01"),
                "en",
                "seed");
        profile.addAddress(CustomerAddressEntity.of(AddressType.HOME, "old", null, "city", null, "2000", "AU"));
        profile.addContactPreference(CustomerContactPreferenceEntity.of(ContactChannel.SMS, false, "seed"));

        CustomerUpdateRequest request = new CustomerUpdateRequest(
                "Alicia",
                "Doe",
                "0411222333",
                "fr",
                List.of(new AddressInput(AddressType.MAILING, "1 New", null, "Sydney", null, "2000", "AU")),
                List.of(new ContactPreferenceInput(ContactChannel.EMAIL, true)),
                null,
                null,
                null);

        when(customerProfileRepository.findDetailedByCustomerId("cust-1")).thenReturn(Optional.of(profile));
        when(customerProfileRepository.save(any(CustomerProfileEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, CustomerProfileEntity.class));

        CustomerResponse mapped = new CustomerResponse(
                "cust-1",
                "alice@example.com",
                "Alicia",
                "Doe",
                "0411222333",
                LocalDate.parse("1990-01-01"),
                "fr",
                List.of(),
                List.of(),
                null,
                null);
        when(customerResponseMapper.toResponse(profile)).thenReturn(mapped);

        CustomerResponse response = service.update("cust-1", request, "corr-3", " ");

        assertEquals("Alicia", response.givenName());
        assertEquals("Doe", profile.getFamilyName());
        assertEquals("0411222333", profile.getPhoneNumber());
        assertEquals("fr", profile.getPreferredLanguage());
        assertEquals(1, profile.getAddresses().size());
        assertEquals(1, profile.getContactPreferences().size());

        verify(customerLifecycleEventService).recordSuccess(
                eq("cust-1"),
                eq(LifecycleAction.UPDATE),
                eq("operator"),
                eq("corr-3"),
                eq("{}"));
    }
}

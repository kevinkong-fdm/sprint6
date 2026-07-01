package com.example.banking.customer.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.banking.customer.domain.AddressType;
import com.example.banking.customer.domain.ContactChannel;
import com.example.banking.customer.domain.CustomerAddressEntity;
import com.example.banking.customer.domain.CustomerContactPreferenceEntity;
import com.example.banking.customer.domain.CustomerProfileEntity;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CustomerResponseMapperTest {

    private final CustomerResponseMapper mapper = new CustomerResponseMapper();

    @Test
    void shouldMapCustomerProfileWithNestedCollections() {
        CustomerProfileEntity profile = CustomerProfileEntity.newProfileWithId(
                "cust-1",
                "alice@example.com",
                "alice@example.com",
                "Alice",
                "Doe",
                "0411000000",
                LocalDate.parse("1990-01-01"),
                "en",
                "actor-1");

        profile.addAddress(CustomerAddressEntity.of(
                AddressType.HOME,
                "1 Main St",
                "Apt 2",
                "Sydney",
                "NSW",
                "2000",
                "au"));
        profile.addContactPreference(CustomerContactPreferenceEntity.of(
                ContactChannel.EMAIL,
                true,
                " actor-1 "));

        CustomerResponse response = mapper.toResponse(profile);

        assertEquals("cust-1", response.customerId());
        assertEquals("alice@example.com", response.email());
        assertEquals(1, response.addresses().size());
        assertEquals(1, response.contactPreferences().size());
        assertEquals(AddressType.HOME, response.addresses().get(0).addressType());
        assertEquals("AU", response.addresses().get(0).countryCode());
        assertEquals(ContactChannel.EMAIL, response.contactPreferences().get(0).channel());
        assertEquals(true, response.contactPreferences().get(0).optIn());
    }
}

package com.example.banking.customer.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.banking.customer.api.dto.CustomerUpdateRequest;
import org.junit.jupiter.api.Test;

class ImmutableCustomerFieldValidatorTest {

    private final ImmutableCustomerFieldValidator validator = new ImmutableCustomerFieldValidator();

    @Test
    void shouldAllowMutableOnlyRequest() {
        CustomerUpdateRequest request = new CustomerUpdateRequest(
                "Alice",
                "Doe",
                "0411",
                "en",
                null,
                null,
                null,
                null,
                null);

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void shouldRejectImmutableFieldChanges() {
        CustomerUpdateRequest request = new CustomerUpdateRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                "alice@example.com",
                java.time.LocalDate.parse("1990-01-01"),
                "cust-1");

        assertThrows(CustomerDomainException.ImmutableFieldUpdateException.class, () -> validator.validate(request));
    }
}

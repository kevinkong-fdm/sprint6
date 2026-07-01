package com.example.banking.customer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CustomerAddressEntityTest {

    @Test
    void shouldNormalizeAddressFields() {
        CustomerAddressEntity entity = CustomerAddressEntity.of(
                AddressType.HOME,
                " 1 Main St ",
                "   ",
                " Sydney ",
                " ",
                "2000",
                "au");

        assertNotNull(entity.getAddressId());
        assertEquals(AddressType.HOME, entity.getAddressType());
        assertEquals("1 Main St", entity.getLine1());
        assertNull(entity.getLine2());
        assertEquals("Sydney", entity.getCity());
        assertNull(entity.getRegion());
        assertEquals("2000", entity.getPostalCode());
        assertEquals("AU", entity.getCountryCode());
    }
}

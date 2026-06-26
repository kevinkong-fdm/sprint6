package com.example.banking.customer.api.dto;

import com.example.banking.customer.domain.AddressType;

public record AddressOutput(
        String addressId,
        AddressType addressType,
        String line1,
        String line2,
        String city,
        String region,
        String postalCode,
        String countryCode
) {
}

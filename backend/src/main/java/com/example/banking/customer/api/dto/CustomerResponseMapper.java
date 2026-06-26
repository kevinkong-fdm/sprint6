package com.example.banking.customer.api.dto;

import com.example.banking.customer.domain.CustomerAddressEntity;
import com.example.banking.customer.domain.CustomerContactPreferenceEntity;
import com.example.banking.customer.domain.CustomerProfileEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CustomerResponseMapper {

    public CustomerResponse toResponse(CustomerProfileEntity entity) {
        List<AddressOutput> addresses = entity.getAddresses().stream()
                .map(this::toAddressOutput)
                .toList();

        List<ContactPreferenceOutput> contactPreferences = entity.getContactPreferences().stream()
                .map(this::toContactPreferenceOutput)
                .toList();

        return new CustomerResponse(
                entity.getCustomerId(),
                entity.getEmail(),
                entity.getGivenName(),
                entity.getFamilyName(),
                entity.getPhoneNumber(),
                entity.getDateOfBirth(),
                entity.getPreferredLanguage(),
                addresses,
                contactPreferences,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private AddressOutput toAddressOutput(CustomerAddressEntity entity) {
        return new AddressOutput(
                entity.getAddressId(),
                entity.getAddressType(),
                entity.getLine1(),
                entity.getLine2(),
                entity.getCity(),
                entity.getRegion(),
                entity.getPostalCode(),
                entity.getCountryCode());
    }

    private ContactPreferenceOutput toContactPreferenceOutput(CustomerContactPreferenceEntity entity) {
        return new ContactPreferenceOutput(
                entity.getPreferenceId(),
                entity.getChannel(),
                entity.isOptIn());
    }
}

package com.example.banking.customer.application;

import com.example.banking.customer.api.dto.AddressInput;
import com.example.banking.customer.api.dto.ContactPreferenceInput;
import com.example.banking.customer.api.dto.CustomerResponse;
import com.example.banking.customer.api.dto.CustomerResponseMapper;
import com.example.banking.customer.api.dto.CustomerUpdateRequest;
import com.example.banking.customer.domain.CustomerAddressEntity;
import com.example.banking.customer.domain.CustomerContactPreferenceEntity;
import com.example.banking.customer.domain.CustomerProfileEntity;
import com.example.banking.customer.domain.LifecycleAction;
import com.example.banking.customer.infrastructure.CustomerProfileRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCustomerService {

    private final CustomerProfileRepository customerProfileRepository;
    private final ImmutableCustomerFieldValidator immutableCustomerFieldValidator;
    private final CustomerResponseMapper customerResponseMapper;
    private final CustomerLifecycleEventService customerLifecycleEventService;

    public UpdateCustomerService(
            CustomerProfileRepository customerProfileRepository,
            ImmutableCustomerFieldValidator immutableCustomerFieldValidator,
            CustomerResponseMapper customerResponseMapper,
            CustomerLifecycleEventService customerLifecycleEventService
    ) {
        this.customerProfileRepository = customerProfileRepository;
        this.immutableCustomerFieldValidator = immutableCustomerFieldValidator;
        this.customerResponseMapper = customerResponseMapper;
        this.customerLifecycleEventService = customerLifecycleEventService;
    }

    @Transactional
    public CustomerResponse update(String customerId, CustomerUpdateRequest request, String correlationId, String actorId) {
        immutableCustomerFieldValidator.validate(request);

        if (!hasAnyUpdates(request)) {
            throw new CustomerDomainException.UpdateValidationException("Customer update validation failed.");
        }

        CustomerProfileEntity profile = customerProfileRepository.findDetailedByCustomerId(customerId)
                .orElseThrow(() -> {
                    customerLifecycleEventService.recordFailure(
                            customerId,
                            LifecycleAction.UPDATE,
                            effectiveActor(actorId),
                            correlationId,
                            "CUST-GET-001",
                            "{}");
                    return new CustomerDomainException.GetNotFoundException("Customer not found.");
                });

        profile.applyMutableUpdates(
                request.givenName(),
                request.familyName(),
                request.phoneNumber(),
                request.preferredLanguage(),
                effectiveActor(actorId));

        if (request.addresses() != null) {
            profile.replaceAddresses(mapAddresses(request.addresses()));
        }
        if (request.contactPreferences() != null) {
            profile.replaceContactPreferences(mapPreferences(request.contactPreferences(), effectiveActor(actorId)));
        }

        CustomerProfileEntity saved = customerProfileRepository.save(profile);
        customerLifecycleEventService.recordSuccess(
                customerId,
                LifecycleAction.UPDATE,
                effectiveActor(actorId),
                correlationId,
                "{}");
        return customerResponseMapper.toResponse(saved);
    }

    private boolean hasAnyUpdates(CustomerUpdateRequest request) {
        return request.givenName() != null
                || request.familyName() != null
                || request.phoneNumber() != null
                || request.preferredLanguage() != null
                || request.addresses() != null
                || request.contactPreferences() != null;
    }

    private List<CustomerAddressEntity> mapAddresses(List<AddressInput> addresses) {
        return addresses.stream()
                .map(address -> CustomerAddressEntity.of(
                        address.addressType(),
                        address.line1(),
                        address.line2(),
                        address.city(),
                        address.region(),
                        address.postalCode(),
                        address.countryCode()))
                .toList();
    }

    private List<CustomerContactPreferenceEntity> mapPreferences(List<ContactPreferenceInput> preferences, String actorId) {
        return preferences.stream()
                .map(preference -> CustomerContactPreferenceEntity.of(
                        preference.channel(),
                        Boolean.TRUE.equals(preference.optIn()),
                        actorId))
                .toList();
    }

    private String effectiveActor(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            return "operator";
        }
        return actorId;
    }
}

package com.example.banking.customer.application;

import com.example.banking.customer.api.dto.AddressInput;
import com.example.banking.customer.api.dto.ContactPreferenceInput;
import com.example.banking.customer.api.dto.CustomerCreateRequest;
import com.example.banking.customer.api.dto.CustomerResponse;
import com.example.banking.customer.api.dto.CustomerResponseMapper;
import com.example.banking.customer.domain.CustomerAddressEntity;
import com.example.banking.customer.domain.CustomerContactPreferenceEntity;
import com.example.banking.customer.domain.CustomerProfileEntity;
import com.example.banking.customer.domain.LifecycleAction;
import com.example.banking.customer.infrastructure.CustomerProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCustomerService {

    private final CustomerProfileRepository customerProfileRepository;
    private final CustomerResponseMapper customerResponseMapper;
    private final CustomerLifecycleEventService customerLifecycleEventService;

    public CreateCustomerService(
            CustomerProfileRepository customerProfileRepository,
            CustomerResponseMapper customerResponseMapper,
            CustomerLifecycleEventService customerLifecycleEventService
    ) {
        this.customerProfileRepository = customerProfileRepository;
        this.customerResponseMapper = customerResponseMapper;
        this.customerLifecycleEventService = customerLifecycleEventService;
    }

    @Transactional
    public CustomerResponse create(CustomerCreateRequest request, String correlationId, String actorId) {
        return createInternal(null, request, correlationId, actorId);
    }

    @Transactional
    public CustomerResponse createWithCustomerId(
            String customerId,
            CustomerCreateRequest request,
            String correlationId,
            String actorId
    ) {
        if (customerId == null || customerId.isBlank()) {
            throw new CustomerDomainException.CreateValidationException("Customer creation validation failed.");
        }
        return createInternal(customerId.trim(), request, correlationId, actorId);
    }

    private CustomerResponse createInternal(
            String customerId,
            CustomerCreateRequest request,
            String correlationId,
            String actorId
    ) {
        String normalizedEmail = CustomerEmailNormalizer.normalize(request.email());
        if (normalizedEmail.isBlank()) {
            throw new CustomerDomainException.CreateValidationException("Customer creation validation failed.");
        }

        if (customerProfileRepository.existsByEmailNormalized(normalizedEmail)) {
            throw new CustomerDomainException.DuplicateIdentityException("Duplicate customer identity attribute.");
        }

        try {
            CustomerProfileEntity profile = CustomerProfileEntity.newProfileWithId(
                    customerId,
                    request.email(),
                    normalizedEmail,
                    request.givenName(),
                    request.familyName(),
                    request.phoneNumber(),
                    request.dateOfBirth(),
                    request.preferredLanguage(),
                    effectiveActor(actorId));

            if (request.addresses() != null) {
                for (AddressInput address : request.addresses()) {
                    profile.addAddress(CustomerAddressEntity.of(
                            address.addressType(),
                            address.line1(),
                            address.line2(),
                            address.city(),
                            address.region(),
                            address.postalCode(),
                            address.countryCode()));
                }
            }

            if (request.contactPreferences() != null) {
                for (ContactPreferenceInput preference : request.contactPreferences()) {
                    profile.addContactPreference(CustomerContactPreferenceEntity.of(
                            preference.channel(),
                            Boolean.TRUE.equals(preference.optIn()),
                            effectiveActor(actorId)));
                }
            }

            CustomerProfileEntity saved = customerProfileRepository.save(profile);
            customerLifecycleEventService.recordSuccess(
                    saved.getCustomerId(),
                    LifecycleAction.CREATE,
                    effectiveActor(actorId),
                    correlationId,
                    "{}");
            return customerResponseMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            customerLifecycleEventService.recordFailure(
                    customerId,
                    LifecycleAction.CREATE,
                    effectiveActor(actorId),
                    correlationId,
                    "CUST-CRT-002",
                    "{}");
            throw new CustomerDomainException.DuplicateIdentityException("Duplicate customer identity attribute.");
        }
    }

    private String effectiveActor(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            return "operator";
        }
        return actorId;
    }
}

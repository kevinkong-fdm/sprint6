package com.example.banking.customer.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_profile")
public class CustomerProfileEntity {

    @Id
    @Column(name = "customer_id", nullable = false, columnDefinition = "CHAR(36)")
    private String customerId;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "email_normalized", nullable = false, unique = true, length = 254)
    private String emailNormalized;

    @Column(name = "given_name", nullable = false, length = 100)
    private String givenName;

    @Column(name = "family_name", nullable = false, length = 100)
    private String familyName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "preferred_language", length = 20)
    private String preferredLanguage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 128)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", nullable = false, length = 128)
    private String updatedBy;

    @OneToMany(mappedBy = "customerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerAddressEntity> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "customerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerContactPreferenceEntity> contactPreferences = new ArrayList<>();

    public static CustomerProfileEntity newProfile(
            String email,
            String emailNormalized,
            String givenName,
            String familyName,
            String phoneNumber,
            LocalDate dateOfBirth,
            String preferredLanguage,
            String actorId
    ) {
        return newProfileWithId(
                null,
                email,
                emailNormalized,
                givenName,
                familyName,
                phoneNumber,
                dateOfBirth,
                preferredLanguage,
                actorId);
    }

    public static CustomerProfileEntity newProfileWithId(
            String customerId,
            String email,
            String emailNormalized,
            String givenName,
            String familyName,
            String phoneNumber,
            LocalDate dateOfBirth,
            String preferredLanguage,
            String actorId
    ) {
        Instant now = Instant.now();
        CustomerProfileEntity entity = new CustomerProfileEntity();
        entity.customerId = normalizeCustomerId(customerId);
        entity.email = normalizeRequired(email);
        entity.emailNormalized = normalizeRequired(emailNormalized);
        entity.givenName = normalizeRequired(givenName);
        entity.familyName = normalizeRequired(familyName);
        entity.phoneNumber = normalizeNullable(phoneNumber);
        entity.dateOfBirth = dateOfBirth;
        entity.preferredLanguage = normalizeNullable(preferredLanguage);
        entity.createdAt = now;
        entity.createdBy = normalizeRequired(actorId);
        entity.updatedAt = now;
        entity.updatedBy = normalizeRequired(actorId);
        return entity;
    }

    public void applyMutableUpdates(
            String givenName,
            String familyName,
            String phoneNumber,
            String preferredLanguage,
            String actorId
    ) {
        boolean changed = false;

        if (givenName != null) {
            this.givenName = normalizeRequired(givenName);
            changed = true;
        }
        if (familyName != null) {
            this.familyName = normalizeRequired(familyName);
            changed = true;
        }
        if (phoneNumber != null) {
            this.phoneNumber = normalizeNullable(phoneNumber);
            changed = true;
        }
        if (preferredLanguage != null) {
            this.preferredLanguage = normalizeNullable(preferredLanguage);
            changed = true;
        }

        if (changed) {
            this.updatedAt = Instant.now();
            this.updatedBy = normalizeRequired(actorId);
        }
    }

    public void replaceAddresses(List<CustomerAddressEntity> newAddresses) {
        this.addresses.clear();
        if (newAddresses == null) {
            return;
        }
        for (CustomerAddressEntity address : newAddresses) {
            addAddress(address);
        }
    }

    public void addAddress(CustomerAddressEntity address) {
        address.setCustomerProfile(this);
        this.addresses.add(address);
    }

    public void replaceContactPreferences(List<CustomerContactPreferenceEntity> newPreferences) {
        this.contactPreferences.clear();
        if (newPreferences == null) {
            return;
        }
        for (CustomerContactPreferenceEntity preference : newPreferences) {
            addContactPreference(preference);
        }
    }

    public void addContactPreference(CustomerContactPreferenceEntity preference) {
        preference.setCustomerProfile(this);
        this.contactPreferences.add(preference);
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailNormalized() {
        return emailNormalized;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public List<CustomerAddressEntity> getAddresses() {
        return List.copyOf(addresses);
    }

    public List<CustomerContactPreferenceEntity> getContactPreferences() {
        return List.copyOf(contactPreferences);
    }

    private static String normalizeRequired(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeCustomerId(String customerId) {
        if (customerId == null) {
            return UUID.randomUUID().toString();
        }

        String normalized = customerId.trim();
        return normalized.isEmpty() ? UUID.randomUUID().toString() : normalized;
    }
}

package com.example.banking.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_address")
public class CustomerAddressEntity {

    @Id
    @Column(name = "address_id", nullable = false, columnDefinition = "CHAR(36)")
    private String addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfileEntity customerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 16)
    private AddressType addressType;

    @Column(name = "line1", nullable = false, length = 200)
    private String line1;

    @Column(name = "line2", length = 200)
    private String line2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static CustomerAddressEntity of(
            AddressType addressType,
            String line1,
            String line2,
            String city,
            String region,
            String postalCode,
            String countryCode
    ) {
        Instant now = Instant.now();
        CustomerAddressEntity entity = new CustomerAddressEntity();
        entity.addressId = UUID.randomUUID().toString();
        entity.addressType = addressType;
        entity.line1 = normalizeRequired(line1);
        entity.line2 = normalizeNullable(line2);
        entity.city = normalizeRequired(city);
        entity.region = normalizeNullable(region);
        entity.postalCode = normalizeRequired(postalCode);
        entity.countryCode = normalizeRequired(countryCode).toUpperCase();
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    void setCustomerProfile(CustomerProfileEntity customerProfile) {
        this.customerProfile = customerProfile;
    }

    public String getAddressId() {
        return addressId;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountryCode() {
        return countryCode;
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
}

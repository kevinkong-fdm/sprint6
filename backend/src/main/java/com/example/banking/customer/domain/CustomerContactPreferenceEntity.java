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
@Table(name = "customer_contact_preference")
public class CustomerContactPreferenceEntity {

    @Id
    @Column(name = "preference_id", nullable = false, columnDefinition = "CHAR(36)")
    private String preferenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfileEntity customerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 16)
    private ContactChannel channel;

    @Column(name = "opt_in", nullable = false)
    private boolean optIn;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", nullable = false, length = 128)
    private String updatedBy;

    public static CustomerContactPreferenceEntity of(ContactChannel channel, boolean optIn, String actorId) {
        CustomerContactPreferenceEntity entity = new CustomerContactPreferenceEntity();
        entity.preferenceId = UUID.randomUUID().toString();
        entity.channel = channel;
        entity.optIn = optIn;
        entity.updatedAt = Instant.now();
        entity.updatedBy = normalizeRequired(actorId);
        return entity;
    }

    void setCustomerProfile(CustomerProfileEntity customerProfile) {
        this.customerProfile = customerProfile;
    }

    public String getPreferenceId() {
        return preferenceId;
    }

    public ContactChannel getChannel() {
        return channel;
    }

    public boolean isOptIn() {
        return optIn;
    }

    private static String normalizeRequired(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}

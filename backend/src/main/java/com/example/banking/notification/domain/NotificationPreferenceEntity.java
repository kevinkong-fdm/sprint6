package com.example.banking.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notification_preference")
public class NotificationPreferenceEntity {

    @Id
    @Column(name = "customer_id", nullable = false, columnDefinition = "CHAR(36)")
    private String customerId;

    @Column(name = "standing_order_notifications_enabled", nullable = false)
    private boolean standingOrderNotificationsEnabled;

    @Column(name = "managed_by_system", nullable = false)
    private boolean managedBySystem;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static NotificationPreferenceEntity systemDefault(String customerId) {
        Instant now = Instant.now();

        NotificationPreferenceEntity entity = new NotificationPreferenceEntity();
        entity.customerId = customerId == null ? "" : customerId.trim();
        entity.standingOrderNotificationsEnabled = true;
        entity.managedBySystem = true;
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    public String getCustomerId() {
        return customerId;
    }

    public boolean isStandingOrderNotificationsEnabled() {
        return standingOrderNotificationsEnabled;
    }

    public boolean isManagedBySystem() {
        return managedBySystem;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

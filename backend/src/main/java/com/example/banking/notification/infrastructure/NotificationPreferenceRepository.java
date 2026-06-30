package com.example.banking.notification.infrastructure;

import com.example.banking.notification.domain.NotificationPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreferenceEntity, String> {
}

package com.example.banking.notification.infrastructure;

import com.example.banking.notification.domain.NotificationEventEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationEventRepository
        extends JpaRepository<NotificationEventEntity, String>, JpaSpecificationExecutor<NotificationEventEntity> {

        Optional<NotificationEventEntity> findByDedupeKey(String dedupeKey);
}

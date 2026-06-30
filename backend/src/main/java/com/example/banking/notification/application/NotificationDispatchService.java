package com.example.banking.notification.application;

import com.example.banking.notification.domain.NotificationEventEntity;
import com.example.banking.notification.infrastructure.NotificationEventRepository;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatchService {

    private static final int MAX_ATTEMPTS = 3;

    private final NotificationEventRepository notificationEventRepository;

    public NotificationDispatchService(NotificationEventRepository notificationEventRepository) {
        this.notificationEventRepository = notificationEventRepository;
    }

    public NotificationEventEntity dispatch(NotificationEventEntity event) {
        NotificationEventEntity persisted = notificationEventRepository.save(event);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            if (isDispatchable(persisted)) {
                persisted.markSent();
                return notificationEventRepository.save(persisted);
            }

            persisted.markFailed();
            persisted = notificationEventRepository.save(persisted);
        }

        throw new StandingOrderDomainException.NotificationDispatchFailedException();
    }

    private boolean isDispatchable(NotificationEventEntity event) {
        return event.getTitle() != null
                && !event.getTitle().isBlank()
                && event.getMessage() != null
                && !event.getMessage().isBlank();
    }
}

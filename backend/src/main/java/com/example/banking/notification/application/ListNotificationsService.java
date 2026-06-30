package com.example.banking.notification.application;

import com.example.banking.notification.api.dto.NotificationEventResponse;
import com.example.banking.notification.api.dto.NotificationListData;
import com.example.banking.notification.api.dto.NotificationListResponse;
import com.example.banking.notification.domain.NotificationDispatchStatus;
import com.example.banking.notification.domain.NotificationEventEntity;
import com.example.banking.notification.domain.NotificationEventType;
import com.example.banking.notification.infrastructure.NotificationEventRepository;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ListNotificationsService {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final NotificationEventRepository notificationEventRepository;

    public ListNotificationsService(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            NotificationEventRepository notificationEventRepository
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.notificationEventRepository = notificationEventRepository;
    }

    public NotificationListResponse list(
            String actorId,
            String eventType,
            String dispatchStatus,
            int page,
            int size,
            String correlationId
    ) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);

        NotificationEventType resolvedType = parseEventType(eventType);
        NotificationDispatchStatus resolvedStatus = parseDispatchStatus(dispatchStatus);

        int resolvedPage = Math.max(1, page);
        int resolvedSize = Math.min(200, Math.max(1, size));
        PageRequest pageRequest = PageRequest.of(
                resolvedPage - 1,
                resolvedSize,
                Sort.by(Sort.Direction.DESC, "createdAt", "notificationEventId"));

        Specification<NotificationEventEntity> specification = byCustomerId(resolvedActorId);
        if (resolvedType != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("eventType"), resolvedType));
        }
        if (resolvedStatus != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("dispatchStatus"), resolvedStatus));
        }

        Page<NotificationEventEntity> result = notificationEventRepository.findAll(specification, pageRequest);
        List<NotificationEventResponse> items = new ArrayList<>();
        for (NotificationEventEntity entity : result.getContent()) {
            items.add(new NotificationEventResponse(
                    entity.getNotificationEventId(),
                    entity.getStandingOrderId(),
                    entity.getStandingOrderExecutionId(),
                    entity.getEventType().name(),
                    entity.getTitle(),
                    entity.getMessage(),
                    entity.getDispatchStatus().name(),
                    entity.getDispatchAttemptCount(),
                    entity.getCreatedAt(),
                    entity.getDispatchedAt()));
        }

        return new NotificationListResponse(
                correlationId,
                Instant.now(),
                new NotificationListData(items, resolvedPage, resolvedSize, result.getTotalElements()));
    }

    private Specification<NotificationEventEntity> byCustomerId(String customerId) {
        return (root, query, cb) -> cb.equal(root.get("customerId"), customerId);
    }

    private NotificationEventType parseEventType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return NotificationEventType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new StandingOrderDomainException.CreateValidationException("Invalid notification event type.");
        }
    }

    private NotificationDispatchStatus parseDispatchStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return NotificationDispatchStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new StandingOrderDomainException.CreateValidationException("Invalid notification dispatch status.");
        }
    }
}

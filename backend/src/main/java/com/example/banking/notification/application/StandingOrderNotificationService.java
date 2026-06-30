package com.example.banking.notification.application;

import com.example.banking.notification.domain.NotificationEventEntity;
import com.example.banking.notification.domain.NotificationEventType;
import com.example.banking.notification.domain.NotificationPreferenceEntity;
import com.example.banking.notification.infrastructure.NotificationEventRepository;
import com.example.banking.notification.infrastructure.NotificationPreferenceRepository;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionOutcome;
import org.springframework.stereotype.Service;

@Service
public class StandingOrderNotificationService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationEventRepository notificationEventRepository;
    private final NotificationDispatchService notificationDispatchService;

    public StandingOrderNotificationService(
            NotificationPreferenceRepository notificationPreferenceRepository,
            NotificationEventRepository notificationEventRepository,
            NotificationDispatchService notificationDispatchService
    ) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.notificationEventRepository = notificationEventRepository;
        this.notificationDispatchService = notificationDispatchService;
    }

    public void publishLifecycleUpdate(StandingOrderEntity standingOrder, String action, String correlationId) {
        NotificationPreferenceEntity preference = resolvePreference(standingOrder.getCustomerId());
        if (!preference.isStandingOrderNotificationsEnabled()) {
            return;
        }

        String dedupeKey = buildLifecycleDedupeKey(standingOrder.getStandingOrderId(), action, correlationId);
        if (notificationEventRepository.findByDedupeKey(dedupeKey).isPresent()) {
            return;
        }

        NotificationEventEntity event = NotificationEventEntity.pending(
                standingOrder.getCustomerId(),
                standingOrder.getStandingOrderId(),
                null,
                NotificationEventType.LIFECYCLE_UPDATED,
                "Standing order updated",
                buildLifecycleMessage(standingOrder, action),
                dedupeKey,
                correlationId);

        notificationDispatchService.dispatch(event);
    }

    public void publishExecutionOutcome(
            StandingOrderEntity standingOrder,
            StandingOrderExecutionEntity execution,
            String correlationId
    ) {
        NotificationPreferenceEntity preference = resolvePreference(standingOrder.getCustomerId());
        if (!preference.isStandingOrderNotificationsEnabled()) {
            return;
        }

        NotificationEventType eventType = toEventType(execution.getOutcome());
        String dedupeKey = execution.getStandingOrderExecutionId() + ":" + eventType.name();
        if (notificationEventRepository.findByDedupeKey(dedupeKey).isPresent()) {
            return;
        }

        String title = switch (eventType) {
            case EXECUTION_SUCCESS -> "Standing order executed";
            case EXECUTION_FAILURE -> "Standing order failed";
            case EXECUTION_SKIPPED -> "Standing order skipped";
            case LIFECYCLE_UPDATED -> "Standing order updated";
        };

        String message = switch (eventType) {
            case EXECUTION_SUCCESS -> buildExecutionMessage("succeeded", standingOrder, execution);
            case EXECUTION_FAILURE -> buildExecutionMessage("failed", standingOrder, execution);
            case EXECUTION_SKIPPED -> buildExecutionMessage("skipped", standingOrder, execution);
            case LIFECYCLE_UPDATED -> buildLifecycleMessage(standingOrder, "UPDATE");
        };

        NotificationEventEntity event = NotificationEventEntity.pending(
                standingOrder.getCustomerId(),
                standingOrder.getStandingOrderId(),
                execution.getStandingOrderExecutionId(),
                eventType,
                title,
                message,
                dedupeKey,
                correlationId);

        notificationDispatchService.dispatch(event);
    }

    private NotificationPreferenceEntity resolvePreference(String customerId) {
        return notificationPreferenceRepository.findById(customerId)
                .orElseGet(() -> notificationPreferenceRepository.save(NotificationPreferenceEntity.systemDefault(customerId)));
    }

    private NotificationEventType toEventType(StandingOrderExecutionOutcome outcome) {
        return switch (outcome) {
            case SUCCESS -> NotificationEventType.EXECUTION_SUCCESS;
            case FAILED -> NotificationEventType.EXECUTION_FAILURE;
            case SKIPPED -> NotificationEventType.EXECUTION_SKIPPED;
        };
    }

    private String buildLifecycleDedupeKey(String standingOrderId, String action, String correlationId) {
        String normalizedAction = action == null || action.isBlank() ? "UPDATE" : action.trim();
        String normalizedCorrelation = correlationId == null || correlationId.isBlank()
                ? String.valueOf(System.currentTimeMillis())
                : correlationId.trim();
        return standingOrderId + ":LIFECYCLE:" + normalizedAction + ":" + normalizedCorrelation;
    }

    private String buildLifecycleMessage(StandingOrderEntity standingOrder, String action) {
        return "Standing order " + standingOrder.getStandingOrderId()
                + " (" + standingOrder.getSourceAccountId() + " -> " + standingOrder.getDestinationAccountId() + ")"
                + " is now " + standingOrder.getStatus().name()
                + " via action " + action + ".";
    }

    private String buildExecutionMessage(
            String outcome,
            StandingOrderEntity standingOrder,
            StandingOrderExecutionEntity execution
    ) {
        String reasonCode = execution.getFailureReasonCode() == null || execution.getFailureReasonCode().isBlank()
                ? "N/A"
                : execution.getFailureReasonCode();
        String transferReference = execution.getTransferReferenceId() == null || execution.getTransferReferenceId().isBlank()
                ? "N/A"
                : execution.getTransferReferenceId();

        return "Execution " + execution.getStandingOrderExecutionId()
                + " " + outcome
                + " for standing order " + standingOrder.getStandingOrderId()
                + " (" + standingOrder.getSourceAccountId() + " -> " + standingOrder.getDestinationAccountId() + ")"
                + ". Amount=" + standingOrder.getAmount().toPlainString()
                + ", reasonCode=" + reasonCode
                + ", transferReference=" + transferReference
                + ", scheduledFor=" + execution.getScheduledFor() + ".";
    }
}

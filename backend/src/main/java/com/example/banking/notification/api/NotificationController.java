package com.example.banking.notification.api;

import com.example.banking.notification.api.dto.NotificationListResponse;
import com.example.banking.notification.api.dto.NotificationPreferenceUpdateRequest;
import com.example.banking.notification.application.ListNotificationsService;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final ListNotificationsService listNotificationsService;

    public NotificationController(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            ListNotificationsService listNotificationsService
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.listNotificationsService = listNotificationsService;
    }

    @GetMapping("/standing-orders")
    public NotificationListResponse listStandingOrderNotifications(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String dispatchStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        return listNotificationsService.list(actorId, eventType, dispatchStatus, page, size, correlationId);
    }

    @PatchMapping("/preferences")
    public void updateNotificationPreferences(
            @Valid @RequestBody NotificationPreferenceUpdateRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        throw new StandingOrderDomainException.NotificationPreferenceUnsupportedException();
    }
}

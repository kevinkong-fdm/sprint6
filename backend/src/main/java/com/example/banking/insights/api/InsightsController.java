package com.example.banking.insights.api;

import com.example.banking.insights.api.dto.SpendingInsightsSingleResponse;
import com.example.banking.insights.application.GetSpendingInsightsService;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/insights/spending")
public class InsightsController {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final GetSpendingInsightsService getSpendingInsightsService;

    public InsightsController(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            GetSpendingInsightsService getSpendingInsightsService
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.getSpendingInsightsService = getSpendingInsightsService;
    }

    @GetMapping
    public SpendingInsightsSingleResponse getSpendingInsights(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @RequestParam(defaultValue = "PREVIOUS_PERIOD") String comparisonMode,
            @RequestParam(required = false) String accountId,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");

        return getSpendingInsightsService.getInsights(
                periodStart,
                periodEnd,
                comparisonMode,
                accountId,
                actorId,
                correlationId);
    }
}

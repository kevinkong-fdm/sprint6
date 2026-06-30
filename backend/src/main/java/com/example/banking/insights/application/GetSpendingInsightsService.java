package com.example.banking.insights.application;

import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import com.example.banking.insights.api.dto.SpendingInsightsCategoryResponse;
import com.example.banking.insights.api.dto.SpendingInsightsDataResponse;
import com.example.banking.insights.api.dto.SpendingInsightsSingleResponse;
import com.example.banking.insights.api.dto.SpendingInsightsTotalsResponse;
import com.example.banking.insights.domain.ComparisonMode;
import com.example.banking.standingorder.application.PlatformTimezoneService;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetSpendingInsightsService {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final BankAccountRepository bankAccountRepository;
    private final PlatformTimezoneService platformTimezoneService;
    private final InsightsAggregationService insightsAggregationService;
    private final InsightsAuditService insightsAuditService;

    public GetSpendingInsightsService(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            BankAccountRepository bankAccountRepository,
            PlatformTimezoneService platformTimezoneService,
            InsightsAggregationService insightsAggregationService,
            InsightsAuditService insightsAuditService
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.bankAccountRepository = bankAccountRepository;
        this.platformTimezoneService = platformTimezoneService;
        this.insightsAggregationService = insightsAggregationService;
        this.insightsAuditService = insightsAuditService;
    }

    public SpendingInsightsSingleResponse getInsights(
            LocalDate periodStart,
            LocalDate periodEnd,
            String comparisonMode,
            String accountId,
            String actorId,
            String correlationId
    ) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);

        if (periodStart == null || periodEnd == null || periodStart.isAfter(periodEnd)) {
            throw new StandingOrderDomainException.InsightsValidationException();
        }

        ComparisonMode mode = ComparisonMode.from(comparisonMode);
        if (mode == null) {
            throw new StandingOrderDomainException.InsightsValidationException();
        }

        List<String> accountIds = resolveAccountIds(resolvedActorId, accountId);
        String normalizedAccountId = accountId == null || accountId.isBlank() ? null : accountId.trim();

        InsightsAggregationService.InsightsAggregationResult result = insightsAggregationService.aggregateAndPersist(
                resolvedActorId,
                normalizedAccountId,
                accountIds,
                periodStart,
                periodEnd,
                mode,
                correlationId,
                platformTimezoneService.zoneId());

        List<SpendingInsightsCategoryResponse> categories = new ArrayList<>();
        for (InsightsAggregationService.CategoryAggregation category : result.categories()) {
            categories.add(new SpendingInsightsCategoryResponse(
                    category.category(),
                    asMoney(category.currentTotal()),
                    asMoneyOrNull(category.previousTotal()),
                    asMoneyOrNull(category.deltaAmount()),
                    toDouble(category.deltaPercent())));
        }

        SpendingInsightsTotalsResponse totals = new SpendingInsightsTotalsResponse(
                asMoney(result.totals().currentTotal()),
                asMoneyOrNull(result.totals().previousTotal()),
                asMoneyOrNull(result.totals().deltaAmount()),
                toDouble(result.totals().deltaPercent()));

        SpendingInsightsDataResponse data = new SpendingInsightsDataResponse(
                periodStart,
                periodEnd,
                mode.name(),
                result.snapshot().isInsufficientData(),
                result.snapshot().getInsufficiencyReason().name(),
                categories,
                totals);

        insightsAuditService.auditRequest(
                resolvedActorId,
                normalizedAccountId,
                periodStart,
                periodEnd,
                mode.name(),
                correlationId,
                result.snapshot().isInsufficientData());

        return new SpendingInsightsSingleResponse(correlationId, Instant.now(), data);
    }

    private List<String> resolveAccountIds(String actorId, String accountId) {
        if (accountId != null && !accountId.isBlank()) {
            BankAccountEntity account = bankAccountRepository.findById(accountId.trim())
                    .orElseThrow(StandingOrderDomainException.AccessForbiddenException::new);
            if (!account.getCustomerId().equals(actorId)) {
                throw new StandingOrderDomainException.AccessForbiddenException();
            }
            return List.of(account.getAccountId());
        }

        List<BankAccountEntity> accounts = bankAccountRepository.findByCustomerId(actorId);
        List<String> accountIds = new ArrayList<>();
        for (BankAccountEntity account : accounts) {
            accountIds.add(account.getAccountId());
        }
        return accountIds;
    }

    private String asMoney(BigDecimal value) {
        if (value == null) {
            return "0.0000";
        }
        return value.setScale(4, RoundingMode.HALF_UP).toPlainString();
    }

    private String asMoneyOrNull(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return asMoney(value);
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}

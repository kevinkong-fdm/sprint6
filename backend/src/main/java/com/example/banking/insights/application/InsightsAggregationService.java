package com.example.banking.insights.application;

import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.MovementDirection;
import com.example.banking.account.domain.MovementType;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import com.example.banking.insights.domain.ComparisonMode;
import com.example.banking.insights.domain.InsufficiencyReason;
import com.example.banking.insights.domain.SpendingCategoryMetricEntity;
import com.example.banking.insights.domain.SpendingInsightSnapshotEntity;
import com.example.banking.insights.infrastructure.SpendingCategoryMetricRepository;
import com.example.banking.insights.infrastructure.SpendingInsightSnapshotRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class InsightsAggregationService {

    private final AccountMovementRepository accountMovementRepository;
    private final SpendingInsightSnapshotRepository spendingInsightSnapshotRepository;
    private final SpendingCategoryMetricRepository spendingCategoryMetricRepository;

    public InsightsAggregationService(
            AccountMovementRepository accountMovementRepository,
            SpendingInsightSnapshotRepository spendingInsightSnapshotRepository,
            SpendingCategoryMetricRepository spendingCategoryMetricRepository
    ) {
        this.accountMovementRepository = accountMovementRepository;
        this.spendingInsightSnapshotRepository = spendingInsightSnapshotRepository;
        this.spendingCategoryMetricRepository = spendingCategoryMetricRepository;
    }

    public InsightsAggregationResult aggregateAndPersist(
            String customerId,
            String accountId,
            List<String> accountIds,
            LocalDate periodStart,
            LocalDate periodEnd,
            ComparisonMode comparisonMode,
            String correlationId,
            ZoneId platformZone
    ) {
        Instant currentStart = periodStart.atStartOfDay(platformZone).toInstant();
        Instant currentEndExclusive = periodEnd.plusDays(1).atStartOfDay(platformZone).toInstant();

        List<AccountMovementEntity> currentMovements = accountMovementRepository
                .findByAccountIdInAndDirectionAndPostedAtGreaterThanEqualAndPostedAtLessThan(
                        accountIds,
                        MovementDirection.DEBIT,
                        currentStart,
                        currentEndExclusive);

        Map<String, BigDecimal> currentByCategory = aggregateByCategory(currentMovements);

        Map<String, BigDecimal> previousByCategory = Map.of();
        BigDecimal previousTotal = null;
        BigDecimal totalDelta = null;
        BigDecimal percentDelta = null;
        boolean insufficientData = false;

        if (comparisonMode == ComparisonMode.PREVIOUS_PERIOD) {
            long periodDays = periodEnd.toEpochDay() - periodStart.toEpochDay() + 1;
            LocalDate previousEnd = periodStart.minusDays(1);
            LocalDate previousStart = previousEnd.minusDays(periodDays - 1);

            Instant previousStartInstant = previousStart.atStartOfDay(platformZone).toInstant();
            Instant previousEndExclusive = previousEnd.plusDays(1).atStartOfDay(platformZone).toInstant();

            List<AccountMovementEntity> previousMovements = accountMovementRepository
                    .findByAccountIdInAndDirectionAndPostedAtGreaterThanEqualAndPostedAtLessThan(
                            accountIds,
                            MovementDirection.DEBIT,
                            previousStartInstant,
                            previousEndExclusive);

            previousByCategory = aggregateByCategory(previousMovements);
            previousTotal = sum(previousByCategory.values());
            insufficientData = previousMovements.isEmpty();
        }

        BigDecimal currentTotal = sum(currentByCategory.values());
        if (previousTotal != null) {
            totalDelta = currentTotal.subtract(previousTotal).setScale(4, RoundingMode.HALF_UP);
            percentDelta = percent(totalDelta, previousTotal);
        }

        InsufficiencyReason insufficiencyReason = insufficientData
                ? InsufficiencyReason.INSUFFICIENT_HISTORY
                : InsufficiencyReason.NONE;

        SpendingInsightSnapshotEntity snapshot = spendingInsightSnapshotRepository.save(
                SpendingInsightSnapshotEntity.create(
                        customerId,
                        accountId,
                        periodStart,
                        periodEnd,
                        comparisonMode,
                        insufficientData,
                        insufficiencyReason,
                        correlationId));

        Set<String> orderedCategories = new LinkedHashSet<>();
        orderedCategories.addAll(currentByCategory.keySet());
        List<String> sortedCategories = orderedCategories.stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        List<CategoryAggregation> categories = new ArrayList<>();
        for (String category : sortedCategories) {
            BigDecimal current = normalize(currentByCategory.get(category));
            BigDecimal previous = previousByCategory.containsKey(category)
                    ? normalize(previousByCategory.get(category))
                    : null;
            BigDecimal delta = previous == null ? null : current.subtract(previous).setScale(4, RoundingMode.HALF_UP);
            BigDecimal deltaPercent = previous == null ? null : percent(delta, previous);

            spendingCategoryMetricRepository.save(
                    SpendingCategoryMetricEntity.create(
                            snapshot.getInsightSnapshotId(),
                            category,
                            current,
                            previous,
                            delta,
                            deltaPercent));

            categories.add(new CategoryAggregation(category, current, previous, delta, deltaPercent));
        }

        return new InsightsAggregationResult(
                snapshot,
                categories,
                new TotalsAggregation(currentTotal, previousTotal, totalDelta, percentDelta));
    }

    private Map<String, BigDecimal> aggregateByCategory(List<AccountMovementEntity> movements) {
        Map<String, BigDecimal> totals = new HashMap<>();

        for (AccountMovementEntity movement : movements) {
            String category = toCategory(movement.getMovementType());
            BigDecimal amount = normalize(movement.getAmount());
            totals.merge(category, amount, BigDecimal::add);
        }

        return totals;
    }

    private String toCategory(MovementType movementType) {
        if (movementType == MovementType.WITHDRAWAL) {
            return "CASH_WITHDRAWAL";
        }
        if (movementType == MovementType.TRANSFER_DEBIT) {
            return "TRANSFER";
        }
        if (movementType == MovementType.CLOSEOUT_DEBIT) {
            return "CLOSEOUT";
        }
        return "OTHER_DEBIT";
    }

    private BigDecimal sum(Iterable<BigDecimal> values) {
        BigDecimal result = BigDecimal.ZERO.setScale(4);
        for (BigDecimal value : values) {
            result = result.add(normalize(value));
        }
        return result;
    }

    private BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(4);
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal percent(BigDecimal delta, BigDecimal baseline) {
        if (baseline == null || baseline.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return delta
                .divide(baseline, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.HALF_UP);
    }

    public record CategoryAggregation(
            String category,
            BigDecimal currentTotal,
            BigDecimal previousTotal,
            BigDecimal deltaAmount,
            BigDecimal deltaPercent
    ) {
    }

    public record TotalsAggregation(
            BigDecimal currentTotal,
            BigDecimal previousTotal,
            BigDecimal deltaAmount,
            BigDecimal deltaPercent
    ) {
    }

    public record InsightsAggregationResult(
            SpendingInsightSnapshotEntity snapshot,
            List<CategoryAggregation> categories,
            TotalsAggregation totals
    ) {
    }
}

package com.example.banking.statement.application;

import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.MovementDirection;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import com.example.banking.statement.domain.StatementEntryType;
import com.example.banking.standingorder.application.PlatformTimezoneService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StatementAggregationService {

    private final AccountMovementRepository accountMovementRepository;
    private final PlatformTimezoneService platformTimezoneService;

    public StatementAggregationService(
            AccountMovementRepository accountMovementRepository,
            PlatformTimezoneService platformTimezoneService
    ) {
        this.accountMovementRepository = accountMovementRepository;
        this.platformTimezoneService = platformTimezoneService;
    }

    public StatementAggregationResult aggregate(String accountId, YearMonth statementMonth) {
        Instant startInclusive = platformTimezoneService.monthStart(statementMonth);
        Instant endExclusive = platformTimezoneService.monthEndExclusive(statementMonth);

        BigDecimal openingBalance = accountMovementRepository
                .findTopByAccountIdAndPostedAtLessThanOrderByPostedAtDescMovementIdDesc(accountId, startInclusive)
                .map(AccountMovementEntity::getBalanceAfter)
                .orElse(BigDecimal.ZERO.setScale(4));

        List<AccountMovementEntity> monthlyMovements = accountMovementRepository
                .findByAccountIdAndPostedAtGreaterThanEqualAndPostedAtLessThanOrderByPostedAtAscMovementIdAsc(
                        accountId,
                        startInclusive,
                        endExclusive);

        BigDecimal totalDebits = BigDecimal.ZERO.setScale(4);
        BigDecimal totalCredits = BigDecimal.ZERO.setScale(4);
        BigDecimal closingBalance = openingBalance;

        List<StatementLineDraft> lineItems = new ArrayList<>();

        for (AccountMovementEntity movement : monthlyMovements) {
            BigDecimal amount = normalize(movement.getAmount());
            if (movement.getDirection() == MovementDirection.DEBIT) {
                totalDebits = totalDebits.add(amount);
            } else {
                totalCredits = totalCredits.add(amount);
            }

            closingBalance = normalize(movement.getBalanceAfter());
            lineItems.add(new StatementLineDraft(
                    movement.getMovementId(),
                    movement.getPostedAt() == null ? movement.getCreatedAt() : movement.getPostedAt(),
                    movement.getDirection() == MovementDirection.DEBIT ? StatementEntryType.DEBIT : StatementEntryType.CREDIT,
                    amount,
                    normalize(movement.getBalanceAfter()),
                    movement.getMovementType().name().replace('_', ' ')));
        }

        return new StatementAggregationResult(
                openingBalance,
                closingBalance,
                totalDebits,
                totalCredits,
                lineItems);
    }

    private BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(4);
        }
        return value.setScale(4, java.math.RoundingMode.HALF_UP);
    }

    public record StatementAggregationResult(
            BigDecimal openingBalance,
            BigDecimal closingBalance,
            BigDecimal totalDebits,
            BigDecimal totalCredits,
            List<StatementLineDraft> lineItems
    ) {
    }

    public record StatementLineDraft(
            String transactionId,
            Instant postedAt,
            StatementEntryType entryType,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String description
    ) {
    }
}

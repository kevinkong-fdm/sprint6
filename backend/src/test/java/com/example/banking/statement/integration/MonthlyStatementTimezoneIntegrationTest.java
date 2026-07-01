package com.example.banking.statement.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.MovementDirection;
import com.example.banking.account.domain.MovementType;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import com.example.banking.statement.application.StatementAggregationService;
import com.example.banking.standingorder.application.PlatformTimezoneService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonthlyStatementTimezoneIntegrationTest {

    @Mock
    private AccountMovementRepository accountMovementRepository;

    private PlatformTimezoneService platformTimezoneService;
    private StatementAggregationService statementAggregationService;

    @BeforeEach
    void setUp() {
        platformTimezoneService = new PlatformTimezoneService();
        statementAggregationService = new StatementAggregationService(accountMovementRepository, platformTimezoneService);
    }

    @Test
    void shouldAggregateUsingAestMonthBoundaries() {
        YearMonth month = YearMonth.parse("2026-06");
        Instant startInclusive = platformTimezoneService.monthStart(month);
        Instant endExclusive = platformTimezoneService.monthEndExclusive(month);

        AccountMovementEntity opening = movement(
                "acc-1",
                MovementType.DEPOSIT,
                MovementDirection.CREDIT,
                new BigDecimal("100.0000"),
                new BigDecimal("100.0000"),
                startInclusive.minusSeconds(60));

        AccountMovementEntity debit = movement(
                "acc-1",
                MovementType.WITHDRAWAL,
                MovementDirection.DEBIT,
                new BigDecimal("10.0000"),
                new BigDecimal("90.0000"),
                startInclusive.plusSeconds(60));

        AccountMovementEntity credit = movement(
                "acc-1",
                MovementType.DEPOSIT,
                MovementDirection.CREDIT,
                new BigDecimal("15.0000"),
                new BigDecimal("105.0000"),
                startInclusive.plusSeconds(120));

        when(accountMovementRepository.findTopByAccountIdAndPostedAtLessThanOrderByPostedAtDescMovementIdDesc("acc-1", startInclusive))
                .thenReturn(Optional.of(opening));
        when(accountMovementRepository.findByAccountIdAndPostedAtGreaterThanEqualAndPostedAtLessThanOrderByPostedAtAscMovementIdAsc(
                "acc-1",
                startInclusive,
                endExclusive)).thenReturn(List.of(debit, credit));

        StatementAggregationService.StatementAggregationResult result = statementAggregationService.aggregate("acc-1", month);

        assertEquals(new BigDecimal("100.0000"), result.openingBalance());
        assertEquals(new BigDecimal("10.0000"), result.totalDebits());
        assertEquals(new BigDecimal("15.0000"), result.totalCredits());
        assertEquals(new BigDecimal("105.0000"), result.closingBalance());
        assertEquals(2, result.lineItems().size());
        assertNotNull(result.lineItems().get(0).postedAt());

        verify(accountMovementRepository)
                .findTopByAccountIdAndPostedAtLessThanOrderByPostedAtDescMovementIdDesc("acc-1", startInclusive);
        verify(accountMovementRepository)
                .findByAccountIdAndPostedAtGreaterThanEqualAndPostedAtLessThanOrderByPostedAtAscMovementIdAsc(
                        "acc-1",
                        startInclusive,
                        endExclusive);
    }

    private AccountMovementEntity movement(
            String accountId,
            MovementType movementType,
            MovementDirection movementDirection,
            BigDecimal amount,
            BigDecimal balanceAfter,
            Instant postedAt
    ) {
        AccountMovementEntity entity = AccountMovementEntity.posted(
                accountId,
                movementType,
                amount,
                movementDirection,
                balanceAfter.subtract(amount),
                balanceAfter,
                "idem-1",
                "corr-1",
                "ref-1");
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "postedAt", postedAt);
        return entity;
    }
}

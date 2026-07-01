package com.example.banking.statement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import com.example.banking.statement.api.dto.GenerateMonthlyStatementRequest;
import com.example.banking.statement.api.dto.MonthlyStatementSingleResponse;
import com.example.banking.statement.api.dto.StatementResponseMapper;
import com.example.banking.statement.domain.MonthlyStatementEntity;
import com.example.banking.statement.domain.StatementEntryType;
import com.example.banking.statement.domain.StatementLineItemEntity;
import com.example.banking.statement.infrastructure.MonthlyStatementRepository;
import com.example.banking.statement.infrastructure.StatementLineItemRepository;
import com.example.banking.standingorder.application.PlatformTimezoneService;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateMonthlyStatementServiceTest {

    @Mock
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private PlatformTimezoneService platformTimezoneService;

    @Mock
    private StatementAggregationService statementAggregationService;

    @Mock
    private MonthlyStatementRepository monthlyStatementRepository;

    @Mock
    private StatementLineItemRepository statementLineItemRepository;

    private GenerateMonthlyStatementService service;

    @BeforeEach
    void setUp() {
        service = new GenerateMonthlyStatementService(
                standingOrderAuthorizationService,
                bankAccountRepository,
                platformTimezoneService,
                statementAggregationService,
                monthlyStatementRepository,
                statementLineItemRepository,
                new StatementResponseMapper());
    }

    @Test
    void shouldGenerateMonthlyStatementWhenAuthorized() {
        GenerateMonthlyStatementRequest request = new GenerateMonthlyStatementRequest("acc-1", "2026-06");
        BankAccountEntity account = account("acc-1", "cust-1");

        StatementAggregationService.StatementLineDraft line = new StatementAggregationService.StatementLineDraft(
                "txn-1",
                Instant.parse("2026-06-02T00:00:00Z"),
                StatementEntryType.DEBIT,
                new BigDecimal("10.0000"),
                new BigDecimal("90.0000"),
                "Coffee");

        StatementAggregationService.StatementAggregationResult aggregationResult =
                new StatementAggregationService.StatementAggregationResult(
                        new BigDecimal("100.0000"),
                        new BigDecimal("90.0000"),
                        new BigDecimal("10.0000"),
                        new BigDecimal("0.0000"),
                        List.of(line));

        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(platformTimezoneService.parseMonth("2026-06")).thenReturn(YearMonth.parse("2026-06"));
        when(platformTimezoneService.zoneId()).thenReturn(ZoneId.of("Australia/Brisbane"));
        when(platformTimezoneService.timezoneCode()).thenReturn("AEST");
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account));
        when(statementAggregationService.aggregate("acc-1", YearMonth.parse("2026-06"))).thenReturn(aggregationResult);
        when(monthlyStatementRepository.findByAccountIdAndCustomerIdAndStatementMonth("acc-1", "cust-1", "2026-06"))
                .thenReturn(Optional.empty());
        when(monthlyStatementRepository.save(any(MonthlyStatementEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, MonthlyStatementEntity.class));
        when(statementLineItemRepository.saveAll(any(List.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, List.class));

        MonthlyStatementSingleResponse response = service.generate(request, "actor-1", "corr-1");

        assertEquals("acc-1", response.data().accountId());
        assertEquals("2026-06", response.data().month());
        assertEquals(1, response.data().lineItems().size());
        verify(statementLineItemRepository).deleteByMonthlyStatementId(any());
    }

    @Test
    void shouldRejectInvalidMonthFormat() {
        GenerateMonthlyStatementRequest request = new GenerateMonthlyStatementRequest("acc-1", "bad-month");
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(platformTimezoneService.parseMonth("bad-month")).thenReturn(null);

        assertThrows(
                StandingOrderDomainException.StatementValidationException.class,
                () -> service.generate(request, "actor-1", "corr-1"));
    }

    @Test
    void shouldRejectFutureMonth() {
        ZoneId zone = ZoneId.of("Australia/Brisbane");
        YearMonth future = YearMonth.now(zone).plusMonths(1);

        GenerateMonthlyStatementRequest request = new GenerateMonthlyStatementRequest("acc-1", future.toString());
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(platformTimezoneService.parseMonth(future.toString())).thenReturn(future);
        when(platformTimezoneService.zoneId()).thenReturn(zone);

        assertThrows(
                StandingOrderDomainException.StatementValidationException.class,
                () -> service.generate(request, "actor-1", "corr-1"));
    }

    @Test
    void shouldRejectMissingAndForbiddenAccount() {
        ZoneId zone = ZoneId.of("Australia/Brisbane");
        YearMonth month = YearMonth.now(zone);
        GenerateMonthlyStatementRequest request = new GenerateMonthlyStatementRequest("acc-1", month.toString());

        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(platformTimezoneService.parseMonth(month.toString())).thenReturn(month);
        when(platformTimezoneService.zoneId()).thenReturn(zone);
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.empty());

        assertThrows(
                StandingOrderDomainException.StatementUnavailableException.class,
                () -> service.generate(request, "actor-1", "corr-1"));

        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account("acc-1", "other-customer")));

        assertThrows(
                StandingOrderDomainException.AccessForbiddenException.class,
                () -> service.generate(request, "actor-1", "corr-1"));
    }

    private BankAccountEntity account(String accountId, String customerId) {
        BankAccountEntity entity = BankAccountEntity.create(customerId, AccountType.CHECKING, "Main");
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "accountId", accountId);
        return entity;
    }
}

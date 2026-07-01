package com.example.banking.statement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetMonthlyStatementServiceTest {

    @Mock
    private StandingOrderAuthorizationService authorizationService;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private PlatformTimezoneService timezoneService;

    @Mock
    private MonthlyStatementRepository monthlyStatementRepository;

    @Mock
    private StatementLineItemRepository statementLineItemRepository;

    private GetMonthlyStatementService service;

    @BeforeEach
    void setUp() {
        service = new GetMonthlyStatementService(
                authorizationService,
                bankAccountRepository,
                timezoneService,
                monthlyStatementRepository,
                statementLineItemRepository,
                new StatementResponseMapper());
    }

    @Test
    void shouldReturnStatementWhenAuthorizedAndPresent() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        MonthlyStatementEntity statement = MonthlyStatementEntity.createOrReplace(
                null,
                "acc-1",
                "cust-1",
                "2026-06",
                "AEST",
                new BigDecimal("100.0000"),
                new BigDecimal("120.0000"),
                new BigDecimal("20.0000"),
                new BigDecimal("40.0000"),
                1);

        when(authorizationService.requireActorId(any())).thenReturn("cust-1");
        when(timezoneService.parseMonth("2026-06")).thenReturn(java.time.YearMonth.parse("2026-06"));
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account));
        when(monthlyStatementRepository.findByAccountIdAndCustomerIdAndStatementMonth("acc-1", "cust-1", "2026-06"))
                .thenReturn(Optional.of(statement));
        when(statementLineItemRepository.findByMonthlyStatementIdOrderByPostedAtAscTransactionIdAsc(any()))
                .thenReturn(List.of(StatementLineItemEntity.create(
                        statement.getMonthlyStatementId(),
                        "txn-1",
                        Instant.parse("2026-06-01T00:00:00Z"),
                        StatementEntryType.DEBIT,
                        new BigDecimal("10.0000"),
                        new BigDecimal("90.0000"),
                        "Coffee")));

        MonthlyStatementSingleResponse response = service.get("acc-1", "2026-06", "cust-1", "corr-1");

        assertEquals("2026-06", response.data().month());
        assertEquals("acc-1", response.data().accountId());
        assertEquals(1, response.data().lineItems().size());
    }

    @Test
    void shouldRejectInvalidMonth() {
        when(authorizationService.requireActorId(any())).thenReturn("cust-1");
        when(timezoneService.parseMonth("bad")).thenReturn(null);

        assertThrows(StandingOrderDomainException.StatementValidationException.class,
                () -> service.get("acc-1", "bad", "cust-1", "corr-1"));
    }

    @Test
    void shouldRejectMissingForbiddenOrMissingStatement() {
        when(authorizationService.requireActorId(any())).thenReturn("cust-1");
        when(timezoneService.parseMonth("2026-06")).thenReturn(java.time.YearMonth.parse("2026-06"));
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.empty());

        assertThrows(StandingOrderDomainException.StatementUnavailableException.class,
                () -> service.get("acc-1", "2026-06", "cust-1", "corr-1"));

        BankAccountEntity foreign = BankAccountEntity.create("other", AccountType.CHECKING, "Other");
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(foreign));

        assertThrows(StandingOrderDomainException.AccessForbiddenException.class,
                () -> service.get("acc-1", "2026-06", "cust-1", "corr-1"));

        BankAccountEntity owned = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Mine");
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(owned));
        when(monthlyStatementRepository.findByAccountIdAndCustomerIdAndStatementMonth("acc-1", "cust-1", "2026-06"))
                .thenReturn(Optional.empty());

        assertThrows(StandingOrderDomainException.StatementUnavailableException.class,
                () -> service.get("acc-1", "2026-06", "cust-1", "corr-1"));
    }
}

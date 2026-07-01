package com.example.banking.statement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import com.example.banking.statement.api.dto.MonthlyStatementListResponse;
import com.example.banking.statement.api.dto.MonthlyStatementResponse;
import com.example.banking.statement.api.dto.StatementLineItemResponse;
import com.example.banking.statement.api.dto.StatementResponseMapper;
import com.example.banking.statement.domain.MonthlyStatementEntity;
import com.example.banking.statement.domain.StatementEntryType;
import com.example.banking.statement.domain.StatementLineItemEntity;
import com.example.banking.statement.infrastructure.MonthlyStatementRepository;
import com.example.banking.statement.infrastructure.StatementLineItemRepository;
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
class ListMonthlyStatementsServiceTest {

        private static final String ACCOUNT_ID = "acc-1";

    @Mock
    private StandingOrderAuthorizationService authorizationService;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private MonthlyStatementRepository monthlyStatementRepository;

    @Mock
    private StatementLineItemRepository statementLineItemRepository;

    private StatementResponseMapper mapper;
    private ListMonthlyStatementsService service;

    @BeforeEach
    void setUp() {
        mapper = new StatementResponseMapper();
        service = new ListMonthlyStatementsService(
                authorizationService,
                bankAccountRepository,
                monthlyStatementRepository,
                statementLineItemRepository,
                mapper);
    }

    @Test
    void shouldListLatestStatementPerMonth() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        MonthlyStatementEntity juneLatest = statement(ACCOUNT_ID, "cust-1", "2026-06");
        MonthlyStatementEntity juneOlder = statement(ACCOUNT_ID, "cust-1", "2026-06");
        MonthlyStatementEntity may = statement(ACCOUNT_ID, "cust-1", "2026-05");

        when(authorizationService.requireActorId(any())).thenReturn("cust-1");
        when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(monthlyStatementRepository.findByAccountIdAndCustomerIdOrderByStatementMonthDescGeneratedAtDesc(ACCOUNT_ID, "cust-1"))
                .thenReturn(List.of(juneLatest, juneOlder, may));
        when(statementLineItemRepository.findByMonthlyStatementIdOrderByPostedAtAscTransactionIdAsc(any()))
                .thenAnswer(invocation -> List.of(line(invocation.getArgument(0, String.class))));

        MonthlyStatementListResponse response = service.list(ACCOUNT_ID, "cust-1", "corr-1");

        assertEquals(2, response.data().size());
        assertEquals("2026-06", response.data().get(0).month());
        assertEquals("2026-05", response.data().get(1).month());
    }

    @Test
    void shouldRejectMissingOrForbiddenAccount() {
        when(authorizationService.requireActorId(any())).thenReturn("cust-1");
        when(bankAccountRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(StandingOrderDomainException.StatementUnavailableException.class,
                () -> service.list("missing", "cust-1", "corr-1"));

        BankAccountEntity other = BankAccountEntity.create("other", AccountType.CHECKING, "Other");
        when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(other));

        assertThrows(StandingOrderDomainException.AccessForbiddenException.class,
                () -> service.list(ACCOUNT_ID, "cust-1", "corr-1"));
    }

    @Test
    void shouldRejectEmptyHistory() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        when(authorizationService.requireActorId(any())).thenReturn("cust-1");
        when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(monthlyStatementRepository.findByAccountIdAndCustomerIdOrderByStatementMonthDescGeneratedAtDesc(ACCOUNT_ID, "cust-1"))
                .thenReturn(List.of());

        assertThrows(StandingOrderDomainException.StatementUnavailableException.class,
                () -> service.list(ACCOUNT_ID, "cust-1", "corr-1"));
    }

    private MonthlyStatementEntity statement(String accountId, String customerId, String month) {
        return MonthlyStatementEntity.createOrReplace(
                null,
                accountId,
                customerId,
                month,
                "AEST",
                new BigDecimal("100.0000"),
                new BigDecimal("120.0000"),
                new BigDecimal("10.0000"),
                new BigDecimal("30.0000"),
                1);
    }

    private StatementLineItemEntity line(String statementId) {
        return StatementLineItemEntity.create(
                statementId,
                "txn-1",
                Instant.parse("2026-06-01T00:00:00Z"),
                StatementEntryType.DEBIT,
                new BigDecimal("10.0000"),
                new BigDecimal("90.0000"),
                "Coffee");
    }
}

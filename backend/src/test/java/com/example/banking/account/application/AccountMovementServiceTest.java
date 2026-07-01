package com.example.banking.account.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.api.dto.DepositRequest;
import com.example.banking.account.api.dto.MovementResponse;
import com.example.banking.account.api.dto.WithdrawalRequest;
import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.domain.MovementDirection;
import com.example.banking.account.domain.MovementType;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import com.example.banking.account.infrastructure.BankAccountRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountMovementServiceTest {

    @Mock
    private AccountAuthorizationService accountAuthorizationService;

    @Mock
    private MovementIdempotencyService movementIdempotencyService;

    @Mock
    private AccountMovementRepository accountMovementRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private AccountResponseMapper accountResponseMapper;

    private AccountMovementService service;

    @BeforeEach
    void setUp() {
        service = new AccountMovementService(
                accountAuthorizationService,
                movementIdempotencyService,
                accountMovementRepository,
                bankAccountRepository,
                accountResponseMapper);
    }

    @Test
    void shouldRejectInvalidDepositAmount() {
        assertThrows(
                AccountDomainException.DepositValidationException.class,
                () -> service.deposit("acc-1", new DepositRequest(BigDecimal.ZERO, "idem"), "corr", "cust-1"));
    }

    @Test
    void shouldReturnIdempotentDeposit() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        AccountMovementEntity existing = AccountMovementEntity.posted(
                "acc-1",
                MovementType.DEPOSIT,
                new BigDecimal("20.0000"),
                MovementDirection.CREDIT,
                new BigDecimal("10.0000"),
                new BigDecimal("30.0000"),
                "idem-1",
                "corr-1",
                null);

        when(accountAuthorizationService.requireOwnedAccount("acc-1", "cust-1")).thenReturn(account);
        when(movementIdempotencyService.findExisting("acc-1", MovementType.DEPOSIT, "idem-1"))
                .thenReturn(Optional.of(existing));
        when(accountResponseMapper.toMovementResponse(existing))
                .thenReturn(new MovementResponse(
                        existing.getMovementId(),
                        "acc-1",
                        "DEPOSIT",
                        "20.0000",
                        "30.0000",
                        existing.getPostedAt()));

        MovementResponse response = service.deposit(
                "acc-1",
                new DepositRequest(new BigDecimal("20.0000"), "idem-1"),
                "corr-1",
                "cust-1");

        assertEquals("DEPOSIT", response.movementType());
        verify(accountMovementRepository, never()).save(any(AccountMovementEntity.class));
    }

    @Test
    void shouldPersistDeposit() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");

        when(accountAuthorizationService.requireOwnedAccount("acc-1", "cust-1")).thenReturn(account);
        when(movementIdempotencyService.findExisting("acc-1", MovementType.DEPOSIT, "idem-2"))
                .thenReturn(Optional.empty());
        when(bankAccountRepository.save(any(BankAccountEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, BankAccountEntity.class));
        when(accountMovementRepository.save(any(AccountMovementEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, AccountMovementEntity.class));
        when(accountResponseMapper.toMovementResponse(any(AccountMovementEntity.class)))
                .thenAnswer(invocation -> {
                    AccountMovementEntity entity = invocation.getArgument(0, AccountMovementEntity.class);
                    return new MovementResponse(
                            entity.getMovementId(),
                            entity.getAccountId(),
                            entity.getMovementType().name(),
                            entity.getAmount().toPlainString(),
                            entity.getBalanceAfter().toPlainString(),
                            entity.getPostedAt());
                });

        MovementResponse response = service.deposit(
                "acc-1",
                new DepositRequest(new BigDecimal("15.123456"), "idem-2"),
                "corr-2",
                "cust-1");

        assertEquals("15.1235", account.getAvailableBalance().toPlainString());
        assertEquals("15.1235", response.amount());
    }

    @Test
    void shouldRejectInvalidWithdrawalAmount() {
        assertThrows(
                AccountDomainException.WithdrawalValidationException.class,
                () -> service.withdraw("acc-1", new WithdrawalRequest(BigDecimal.ZERO, "idem"), "corr", "cust-1"));
    }

    @Test
    void shouldRejectInsufficientWithdrawalFunds() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");

        when(accountAuthorizationService.requireOwnedAccount("acc-1", "cust-1")).thenReturn(account);
        when(movementIdempotencyService.findExisting("acc-1", MovementType.WITHDRAWAL, "idem-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountDomainException.InsufficientWithdrawalFundsException.class,
                () -> service.withdraw(
                        "acc-1",
                        new WithdrawalRequest(new BigDecimal("1.0000"), "idem-1"),
                        "corr-1",
                        "cust-1"));
    }

    @Test
    void shouldPersistWithdrawal() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        account.credit(new BigDecimal("20.0000"));

        when(accountAuthorizationService.requireOwnedAccount("acc-1", "cust-1")).thenReturn(account);
        when(movementIdempotencyService.findExisting("acc-1", MovementType.WITHDRAWAL, "idem-2"))
                .thenReturn(Optional.empty());
        when(bankAccountRepository.save(any(BankAccountEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, BankAccountEntity.class));
        when(accountMovementRepository.save(any(AccountMovementEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, AccountMovementEntity.class));
        when(accountResponseMapper.toMovementResponse(any(AccountMovementEntity.class)))
                .thenAnswer(invocation -> {
                    AccountMovementEntity entity = invocation.getArgument(0, AccountMovementEntity.class);
                    return new MovementResponse(
                            entity.getMovementId(),
                            entity.getAccountId(),
                            entity.getMovementType().name(),
                            entity.getAmount().toPlainString(),
                            entity.getBalanceAfter().toPlainString(),
                            entity.getPostedAt());
                });

        MovementResponse response = service.withdraw(
                "acc-1",
                new WithdrawalRequest(new BigDecimal("5.12555"), "idem-2"),
                "corr-2",
                "cust-1");

        assertEquals("14.8744", account.getAvailableBalance().toPlainString());
        assertEquals("5.1256", response.amount());
    }
}

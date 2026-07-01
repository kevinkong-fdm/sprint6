package com.example.banking.account.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.api.dto.MovementResponse;
import com.example.banking.account.api.dto.TransferRequest;
import com.example.banking.account.api.dto.TransferResponse;
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
class TransferFundsServiceTest {

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

    private TransferFundsService service;

    @BeforeEach
    void setUp() {
        service = new TransferFundsService(
                accountAuthorizationService,
                movementIdempotencyService,
                accountMovementRepository,
                bankAccountRepository,
                accountResponseMapper);
    }

    @Test
    void shouldRejectInvalidAmount() {
        TransferRequest request = new TransferRequest("src-1", "dst-1", BigDecimal.ZERO, "idem");

        assertThrows(AccountDomainException.TransferValidationException.class,
                () -> service.transfer(request, "corr-1", "cust-1"));
    }

    @Test
    void shouldRejectSameSourceAndDestination() {
        TransferRequest request = new TransferRequest("acc-1", "acc-1", new BigDecimal("1.0000"), "idem");

        assertThrows(AccountDomainException.TransferPairingException.class,
                () -> service.transfer(request, "corr-1", "cust-1"));
    }

    @Test
    void shouldReturnIdempotentTransfer() {
        AccountMovementEntity debit = AccountMovementEntity.posted(
                "src-1",
                MovementType.TRANSFER_DEBIT,
                new BigDecimal("10.0000"),
                MovementDirection.DEBIT,
                new BigDecimal("50.0000"),
                new BigDecimal("40.0000"),
                "idem-1",
                "corr-1",
                "transfer-1");
        AccountMovementEntity credit = AccountMovementEntity.posted(
                "dst-1",
                MovementType.TRANSFER_CREDIT,
                new BigDecimal("10.0000"),
                MovementDirection.CREDIT,
                new BigDecimal("20.0000"),
                new BigDecimal("30.0000"),
                "idem-1",
                "corr-1",
                "transfer-1");

        when(movementIdempotencyService.findExisting("src-1", MovementType.TRANSFER_DEBIT, "idem-1"))
                .thenReturn(Optional.of(debit));
        when(accountMovementRepository.findFirstByReferenceIdAndMovementTypeOrderByCreatedAtDesc(
                "transfer-1",
                MovementType.TRANSFER_CREDIT)).thenReturn(Optional.of(credit));
        when(accountResponseMapper.toMovementResponse(debit))
                .thenReturn(new MovementResponse("m1", "src-1", "TRANSFER_DEBIT", "10.0000", "40.0000", debit.getPostedAt()));
        when(accountResponseMapper.toMovementResponse(credit))
                .thenReturn(new MovementResponse("m2", "dst-1", "TRANSFER_CREDIT", "10.0000", "30.0000", credit.getPostedAt()));

        TransferResponse response = service.transfer(
                new TransferRequest("src-1", "dst-1", new BigDecimal("10.0000"), "idem-1"),
                "corr-1",
                "cust-1");

        assertEquals("transfer-1", response.transferId());
        verify(accountAuthorizationService, never()).requireOwnedAccount(any(), any());
    }

    @Test
    void shouldRejectBrokenIdempotentTransferWithoutCredit() {
        AccountMovementEntity debit = AccountMovementEntity.posted(
                "src-1",
                MovementType.TRANSFER_DEBIT,
                new BigDecimal("10.0000"),
                MovementDirection.DEBIT,
                new BigDecimal("50.0000"),
                new BigDecimal("40.0000"),
                "idem-1",
                "corr-1",
                "transfer-1");

        when(movementIdempotencyService.findExisting("src-1", MovementType.TRANSFER_DEBIT, "idem-1"))
                .thenReturn(Optional.of(debit));
        when(accountMovementRepository.findFirstByReferenceIdAndMovementTypeOrderByCreatedAtDesc(
                "transfer-1",
                MovementType.TRANSFER_CREDIT)).thenReturn(Optional.empty());

        assertThrows(
                AccountDomainException.TransferValidationException.class,
                () -> service.transfer(
                        new TransferRequest("src-1", "dst-1", new BigDecimal("10.0000"), "idem-1"),
                        "corr-1",
                        "cust-1"));
    }

    @Test
    void shouldRejectWhenFundsAreInsufficient() {
        BankAccountEntity source = BankAccountEntity.create("cust-1", AccountType.CHECKING, "source");
        BankAccountEntity destination = BankAccountEntity.create("cust-1", AccountType.SAVINGS, "dest");

        when(movementIdempotencyService.findExisting("src-1", MovementType.TRANSFER_DEBIT, "idem-1"))
                .thenReturn(Optional.empty());
        when(accountAuthorizationService.requireOwnedAccount("src-1", "cust-1")).thenReturn(source);
        when(accountAuthorizationService.requireOwnedAccount("dst-1", "cust-1")).thenReturn(destination);

        assertThrows(
                AccountDomainException.InsufficientTransferFundsException.class,
                () -> service.transfer(
                        new TransferRequest("src-1", "dst-1", new BigDecimal("1.0000"), "idem-1"),
                        "corr-1",
                        "cust-1"));
    }

    @Test
    void shouldTransferFundsAndPersistMovements() {
        BankAccountEntity source = BankAccountEntity.create("cust-1", AccountType.CHECKING, "source");
        BankAccountEntity destination = BankAccountEntity.create("cust-1", AccountType.SAVINGS, "dest");
        source.credit(new BigDecimal("50.0000"));
        destination.credit(new BigDecimal("5.0000"));

        when(movementIdempotencyService.findExisting("src-1", MovementType.TRANSFER_DEBIT, "idem-2"))
                .thenReturn(Optional.empty());
        when(accountAuthorizationService.requireOwnedAccount("src-1", "cust-1")).thenReturn(source);
        when(accountAuthorizationService.requireOwnedAccount("dst-1", "cust-1")).thenReturn(destination);
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

        TransferResponse response = service.transfer(
                new TransferRequest("src-1", "dst-1", new BigDecimal("10.0000"), "idem-2"),
                "corr-2",
                "cust-1");

        assertNotNull(response.transferId());
        assertEquals("40.0000", source.getAvailableBalance().toPlainString());
        assertEquals("15.0000", destination.getAvailableBalance().toPlainString());

        verify(bankAccountRepository).save(eq(source));
        verify(bankAccountRepository).save(eq(destination));
        verify(accountMovementRepository, times(2)).save(any(AccountMovementEntity.class));
    }
}

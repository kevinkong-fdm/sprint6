package com.example.banking.account.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.api.dto.DeleteAccountRequest;
import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.domain.DeleteEligibilityResult;
import com.example.banking.account.domain.MovementStatus;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import com.example.banking.account.infrastructure.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteAccountWorkflowServiceTest {

    @Mock
    private AccountAuthorizationService accountAuthorizationService;

    @Mock
    private AccountMovementRepository accountMovementRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private AccountAuditService accountAuditService;

    private DeleteAccountWorkflowService service;

    @BeforeEach
    void setUp() {
        service = new DeleteAccountWorkflowService(
                accountAuthorizationService,
                accountMovementRepository,
                bankAccountRepository,
                accountAuditService);
    }

    @Test
    void shouldRejectDeleteWhenPendingMovementExists() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        DeleteAccountRequest request = new DeleteAccountRequest("dst-1", "idem-1");

        when(accountAuthorizationService.requireOwnedAccountForDelete("acc-1", "cust-1")).thenReturn(account);
        when(accountMovementRepository.existsByAccountIdAndStatus("acc-1", MovementStatus.PENDING)).thenReturn(true);

        AccountDomainException.DeleteEligibilityException ex = assertThrows(
                AccountDomainException.DeleteEligibilityException.class,
                () -> service.delete("acc-1", request, "corr-1", "cust-1"));

        assertEquals("ACCT-DEL-002", ex.getErrorCode());
        assertEquals(409, ex.getStatus());
        verify(accountAuditService).recordDeleteDecision(
                "acc-1",
                "cust-1",
                "dst-1",
                DeleteEligibilityResult.REJECTED_PENDING_MOVEMENT,
                "ACCT-DEL-002",
                "corr-1");
        verify(bankAccountRepository, never()).delete(account);
    }

    @Test
    void shouldDeleteAccountAndRecordAllowedDecision() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        DeleteAccountRequest request = new DeleteAccountRequest("dst-1", "idem-2");

        when(accountAuthorizationService.requireOwnedAccountForDelete("acc-1", "cust-1")).thenReturn(account);
        when(accountMovementRepository.existsByAccountIdAndStatus("acc-1", MovementStatus.PENDING)).thenReturn(false);

        service.delete("acc-1", request, "corr-2", "cust-1");

        verify(bankAccountRepository).delete(account);
        verify(accountAuditService).recordDeleteDecision(
                "acc-1",
                "cust-1",
                "dst-1",
                DeleteEligibilityResult.ALLOWED,
                null,
                "corr-2");
    }
}

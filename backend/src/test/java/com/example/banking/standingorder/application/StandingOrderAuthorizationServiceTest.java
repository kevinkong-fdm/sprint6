package com.example.banking.standingorder.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StandingOrderAuthorizationServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private StandingOrderRepository standingOrderRepository;

    private StandingOrderAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new StandingOrderAuthorizationService(bankAccountRepository, standingOrderRepository);
    }

    @Test
    void shouldRequireActorId() {
        assertEquals("actor-1", service.requireActorId(" actor-1 "));
        assertThrows(StandingOrderDomainException.AuthenticationRequiredException.class, () -> service.requireActorId(" "));
    }

    @Test
    void shouldRequireOwnedSourceAccount() {
        BankAccountEntity account = BankAccountEntity.create(
                "cust-1",
                com.example.banking.account.domain.AccountType.CHECKING,
            "Main");
        when(bankAccountRepository.findById(account.getAccountId())).thenReturn(Optional.of(account));

        BankAccountEntity resolved = service.requireOwnedSourceAccount(account.getAccountId(), "cust-1");
        assertEquals(account.getAccountId(), resolved.getAccountId());

        assertThrows(StandingOrderDomainException.SourceUnauthorizedException.class,
                () -> service.requireOwnedSourceAccount(account.getAccountId(), "other"));
    }

    @Test
    void shouldRequireOwnedDestinationAccount() {
        BankAccountEntity account = BankAccountEntity.create(
                "cust-1",
                com.example.banking.account.domain.AccountType.SAVINGS,
            "Save");
        when(bankAccountRepository.findById(account.getAccountId())).thenReturn(Optional.of(account));

        BankAccountEntity resolved = service.requireOwnedDestinationAccount(account.getAccountId(), "cust-1");
        assertEquals(account.getAccountId(), resolved.getAccountId());

        assertThrows(StandingOrderDomainException.DestinationOwnershipMismatchException.class,
                () -> service.requireOwnedDestinationAccount(account.getAccountId(), "other"));
    }

    @Test
    void shouldRequireOwnedStandingOrder() {
        StandingOrderEntity standingOrder = StandingOrderEntity.create(
                "cust-1",
                "src-1",
                "dst-1",
                new BigDecimal("10.0000"),
                com.example.banking.standingorder.domain.StandingOrderFrequency.DAILY,
                LocalDate.parse("2026-06-01"),
                null,
                null,
                null,
                "09:00",
                Instant.parse("2026-06-02T00:00:00Z"),
                "AEST",
                "idem-1");

        when(standingOrderRepository.findById(standingOrder.getStandingOrderId())).thenReturn(Optional.of(standingOrder));

        StandingOrderEntity resolved = service.requireOwnedStandingOrder(standingOrder.getStandingOrderId(), "cust-1");
        assertEquals(standingOrder.getStandingOrderId(), resolved.getStandingOrderId());

        assertThrows(StandingOrderDomainException.AccessForbiddenException.class,
                () -> service.requireOwnedStandingOrder(standingOrder.getStandingOrderId(), "other"));
    }
}

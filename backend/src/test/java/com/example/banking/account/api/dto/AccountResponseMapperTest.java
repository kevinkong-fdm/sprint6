package com.example.banking.account.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.domain.MovementDirection;
import com.example.banking.account.domain.MovementType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AccountResponseMapperTest {

    private final AccountResponseMapper mapper = new AccountResponseMapper();

    @Test
    void shouldMapAccountWithMoneyFormattingAndNullFallback() {
        BankAccountEntity account = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        account.credit(new BigDecimal("12.34567"));

        AccountResponse mapped = mapper.toAccountResponse(account);
        assertEquals("12.3457", mapped.availableBalance());
        assertEquals("12.3457", mapped.ledgerBalance());

        ReflectionTestUtils.setField(account, "availableBalance", null);
        ReflectionTestUtils.setField(account, "ledgerBalance", null);

        AccountResponse nullBalanceMapped = mapper.toAccountResponse(account);
        assertEquals("0.0000", nullBalanceMapped.availableBalance());
        assertEquals("0.0000", nullBalanceMapped.ledgerBalance());
    }

    @Test
    void shouldMapMovementAndHistoryItems() {
        AccountMovementEntity movement = AccountMovementEntity.posted(
                "acc-1",
                MovementType.WITHDRAWAL,
                new BigDecimal("5.12345"),
                MovementDirection.DEBIT,
                new BigDecimal("20.0000"),
                new BigDecimal("14.87655"),
                "idem-1",
                "corr-1",
                "ref-1");

        MovementResponse movementResponse = mapper.toMovementResponse(movement);
        assertEquals("5.1235", movementResponse.amount());
        assertEquals("14.8766", movementResponse.balanceAfter());

        TransactionHistoryItemResponse historyItem = mapper.toHistoryItem(movement);
        assertEquals("WITHDRAWAL", historyItem.movementType());
        assertEquals("5.1235", historyItem.amount());
        assertEquals("14.8766", historyItem.balanceAfter());
    }

    @Test
    void shouldMapAccountListResponse() {
        AccountListResponse response = mapper.toAccountListResponse(List.of(), 2, 20, 50);

        assertEquals(2, response.page());
        assertEquals(20, response.size());
        assertEquals(50, response.total());
    }
}

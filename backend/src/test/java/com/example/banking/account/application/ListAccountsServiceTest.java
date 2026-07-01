package com.example.banking.account.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.api.dto.AccountListResponse;
import com.example.banking.account.api.dto.AccountResponse;
import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ListAccountsServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private AccountResponseMapper accountResponseMapper;

    private ListAccountsService service;

    @BeforeEach
    void setUp() {
        service = new ListAccountsService(bankAccountRepository, accountResponseMapper);
    }

    @Test
    void shouldListAllAccountsWhenFilterInvalid() {
        BankAccountEntity entity = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        Page<BankAccountEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 200), 1);

        when(bankAccountRepository.findByCustomerId(eq("cust-1"), any(PageRequest.class))).thenReturn(page);
        when(accountResponseMapper.toAccountResponse(entity)).thenReturn(new AccountResponse(
                entity.getAccountId(),
                entity.getCustomerId(),
                entity.getAccountType().name(),
                entity.getNickname(),
                entity.getCurrencyCode(),
                entity.getAvailableBalance().toPlainString(),
                entity.getLedgerBalance().toPlainString(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()));
        when(accountResponseMapper.toAccountListResponse(any(), eq(1), eq(200), anyLong()))
                .thenAnswer(invocation -> new AccountListResponse(
                        invocation.getArgument(0, List.class),
                        invocation.getArgument(1, Integer.class),
                        invocation.getArgument(2, Integer.class),
                        invocation.getArgument(3, Long.class)));

        AccountListResponse response = service.list("cust-1", "invalid", 0, 500);

        assertEquals(1, response.page());
        assertEquals(200, response.size());
        assertEquals(1, response.items().size());

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(bankAccountRepository).findByCustomerId(eq("cust-1"), pageCaptor.capture());
        assertEquals(0, pageCaptor.getValue().getPageNumber());
        assertEquals(200, pageCaptor.getValue().getPageSize());
    }

    @Test
    void shouldListAccountsByTypeFilter() {
        BankAccountEntity entity = BankAccountEntity.create("cust-1", AccountType.SAVINGS, "Saver");
        Page<BankAccountEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(1, 10), 1);

        when(bankAccountRepository.findByCustomerIdAndAccountType(
                eq("cust-1"),
                eq(AccountType.SAVINGS),
                any(PageRequest.class))).thenReturn(page);
        when(accountResponseMapper.toAccountResponse(entity)).thenReturn(new AccountResponse(
                entity.getAccountId(),
                entity.getCustomerId(),
                entity.getAccountType().name(),
                entity.getNickname(),
                entity.getCurrencyCode(),
                entity.getAvailableBalance().toPlainString(),
                entity.getLedgerBalance().toPlainString(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()));
        when(accountResponseMapper.toAccountListResponse(any(), eq(2), eq(10), anyLong()))
                .thenAnswer(invocation -> new AccountListResponse(
                        invocation.getArgument(0, List.class),
                        invocation.getArgument(1, Integer.class),
                        invocation.getArgument(2, Integer.class),
                        invocation.getArgument(3, Long.class)));

        AccountListResponse response = service.list("cust-1", "savings", 2, 10);

        assertEquals(2, response.page());
        assertEquals(10, response.size());
        assertEquals("SAVINGS", response.items().get(0).accountType());
    }
}

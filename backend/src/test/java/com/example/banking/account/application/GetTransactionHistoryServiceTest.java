package com.example.banking.account.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.api.dto.AccountResponseMapper;
import com.example.banking.account.api.dto.TransactionHistoryItemResponse;
import com.example.banking.account.api.dto.TransactionHistoryResponse;
import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.MovementDirection;
import com.example.banking.account.domain.MovementType;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import java.math.BigDecimal;
import java.time.Instant;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class GetTransactionHistoryServiceTest {

    @Mock
    private AccountAuthorizationService accountAuthorizationService;

    @Mock
    private AccountMovementRepository accountMovementRepository;

    @Mock
    private AccountResponseMapper accountResponseMapper;

    private GetTransactionHistoryService service;

    @BeforeEach
    void setUp() {
        service = new GetTransactionHistoryService(
                accountAuthorizationService,
                accountMovementRepository,
                accountResponseMapper);
    }

    @Test
    void shouldRejectInvalidDateRange() {
        assertThrows(
                AccountDomainException.HistoryValidationException.class,
                () -> service.history(
                        "acc-1",
                        "cust-1",
                        Instant.parse("2026-06-30T00:00:00Z"),
                        Instant.parse("2026-06-01T00:00:00Z"),
                        null,
                        1,
                        20));
    }

    @Test
    void shouldRejectInvalidMovementType() {
        assertThrows(
                AccountDomainException.HistoryValidationException.class,
                () -> service.history(
                        "acc-1",
                        "cust-1",
                        null,
                        null,
                        "not-a-type",
                        1,
                        20));
    }

    @Test
    void shouldReturnPagedHistoryWithBoundsAndMapping() {
        AccountMovementEntity movement = AccountMovementEntity.posted(
                "acc-1",
                MovementType.DEPOSIT,
                new BigDecimal("15.0000"),
                MovementDirection.CREDIT,
                new BigDecimal("10.0000"),
                new BigDecimal("25.0000"),
                "idem-1",
                "corr-1",
                null);
        Page<AccountMovementEntity> page = new PageImpl<>(List.of(movement), PageRequest.of(0, 200), 1);

        when(accountMovementRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(accountResponseMapper.toHistoryItem(movement)).thenReturn(new TransactionHistoryItemResponse(
                movement.getMovementId(),
                movement.getMovementType().name(),
                movement.getAmount().toPlainString(),
                movement.getBalanceAfter().toPlainString(),
                movement.getStatus().name(),
                movement.getCreatedAt(),
                movement.getPostedAt(),
                movement.getReferenceId()));

        TransactionHistoryResponse response = service.history(
                "acc-1",
                "cust-1",
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-30T00:00:00Z"),
                "deposit",
                0,
                999);

        assertEquals("acc-1", response.accountId());
        assertEquals(1, response.page());
        assertEquals(200, response.size());
        assertEquals(1, response.total());
        assertEquals(1, response.items().size());

        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(accountMovementRepository).findAll(any(Specification.class), pageCaptor.capture());
        assertEquals(0, pageCaptor.getValue().getPageNumber());
        assertEquals(200, pageCaptor.getValue().getPageSize());
    }
}

package com.example.banking.standingorder.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.api.dto.TransferResponse;
import com.example.banking.account.application.TransferFundsService;
import com.example.banking.account.domain.AccountStatus;
import com.example.banking.account.domain.AccountType;
import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.standingorder.api.dto.StandingOrderResponseMapper;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionEntity;
import com.example.banking.standingorder.domain.StandingOrderExecutionOutcome;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import com.example.banking.standingorder.infrastructure.StandingOrderExecutionRepository;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class StandingOrderExecutionServiceTest {

    @Mock
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @Mock
    private StandingOrderIdempotencyService standingOrderIdempotencyService;

    @Mock
    private PlatformTimezoneService platformTimezoneService;

    @Mock
    private TransferFundsService transferFundsService;

    @Mock
    private StandingOrderExecutionRepository standingOrderExecutionRepository;

    @Mock
    private StandingOrderRepository standingOrderRepository;

    @Mock
    private StandingOrderAuditService standingOrderAuditService;

    private StandingOrderExecutionService service;

    @BeforeEach
    void setUp() {
        service = new StandingOrderExecutionService(
                standingOrderAuthorizationService,
                standingOrderIdempotencyService,
                platformTimezoneService,
                transferFundsService,
                standingOrderExecutionRepository,
                standingOrderRepository,
                new StandingOrderResponseMapper(),
                standingOrderAuditService);
    }

    @Test
    void shouldReturnExistingExecutionForIdempotentTrigger() {
        StandingOrderEntity standingOrder = standingOrder("ACTIVE");
        StandingOrderExecutionEntity existing = StandingOrderExecutionEntity.create(
                "so-1",
                Instant.parse("2026-06-02T00:00:00Z"),
                StandingOrderExecutionOutcome.SUCCESS,
                null,
                "trf-1",
                "idem-1",
                "corr-1");

        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1")).thenReturn(standingOrder);
        when(standingOrderIdempotencyService.normalizeKey("idem-1")).thenReturn("idem-1");
        when(standingOrderIdempotencyService.findExistingTrigger("so-1", "idem-1"))
                .thenReturn(Optional.of(existing));

        StandingOrderExecutionEntity result = service.trigger("so-1", "idem-1", "actor-1", "corr-1");

        assertSame(existing, result);
        verify(transferFundsService, never()).transfer(any(), any(), any());
    }

    @Test
    void shouldSkipExecutionWhenStandingOrderNotActive() {
        StandingOrderEntity paused = standingOrder("PAUSED");
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1")).thenReturn(paused);
        when(standingOrderExecutionRepository.save(any(StandingOrderExecutionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, StandingOrderExecutionEntity.class));

        assertThrows(
                StandingOrderDomainException.ExecutionSkippedException.class,
                () -> service.trigger("so-1", null, "actor-1", "corr-1"));

        verify(standingOrderAuditService).auditLifecycle("so-1", "cust-1", "EXECUTION_SKIPPED", "corr-1");
        verify(standingOrderAuthorizationService, never()).requireOwnedSourceAccount(any(), any());
    }

    @Test
    void shouldSkipExecutionWhenSourceAccountNotActive() {
        StandingOrderEntity standingOrder = standingOrder("ACTIVE");
        BankAccountEntity source = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        org.springframework.test.util.ReflectionTestUtils.setField(source, "status", AccountStatus.PENDING_DELETE);

        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1")).thenReturn(standingOrder);
        when(standingOrderAuthorizationService.requireOwnedSourceAccount("src-1", "cust-1")).thenReturn(source);
        when(standingOrderExecutionRepository.save(any(StandingOrderExecutionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, StandingOrderExecutionEntity.class));

        assertThrows(
                StandingOrderDomainException.ExecutionSkippedException.class,
                () -> service.trigger("so-1", null, "actor-1", "corr-1"));

        verify(transferFundsService, never()).transfer(any(), any(), any());
    }

    @Test
    void shouldFailExecutionWhenInsufficientFunds() {
        StandingOrderEntity standingOrder = standingOrder("ACTIVE");
        BankAccountEntity source = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");

        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1")).thenReturn(standingOrder);
        when(standingOrderAuthorizationService.requireOwnedSourceAccount("src-1", "cust-1")).thenReturn(source);
        when(standingOrderExecutionRepository.save(any(StandingOrderExecutionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, StandingOrderExecutionEntity.class));
        when(platformTimezoneService.computeNextExecutionAfterRun(any(), any()))
                .thenReturn(Instant.parse("2026-06-03T00:00:00Z"));
        when(standingOrderRepository.save(any(StandingOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, StandingOrderEntity.class));

        assertThrows(
                StandingOrderDomainException.ExecutionInsufficientFundsException.class,
                () -> service.trigger("so-1", null, "actor-1", "corr-1"));

        verify(standingOrderRepository).save(any(StandingOrderEntity.class));
    }

    @Test
    void shouldExecuteTransferAndRecordSuccess() {
        StandingOrderEntity standingOrder = standingOrder("ACTIVE");
        BankAccountEntity source = BankAccountEntity.create("cust-1", AccountType.CHECKING, "Main");
        source.credit(new BigDecimal("50.0000"));

        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1")).thenReturn(standingOrder);
        when(standingOrderAuthorizationService.requireOwnedSourceAccount("src-1", "cust-1")).thenReturn(source);
        when(transferFundsService.transfer(any(), eq("corr-1"), eq("cust-1")))
                .thenReturn(new TransferResponse("trf-1", null, null));
        when(standingOrderExecutionRepository.save(any(StandingOrderExecutionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, StandingOrderExecutionEntity.class));
        when(platformTimezoneService.computeNextExecutionAfterRun(any(), any()))
                .thenReturn(Instant.parse("2026-06-03T00:00:00Z"));
        when(standingOrderRepository.save(any(StandingOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, StandingOrderEntity.class));

        StandingOrderExecutionEntity result = service.trigger("so-1", null, "actor-1", "corr-1");

        assertEquals(StandingOrderExecutionOutcome.SUCCESS, result.getOutcome());
        assertEquals("trf-1", result.getTransferReferenceId());
        verify(standingOrderAuditService).auditLifecycle("so-1", "cust-1", "EXECUTION_SUCCESS", "corr-1");
    }

    @Test
    void shouldListExecutionsUsingOutcomeFilter() {
        StandingOrderEntity standingOrder = standingOrder("ACTIVE");
        StandingOrderExecutionEntity execution = StandingOrderExecutionEntity.create(
                "so-1",
                Instant.parse("2026-06-02T00:00:00Z"),
                StandingOrderExecutionOutcome.SUCCESS,
                null,
                "trf-1",
                null,
                "corr-1");

        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1")).thenReturn(standingOrder);
        when(standingOrderExecutionRepository.findByStandingOrderIdAndOutcome(eq("so-1"), eq(StandingOrderExecutionOutcome.SUCCESS), any()))
                .thenReturn(new PageImpl<>(List.of(execution)));

        var response = service.listExecutions("so-1", "SUCCESS", -1, 999, "actor-1", "corr-1");

        assertEquals(1, response.data().items().size());
        assertEquals("SUCCESS", response.data().items().get(0).outcome());
        assertEquals(1, response.data().page());
        assertEquals(200, response.data().size());
    }

    @Test
    void shouldRejectInvalidOutcomeFilter() {
        StandingOrderEntity standingOrder = standingOrder("ACTIVE");
        when(standingOrderAuthorizationService.requireActorId("actor-1")).thenReturn("cust-1");
        when(standingOrderAuthorizationService.requireOwnedStandingOrder("so-1", "cust-1")).thenReturn(standingOrder);

        assertThrows(
                StandingOrderDomainException.UpdateValidationException.class,
                () -> service.listExecutions("so-1", "INVALID", 1, 20, "actor-1", "corr-1"));
    }

    private StandingOrderEntity standingOrder(String status) {
        StandingOrderEntity entity = StandingOrderEntity.create(
                "cust-1",
                "src-1",
                "dst-1",
                new BigDecimal("10.0000"),
                StandingOrderFrequency.DAILY,
                LocalDate.parse("2026-06-01"),
                null,
                null,
                null,
                "09:00",
                Instant.parse("2026-06-02T00:00:00Z"),
                "AEST",
                "idem-1");
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "standingOrderId", "so-1");

        if ("PAUSED".equals(status)) {
            entity.pause();
        } else if ("CANCELED".equals(status)) {
            entity.cancel();
        }

        return entity;
    }
}

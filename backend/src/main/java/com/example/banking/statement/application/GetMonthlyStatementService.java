package com.example.banking.statement.application;

import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import com.example.banking.statement.api.dto.MonthlyStatementSingleResponse;
import com.example.banking.statement.api.dto.StatementResponseMapper;
import com.example.banking.statement.domain.MonthlyStatementEntity;
import com.example.banking.statement.domain.StatementLineItemEntity;
import com.example.banking.statement.infrastructure.MonthlyStatementRepository;
import com.example.banking.statement.infrastructure.StatementLineItemRepository;
import com.example.banking.standingorder.application.PlatformTimezoneService;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetMonthlyStatementService {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final BankAccountRepository bankAccountRepository;
    private final PlatformTimezoneService platformTimezoneService;
    private final MonthlyStatementRepository monthlyStatementRepository;
    private final StatementLineItemRepository statementLineItemRepository;
    private final StatementResponseMapper statementResponseMapper;

    public GetMonthlyStatementService(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            BankAccountRepository bankAccountRepository,
            PlatformTimezoneService platformTimezoneService,
            MonthlyStatementRepository monthlyStatementRepository,
            StatementLineItemRepository statementLineItemRepository,
            StatementResponseMapper statementResponseMapper
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.bankAccountRepository = bankAccountRepository;
        this.platformTimezoneService = platformTimezoneService;
        this.monthlyStatementRepository = monthlyStatementRepository;
        this.statementLineItemRepository = statementLineItemRepository;
        this.statementResponseMapper = statementResponseMapper;
    }

    public MonthlyStatementSingleResponse get(String accountId, String month, String actorId, String correlationId) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);
        if (platformTimezoneService.parseMonth(month) == null) {
            throw new StandingOrderDomainException.StatementValidationException(
                    "Month must use YYYY-MM format for monthly statement retrieval.");
        }

        BankAccountEntity account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new StandingOrderDomainException.StatementUnavailableException(
                        "Cannot retrieve statement because the requested account is unavailable."));

        if (!account.getCustomerId().equals(resolvedActorId)) {
            throw new StandingOrderDomainException.AccessForbiddenException();
        }

        MonthlyStatementEntity statement = monthlyStatementRepository
                .findByAccountIdAndCustomerIdAndStatementMonth(accountId, resolvedActorId, month)
                .orElseThrow(() -> new StandingOrderDomainException.StatementUnavailableException(
                        "No statement found for the requested month. Generate it first, then retrieve it."));

        List<StatementLineItemEntity> lineItems = statementLineItemRepository
                .findByMonthlyStatementIdOrderByPostedAtAscTransactionIdAsc(statement.getMonthlyStatementId());

        return statementResponseMapper.toResponse(statement, lineItems, correlationId);
    }
}

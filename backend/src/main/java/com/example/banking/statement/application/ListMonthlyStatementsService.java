package com.example.banking.statement.application;

import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import com.example.banking.statement.api.dto.MonthlyStatementListResponse;
import com.example.banking.statement.api.dto.MonthlyStatementResponse;
import com.example.banking.statement.api.dto.StatementResponseMapper;
import com.example.banking.statement.domain.MonthlyStatementEntity;
import com.example.banking.statement.domain.StatementLineItemEntity;
import com.example.banking.statement.infrastructure.MonthlyStatementRepository;
import com.example.banking.statement.infrastructure.StatementLineItemRepository;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ListMonthlyStatementsService {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final BankAccountRepository bankAccountRepository;
    private final MonthlyStatementRepository monthlyStatementRepository;
    private final StatementLineItemRepository statementLineItemRepository;
    private final StatementResponseMapper statementResponseMapper;

    public ListMonthlyStatementsService(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            BankAccountRepository bankAccountRepository,
            MonthlyStatementRepository monthlyStatementRepository,
            StatementLineItemRepository statementLineItemRepository,
            StatementResponseMapper statementResponseMapper
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.bankAccountRepository = bankAccountRepository;
        this.monthlyStatementRepository = monthlyStatementRepository;
        this.statementLineItemRepository = statementLineItemRepository;
        this.statementResponseMapper = statementResponseMapper;
    }

    public MonthlyStatementListResponse list(String accountId, String actorId, String correlationId) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);

        BankAccountEntity account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new StandingOrderDomainException.StatementUnavailableException(
                        "Cannot retrieve statements because the requested account is unavailable."));

        if (!account.getCustomerId().equals(resolvedActorId)) {
            throw new StandingOrderDomainException.AccessForbiddenException();
        }

        List<MonthlyStatementEntity> generatedStatements = monthlyStatementRepository
                .findByAccountIdAndCustomerIdOrderByStatementMonthDescGeneratedAtDesc(accountId, resolvedActorId);

        if (generatedStatements.isEmpty()) {
            throw new StandingOrderDomainException.StatementUnavailableException(
                    "No generated statements found for this account. Generate at least one statement first.");
        }

        // Keep the latest statement per month and drop older duplicates if legacy data exists.
        Map<String, MonthlyStatementEntity> latestByMonth = new LinkedHashMap<>();
        for (MonthlyStatementEntity statement : generatedStatements) {
            latestByMonth.putIfAbsent(statement.getStatementMonth(), statement);
        }

        List<MonthlyStatementResponse> statements = new ArrayList<>();
        for (MonthlyStatementEntity statement : latestByMonth.values()) {
            List<StatementLineItemEntity> lineItems = statementLineItemRepository
                    .findByMonthlyStatementIdOrderByPostedAtAscTransactionIdAsc(statement.getMonthlyStatementId());
            statements.add(statementResponseMapper.toMonthlyStatement(statement, lineItems));
        }

        return statementResponseMapper.toListResponse(statements, correlationId);
    }
}

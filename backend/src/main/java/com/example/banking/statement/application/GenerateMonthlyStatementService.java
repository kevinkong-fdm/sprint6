package com.example.banking.statement.application;

import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import com.example.banking.statement.api.dto.GenerateMonthlyStatementRequest;
import com.example.banking.statement.api.dto.MonthlyStatementSingleResponse;
import com.example.banking.statement.api.dto.StatementResponseMapper;
import com.example.banking.statement.domain.MonthlyStatementEntity;
import com.example.banking.statement.domain.StatementLineItemEntity;
import com.example.banking.statement.infrastructure.MonthlyStatementRepository;
import com.example.banking.statement.infrastructure.StatementLineItemRepository;
import com.example.banking.standingorder.application.PlatformTimezoneService;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenerateMonthlyStatementService {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final BankAccountRepository bankAccountRepository;
    private final PlatformTimezoneService platformTimezoneService;
    private final StatementAggregationService statementAggregationService;
    private final MonthlyStatementRepository monthlyStatementRepository;
    private final StatementLineItemRepository statementLineItemRepository;
    private final StatementResponseMapper statementResponseMapper;

    public GenerateMonthlyStatementService(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            BankAccountRepository bankAccountRepository,
            PlatformTimezoneService platformTimezoneService,
            StatementAggregationService statementAggregationService,
            MonthlyStatementRepository monthlyStatementRepository,
            StatementLineItemRepository statementLineItemRepository,
            StatementResponseMapper statementResponseMapper
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.bankAccountRepository = bankAccountRepository;
        this.platformTimezoneService = platformTimezoneService;
        this.statementAggregationService = statementAggregationService;
        this.monthlyStatementRepository = monthlyStatementRepository;
        this.statementLineItemRepository = statementLineItemRepository;
        this.statementResponseMapper = statementResponseMapper;
    }

    @Transactional
    public MonthlyStatementSingleResponse generate(
            GenerateMonthlyStatementRequest request,
            String actorId,
            String correlationId
    ) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);

        YearMonth month = platformTimezoneService.parseMonth(request.month());
        if (month == null) {
            throw new StandingOrderDomainException.StatementValidationException(
                    "Month must use YYYY-MM format for monthly statement generation.");
        }

        YearMonth currentMonth = YearMonth.from(ZonedDateTime.now(platformTimezoneService.zoneId()));
        if (month.isAfter(currentMonth)) {
            throw new StandingOrderDomainException.StatementValidationException(
                    "Cannot generate a monthly statement for a future month.");
        }

        BankAccountEntity account = bankAccountRepository.findById(request.accountId())
                .orElseThrow(() -> new StandingOrderDomainException.StatementUnavailableException(
                        "Cannot generate statement because the requested account is unavailable."));

        if (!account.getCustomerId().equals(resolvedActorId)) {
            throw new StandingOrderDomainException.AccessForbiddenException();
        }

        StatementAggregationService.StatementAggregationResult aggregation = statementAggregationService.aggregate(
                account.getAccountId(),
                month);

        MonthlyStatementEntity existing = monthlyStatementRepository
                .findByAccountIdAndCustomerIdAndStatementMonth(account.getAccountId(), resolvedActorId, request.month())
                .orElse(null);

        MonthlyStatementEntity statement = monthlyStatementRepository.save(MonthlyStatementEntity.createOrReplace(
                existing == null ? null : existing.getMonthlyStatementId(),
                account.getAccountId(),
                resolvedActorId,
                request.month(),
                platformTimezoneService.timezoneCode(),
                aggregation.openingBalance(),
                aggregation.closingBalance(),
                aggregation.totalDebits(),
                aggregation.totalCredits(),
                aggregation.lineItems().size()));

        statementLineItemRepository.deleteByMonthlyStatementId(statement.getMonthlyStatementId());

        List<StatementLineItemEntity> lineItems = statementResponseMapper.toLineItemEntities(
                statement.getMonthlyStatementId(),
                aggregation.lineItems());
        List<StatementLineItemEntity> savedItems = statementLineItemRepository.saveAll(lineItems);

        return statementResponseMapper.toResponse(statement, savedItems, correlationId);
    }
}

package com.example.banking.statement.api.dto;

import com.example.banking.statement.application.StatementAggregationService;
import com.example.banking.statement.domain.MonthlyStatementEntity;
import com.example.banking.statement.domain.StatementLineItemEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StatementResponseMapper {

    public MonthlyStatementSingleResponse toResponse(
            MonthlyStatementEntity statement,
            List<StatementLineItemEntity> lineItems,
            String correlationId
    ) {
        return new MonthlyStatementSingleResponse(
                correlationId,
                Instant.now(),
            toMonthlyStatement(statement, lineItems));
        }

        public MonthlyStatementListResponse toListResponse(
            List<MonthlyStatementResponse> statements,
            String correlationId
        ) {
        return new MonthlyStatementListResponse(
            correlationId,
            Instant.now(),
            statements);
        }

        public MonthlyStatementResponse toMonthlyStatement(
            MonthlyStatementEntity statement,
            List<StatementLineItemEntity> lineItems
        ) {
        List<StatementLineItemResponse> mappedItems = new ArrayList<>();
        for (StatementLineItemEntity item : lineItems) {
            mappedItems.add(new StatementLineItemResponse(
                item.getTransactionId(),
                item.getPostedAt(),
                item.getEntryType().name(),
                asMoney(item.getAmount()),
                asMoney(item.getBalanceAfter()),
                item.getDescription()));
        }

        return new MonthlyStatementResponse(
            statement.getAccountId(),
            statement.getCustomerId(),
            statement.getStatementMonth(),
            statement.getTimezoneCode(),
            asMoney(statement.getOpeningBalance()),
            asMoney(statement.getClosingBalance()),
            asMoney(statement.getTotalDebits()),
            asMoney(statement.getTotalCredits()),
            mappedItems,
            statement.getGeneratedAt());
    }

    public List<StatementLineItemEntity> toLineItemEntities(
            String monthlyStatementId,
            List<StatementAggregationService.StatementLineDraft> lineDrafts
    ) {
        List<StatementLineItemEntity> entities = new ArrayList<>();
        for (StatementAggregationService.StatementLineDraft lineDraft : lineDrafts) {
            entities.add(StatementLineItemEntity.create(
                    monthlyStatementId,
                    lineDraft.transactionId(),
                    lineDraft.postedAt(),
                    lineDraft.entryType(),
                    lineDraft.amount(),
                    lineDraft.balanceAfter(),
                    lineDraft.description()));
        }
        return entities;
    }

    private String asMoney(BigDecimal amount) {
        if (amount == null) {
            return "0.0000";
        }
        return amount.setScale(4, RoundingMode.HALF_UP).toPlainString();
    }
}

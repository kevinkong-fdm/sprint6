package com.example.banking.statement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "statement_line_item")
public class StatementLineItemEntity {

    @Id
    @Column(name = "statement_line_item_id", nullable = false, columnDefinition = "CHAR(36)")
    private String statementLineItemId;

    @Column(name = "monthly_statement_id", nullable = false, columnDefinition = "CHAR(36)")
    private String monthlyStatementId;

    @Column(name = "transaction_id", nullable = false, columnDefinition = "CHAR(36)")
    private String transactionId;

    @Column(name = "posted_at", nullable = false)
    private Instant postedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 16)
    private StatementEntryType entryType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(name = "description", nullable = false, length = 200)
    private String description;

    public static StatementLineItemEntity create(
            String monthlyStatementId,
            String transactionId,
            Instant postedAt,
            StatementEntryType entryType,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String description
    ) {
        StatementLineItemEntity entity = new StatementLineItemEntity();
        entity.statementLineItemId = UUID.randomUUID().toString();
        entity.monthlyStatementId = normalizeRequired(monthlyStatementId);
        entity.transactionId = normalizeRequired(transactionId);
        entity.postedAt = postedAt == null ? Instant.now() : postedAt;
        entity.entryType = entryType;
        entity.amount = scale(amount);
        entity.balanceAfter = scale(balanceAfter);
        entity.description = normalizeRequired(description);
        return entity;
    }

    public String getStatementLineItemId() {
        return statementLineItemId;
    }

    public String getMonthlyStatementId() {
        return monthlyStatementId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public StatementEntryType getEntryType() {
        return entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    private static String normalizeRequired(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(4);
        }
        return value.setScale(4, java.math.RoundingMode.HALF_UP);
    }
}

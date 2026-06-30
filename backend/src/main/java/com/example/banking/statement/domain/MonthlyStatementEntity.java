package com.example.banking.statement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monthly_statement")
public class MonthlyStatementEntity {

    @Id
    @Column(name = "monthly_statement_id", nullable = false, columnDefinition = "CHAR(36)")
    private String monthlyStatementId;

    @Column(name = "account_id", nullable = false, columnDefinition = "CHAR(36)")
    private String accountId;

    @Column(name = "customer_id", nullable = false, columnDefinition = "CHAR(36)")
    private String customerId;

    @Column(name = "statement_month", nullable = false, columnDefinition = "CHAR(7)")
    private String statementMonth;

    @Column(name = "timezone_code", nullable = false, length = 16)
    private String timezoneCode;

    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingBalance;

    @Column(name = "closing_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal closingBalance;

    @Column(name = "total_debits", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalDebits;

    @Column(name = "total_credits", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalCredits;

    @Column(name = "line_item_count", nullable = false)
    private int lineItemCount;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    public static MonthlyStatementEntity createOrReplace(
            String existingId,
            String accountId,
            String customerId,
            String statementMonth,
            String timezoneCode,
            BigDecimal openingBalance,
            BigDecimal closingBalance,
            BigDecimal totalDebits,
            BigDecimal totalCredits,
            int lineItemCount
    ) {
        MonthlyStatementEntity entity = new MonthlyStatementEntity();
        entity.monthlyStatementId = existingId == null || existingId.isBlank() ? UUID.randomUUID().toString() : existingId.trim();
        entity.accountId = normalizeRequired(accountId);
        entity.customerId = normalizeRequired(customerId);
        entity.statementMonth = normalizeRequired(statementMonth);
        entity.timezoneCode = normalizeRequired(timezoneCode);
        entity.openingBalance = scale(openingBalance);
        entity.closingBalance = scale(closingBalance);
        entity.totalDebits = scale(totalDebits);
        entity.totalCredits = scale(totalCredits);
        entity.lineItemCount = Math.max(0, lineItemCount);
        entity.generatedAt = Instant.now();
        return entity;
    }

    public String getMonthlyStatementId() {
        return monthlyStatementId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getStatementMonth() {
        return statementMonth;
    }

    public String getTimezoneCode() {
        return timezoneCode;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public BigDecimal getTotalDebits() {
        return totalDebits;
    }

    public BigDecimal getTotalCredits() {
        return totalCredits;
    }

    public int getLineItemCount() {
        return lineItemCount;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
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

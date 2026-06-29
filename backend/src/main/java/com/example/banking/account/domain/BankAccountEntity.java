package com.example.banking.account.domain;

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
@Table(name = "bank_account")
public class BankAccountEntity {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(4);

    @Id
    @Column(name = "account_id", nullable = false, columnDefinition = "CHAR(36)")
    private String accountId;

    @Column(name = "customer_id", nullable = false, columnDefinition = "CHAR(36)")
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 16)
    private AccountType accountType;

    @Column(name = "nickname", length = 80)
    private String nickname;

    @Column(name = "currency_code", nullable = false, columnDefinition = "CHAR(3)")
    private String currencyCode;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal availableBalance;

    @Column(name = "ledger_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal ledgerBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static BankAccountEntity create(String customerId, AccountType accountType, String nickname) {
        Instant now = Instant.now();

        BankAccountEntity entity = new BankAccountEntity();
        entity.accountId = UUID.randomUUID().toString();
        entity.customerId = normalizeRequired(customerId);
        entity.accountType = accountType;
        entity.nickname = normalizeNullable(nickname);
        entity.currencyCode = "USD";
        entity.availableBalance = ZERO;
        entity.ledgerBalance = ZERO;
        entity.status = AccountStatus.ACTIVE;
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    public void updateNickname(String nickname) {
        this.nickname = normalizeNullable(nickname);
        this.updatedAt = Instant.now();
    }

    public void credit(BigDecimal amount) {
        this.availableBalance = this.availableBalance.add(scale(amount));
        this.ledgerBalance = this.ledgerBalance.add(scale(amount));
        this.updatedAt = Instant.now();
    }

    public void debit(BigDecimal amount) {
        this.availableBalance = this.availableBalance.subtract(scale(amount));
        this.ledgerBalance = this.ledgerBalance.subtract(scale(amount));
        this.updatedAt = Instant.now();
    }

    public boolean canDebit(BigDecimal amount) {
        return this.availableBalance.compareTo(scale(amount)) >= 0;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getLedgerBalance() {
        return ledgerBalance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(4, java.math.RoundingMode.HALF_UP);
    }

    private static String normalizeRequired(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}

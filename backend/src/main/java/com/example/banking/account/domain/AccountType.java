package com.example.banking.account.domain;

public enum AccountType {
    CHECKING,
    SAVINGS;

    public static AccountType from(String value) {
        if (value == null) {
            return null;
        }
        try {
            return AccountType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

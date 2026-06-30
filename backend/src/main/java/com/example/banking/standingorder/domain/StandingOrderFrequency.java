package com.example.banking.standingorder.domain;

public enum StandingOrderFrequency {
    DAILY,
    WEEKLY,
    MONTHLY;

    public static StandingOrderFrequency from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return StandingOrderFrequency.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

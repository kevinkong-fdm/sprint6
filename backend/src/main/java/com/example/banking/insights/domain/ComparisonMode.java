package com.example.banking.insights.domain;

public enum ComparisonMode {
    PREVIOUS_PERIOD,
    NONE;

    public static ComparisonMode from(String value) {
        if (value == null || value.isBlank()) {
            return PREVIOUS_PERIOD;
        }
        try {
            return ComparisonMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

package com.example.banking.insights.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ComparisonModeTest {

    @Test
    void shouldParseComparisonModeValues() {
        assertEquals(ComparisonMode.PREVIOUS_PERIOD, ComparisonMode.from(null));
        assertEquals(ComparisonMode.PREVIOUS_PERIOD, ComparisonMode.from(" "));
        assertEquals(ComparisonMode.PREVIOUS_PERIOD, ComparisonMode.from("previous_period"));
        assertEquals(ComparisonMode.NONE, ComparisonMode.from("none"));
        assertNull(ComparisonMode.from("invalid"));
    }
}

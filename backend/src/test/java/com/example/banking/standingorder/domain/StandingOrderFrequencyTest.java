package com.example.banking.standingorder.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class StandingOrderFrequencyTest {

    @Test
    void shouldParseFrequencyValues() {
        assertEquals(StandingOrderFrequency.DAILY, StandingOrderFrequency.from("daily"));
        assertEquals(StandingOrderFrequency.WEEKLY, StandingOrderFrequency.from(" WEEKLY "));
        assertEquals(StandingOrderFrequency.MONTHLY, StandingOrderFrequency.from("MONTHLY"));
        assertNull(StandingOrderFrequency.from(""));
        assertNull(StandingOrderFrequency.from("INVALID"));
        assertNull(StandingOrderFrequency.from(null));
    }
}

package com.example.banking.auth.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class PasswordResetThrottleCounterEntityTest {

    @Test
    void shouldTrackThrottleWindowAndRequests() {
        PasswordResetThrottleCounterEntity entity = PasswordResetThrottleCounterEntity.create("alice@example.com");

        assertEquals(0, entity.getRequestCount());
        assertNotNull(entity.getWindowStartedAt());

        entity.increment();
        entity.increment();
        assertEquals(2, entity.getRequestCount());

        entity.resetWindow();
        assertEquals(0, entity.getRequestCount());
        assertNotNull(entity.getWindowStartedAt());
    }
}

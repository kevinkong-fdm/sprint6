package com.example.banking.auth.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class LoginAttemptCounterEntityTest {

    @Test
    void shouldTrackFailuresAndLockoutWindow() {
        LoginAttemptCounterEntity entity = LoginAttemptCounterEntity.create("alice@example.com");

        assertEquals(0, entity.getFailedCount());
        assertNotNull(entity.getWindowStartedAt());
        assertNull(entity.getLockoutUntil());

        entity.recordFailure();
        entity.recordFailure();
        assertEquals(2, entity.getFailedCount());

        entity.lockForMinutes(15);
        assertNotNull(entity.getLockoutUntil());

        entity.clear();
        assertEquals(0, entity.getFailedCount());
        assertNull(entity.getLockoutUntil());
        assertNotNull(entity.getWindowStartedAt());
    }
}

package com.example.banking.auth.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PasswordResetRequestEntityTest {

    @Test
    void shouldCreatePasswordResetRequestWithExpiry() {
        PasswordResetRequestEntity entity = PasswordResetRequestEntity.create(
                "user-1",
                "token-hash",
                "127.0.0.1",
                "agent");

        Instant requestedAt = (Instant) ReflectionTestUtils.getField(entity, "requestedAt");
        Instant expiresAt = (Instant) ReflectionTestUtils.getField(entity, "expiresAt");

        assertNotNull(ReflectionTestUtils.getField(entity, "id"));
        assertEquals("user-1", ReflectionTestUtils.getField(entity, "userId"));
        assertEquals("token-hash", ReflectionTestUtils.getField(entity, "tokenHash"));
        assertEquals("127.0.0.1", ReflectionTestUtils.getField(entity, "requestIp"));
        assertEquals("agent", ReflectionTestUtils.getField(entity, "requestUserAgent"));
        assertNotNull(requestedAt);
        assertNotNull(expiresAt);
        assertTrue(expiresAt.isAfter(requestedAt));
    }
}

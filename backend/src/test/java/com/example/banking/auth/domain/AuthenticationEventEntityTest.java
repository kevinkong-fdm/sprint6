package com.example.banking.auth.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AuthenticationEventEntityTest {

    @Test
    void shouldCreateAuthenticationEventAndDefaultMetadata() {
        AuthenticationEventEntity entity = AuthenticationEventEntity.of(
                "user-1",
                "LOGIN",
                "SUCCESS",
                null,
                "corr-1",
                null);

        assertNotNull(ReflectionTestUtils.getField(entity, "id"));
        assertEquals("user-1", ReflectionTestUtils.getField(entity, "userId"));
        assertEquals("LOGIN", ReflectionTestUtils.getField(entity, "eventType"));
        assertEquals("SUCCESS", ReflectionTestUtils.getField(entity, "outcome"));
        assertEquals("corr-1", ReflectionTestUtils.getField(entity, "correlationId"));
        assertEquals("{}", ReflectionTestUtils.getField(entity, "metadataJson"));
    }
}

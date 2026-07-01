package com.example.banking.auth.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CredentialRecordEntityTest {

    @Test
    void shouldCreateCredentialRecordWithDefaults() {
        CredentialRecordEntity entity = CredentialRecordEntity.of("user-1", "hash-1");

        assertEquals("user-1", entity.getUserId());
        assertEquals("hash-1", ReflectionTestUtils.getField(entity, "passwordHash"));
        assertEquals("v1", ReflectionTestUtils.getField(entity, "policyVersion"));
        assertNotNull(ReflectionTestUtils.getField(entity, "id"));
        assertNotNull(ReflectionTestUtils.getField(entity, "passwordUpdatedAt"));
    }
}

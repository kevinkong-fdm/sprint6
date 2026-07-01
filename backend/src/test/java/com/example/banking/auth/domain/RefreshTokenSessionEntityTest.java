package com.example.banking.auth.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class RefreshTokenSessionEntityTest {

    @Test
    void shouldIssueTokenSession() {
        RefreshTokenSessionEntity session = RefreshTokenSessionEntity.issue("user-1", "hash-1", null, null);

        assertNotNull(session.getId());
        assertEquals("user-1", session.getUserId());
        assertEquals("hash-1", session.getTokenHash());
        assertNotNull(session.getFamilyId());
        assertNotNull(session.getExpiresAt());
    }

    @Test
    void shouldCarryForwardFamilyAndPreviousTokenAndAllowRevoke() {
        RefreshTokenSessionEntity previous = RefreshTokenSessionEntity.issue("user-1", "hash-1", null, null);
        RefreshTokenSessionEntity rotated = RefreshTokenSessionEntity.issue(
                "user-1",
                "hash-2",
                previous.getId(),
                previous.getFamilyId());

        assertEquals(previous.getId(), rotated.getPreviousTokenId());
        assertEquals(previous.getFamilyId(), rotated.getFamilyId());

        rotated.revoke("rotated");
        assertNotNull(rotated.getRevokedAt());
    }
}

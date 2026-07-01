package com.example.banking.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.banking.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    @Test
    void shouldIssueAndParseAccessAndRefreshTokens() {
        JwtProperties properties = new JwtProperties(
                "banking-auth",
                "short-secret",
                900,
                3_600);
        JwtTokenService service = new JwtTokenService(properties);

        JwtTokenService.TokenPair pair = service.issueTokenPair("user-1", "alice@example.com");

        Claims accessClaims = service.parse(pair.accessToken());
        Claims refreshClaims = service.parse(pair.refreshToken());

        assertEquals("Bearer", pair.tokenType());
        assertEquals("user-1", accessClaims.getSubject());
        assertEquals("alice@example.com", accessClaims.get("email", String.class));
        assertEquals("access", accessClaims.get("tokenType", String.class));
        assertEquals("refresh", refreshClaims.get("tokenType", String.class));
        assertEquals(900, pair.accessTokenExpiresInSeconds());
        assertEquals(3_600, pair.refreshTokenExpiresInSeconds());
    }

    @Test
    void shouldHashTokensDeterministically() {
        JwtTokenService service = new JwtTokenService(new JwtProperties("issuer", "another-secret", 60, 120));

        String hashA = service.tokenHash("token-A");
        String hashA2 = service.tokenHash("token-A");
        String hashB = service.tokenHash("token-B");

        assertEquals(hashA, hashA2);
        assertNotEquals(hashA, hashB);
    }

    @Test
    void shouldRejectMalformedToken() {
        JwtTokenService service = new JwtTokenService(new JwtProperties("issuer", "secret-for-tests", 60, 120));

        assertThrows(RuntimeException.class, () -> service.parse("not-a-jwt"));
    }
}

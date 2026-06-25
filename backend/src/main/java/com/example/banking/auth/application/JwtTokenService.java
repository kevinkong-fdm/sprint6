package com.example.banking.auth.application;

import com.example.banking.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(normalizeSecret(properties.secret()));
    }

    public TokenPair issueTokenPair(String userId, String email) {
        String accessToken = createToken(userId, email, properties.accessTokenTtlSeconds(), "access");
        String refreshToken = createToken(userId, email, properties.refreshTokenTtlSeconds(), "refresh");
        return new TokenPair(
                "Bearer",
                accessToken,
                refreshToken,
                properties.accessTokenTtlSeconds(),
                properties.refreshTokenTtlSeconds()
        );
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }

    public String tokenHash(String token) {
        return UUID.nameUUIDFromBytes(token.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private String createToken(String userId, String email, long ttlSeconds, String tokenType) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(userId)
                .claims(Map.of("email", email, "tokenType", tokenType))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(signingKey)
                .compact();
    }

    private byte[] normalizeSecret(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= 32) {
            return bytes;
        }
        byte[] expanded = new byte[32];
        System.arraycopy(bytes, 0, expanded, 0, bytes.length);
        for (int i = bytes.length; i < expanded.length; i++) {
            expanded[i] = (byte) ('a' + (i % 26));
        }
        return expanded;
    }

    public record TokenPair(
            String tokenType,
            String accessToken,
            String refreshToken,
            long accessTokenExpiresInSeconds,
            long refreshTokenExpiresInSeconds
    ) {
    }
}

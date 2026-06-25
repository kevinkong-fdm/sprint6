package com.example.banking.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long accessTokenTtlSeconds,
        long refreshTokenTtlSeconds
) {
}

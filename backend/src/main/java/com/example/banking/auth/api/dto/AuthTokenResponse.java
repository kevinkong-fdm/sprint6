package com.example.banking.auth.api.dto;

import com.example.banking.auth.application.JwtTokenService;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds,
        long refreshTokenExpiresInSeconds
) {
    public static AuthTokenResponse from(JwtTokenService.TokenPair pair) {
        return new AuthTokenResponse(
                pair.tokenType(),
                pair.accessToken(),
                pair.refreshToken(),
                pair.accessTokenExpiresInSeconds(),
                pair.refreshTokenExpiresInSeconds()
        );
    }
}

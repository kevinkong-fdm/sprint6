package com.example.banking.auth.api.dto;

import com.example.banking.auth.application.JwtTokenService;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds,
        long refreshTokenExpiresInSeconds,
        String userId
) {
    public static AuthTokenResponse from(JwtTokenService.TokenPair pair, String userId) {
        return new AuthTokenResponse(
                pair.tokenType(),
                pair.accessToken(),
                pair.refreshToken(),
                pair.accessTokenExpiresInSeconds(),
                pair.refreshTokenExpiresInSeconds(),
                userId
        );
    }
}

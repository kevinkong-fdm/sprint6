package com.example.banking.auth.api;

import com.example.banking.auth.api.dto.AuthTokenResponse;
import com.example.banking.auth.api.dto.TokenRefreshRequest;
import com.example.banking.auth.application.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/token")
public class TokenRefreshController {

    private final RefreshTokenService refreshTokenService;

    public TokenRefreshController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(
            @Valid @RequestBody TokenRefreshRequest request,
            HttpServletRequest servletRequest
    ) {
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        return refreshTokenService.refresh(request, correlationId);
    }
}

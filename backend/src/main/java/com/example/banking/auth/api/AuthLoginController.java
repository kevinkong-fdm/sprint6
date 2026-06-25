package com.example.banking.auth.api;

import com.example.banking.auth.api.dto.AuthTokenResponse;
import com.example.banking.auth.api.dto.LoginRequest;
import com.example.banking.auth.application.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthLoginController {

    private final LoginService loginService;

    public AuthLoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        return loginService.login(request, correlationId);
    }
}

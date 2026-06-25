package com.example.banking.auth.api;

import com.example.banking.auth.api.dto.AcceptedResponse;
import com.example.banking.auth.api.dto.PasswordResetRequest;
import com.example.banking.auth.application.PasswordResetRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/password-reset")
public class PasswordResetController {

    private final PasswordResetRequestService passwordResetRequestService;

    public PasswordResetController(PasswordResetRequestService passwordResetRequestService) {
        this.passwordResetRequestService = passwordResetRequestService;
    }

    @PostMapping("/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AcceptedResponse requestReset(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest servletRequest
    ) {
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        return passwordResetRequestService.requestReset(
                request,
                correlationId,
                servletRequest.getRemoteAddr(),
                servletRequest.getHeader("User-Agent")
        );
    }
}

package com.example.banking.account.api;

import com.example.banking.account.api.dto.TransferRequest;
import com.example.banking.account.api.dto.TransferResponse;
import com.example.banking.account.application.AccountAuthorizationService;
import com.example.banking.account.application.TransferFundsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final AccountAuthorizationService accountAuthorizationService;
    private final TransferFundsService transferFundsService;

    public TransferController(
            AccountAuthorizationService accountAuthorizationService,
            TransferFundsService transferFundsService
    ) {
        this.accountAuthorizationService = accountAuthorizationService;
        this.transferFundsService = transferFundsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = accountAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        return transferFundsService.transfer(request, correlationId, actorId);
    }
}

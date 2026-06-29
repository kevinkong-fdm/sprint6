package com.example.banking.account.api;

import com.example.banking.account.api.dto.DepositRequest;
import com.example.banking.account.api.dto.MovementResponse;
import com.example.banking.account.api.dto.TransactionHistoryResponse;
import com.example.banking.account.api.dto.WithdrawalRequest;
import com.example.banking.account.application.AccountAuthorizationService;
import com.example.banking.account.application.AccountMovementService;
import com.example.banking.account.application.GetTransactionHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts/{accountId}")
public class AccountMovementController {

    private final AccountAuthorizationService accountAuthorizationService;
    private final AccountMovementService accountMovementService;
    private final GetTransactionHistoryService getTransactionHistoryService;

    public AccountMovementController(
            AccountAuthorizationService accountAuthorizationService,
            AccountMovementService accountMovementService,
            GetTransactionHistoryService getTransactionHistoryService
    ) {
        this.accountAuthorizationService = accountAuthorizationService;
        this.accountMovementService = accountMovementService;
        this.getTransactionHistoryService = getTransactionHistoryService;
    }

    @PostMapping("/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    public MovementResponse deposit(
            @PathVariable String accountId,
            @Valid @RequestBody DepositRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = accountAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        return accountMovementService.deposit(accountId, request, correlationId, actorId);
    }

    @PostMapping("/withdrawals")
    @ResponseStatus(HttpStatus.CREATED)
    public MovementResponse withdraw(
            @PathVariable String accountId,
            @Valid @RequestBody WithdrawalRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = accountAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        return accountMovementService.withdraw(accountId, request, correlationId, actorId);
    }

    @GetMapping("/transactions")
    public TransactionHistoryResponse history(
            @PathVariable String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication
    ) {
        String actorId = accountAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        return getTransactionHistoryService.history(accountId, actorId, from, to, type, page, size);
    }
}

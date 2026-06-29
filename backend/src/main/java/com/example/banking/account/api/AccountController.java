package com.example.banking.account.api;

import com.example.banking.account.api.dto.AccountListResponse;
import com.example.banking.account.api.dto.AccountResponse;
import com.example.banking.account.api.dto.CreateAccountRequest;
import com.example.banking.account.api.dto.DeleteAccountRequest;
import com.example.banking.account.api.dto.UpdateAccountRequest;
import com.example.banking.account.application.AccountAuthorizationService;
import com.example.banking.account.application.CreateAccountService;
import com.example.banking.account.application.DeleteAccountWorkflowService;
import com.example.banking.account.application.GetAccountService;
import com.example.banking.account.application.ListAccountsService;
import com.example.banking.account.application.UpdateAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountAuthorizationService accountAuthorizationService;
    private final CreateAccountService createAccountService;
    private final ListAccountsService listAccountsService;
    private final GetAccountService getAccountService;
    private final UpdateAccountService updateAccountService;
    private final DeleteAccountWorkflowService deleteAccountWorkflowService;

    public AccountController(
            AccountAuthorizationService accountAuthorizationService,
            CreateAccountService createAccountService,
            ListAccountsService listAccountsService,
            GetAccountService getAccountService,
            UpdateAccountService updateAccountService,
            DeleteAccountWorkflowService deleteAccountWorkflowService
    ) {
        this.accountAuthorizationService = accountAuthorizationService;
        this.createAccountService = createAccountService;
        this.listAccountsService = listAccountsService;
        this.getAccountService = getAccountService;
        this.updateAccountService = updateAccountService;
        this.deleteAccountWorkflowService = deleteAccountWorkflowService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(
            @Valid @RequestBody CreateAccountRequest request,
            Authentication authentication
    ) {
        String actorId = accountAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        return createAccountService.create(request, actorId);
    }

    @GetMapping
    public AccountListResponse list(
            @RequestParam(required = false) String accountType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        String actorId = accountAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        return listAccountsService.list(actorId, accountType, page, size);
    }

    @GetMapping("/{accountId}")
    public AccountResponse get(
            @PathVariable String accountId,
            Authentication authentication
    ) {
        String actorId = accountAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        return getAccountService.get(accountId, actorId);
    }

    @PatchMapping("/{accountId}")
    public AccountResponse update(
            @PathVariable String accountId,
            @Valid @RequestBody UpdateAccountRequest request,
            Authentication authentication
    ) {
        String actorId = accountAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        return updateAccountService.update(accountId, request, actorId);
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String accountId,
            @Valid @RequestBody(required = false) DeleteAccountRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = accountAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        DeleteAccountRequest safeRequest = request == null ? new DeleteAccountRequest(null, null) : request;
        deleteAccountWorkflowService.delete(accountId, safeRequest, correlationId, actorId);
    }
}

package com.example.banking.statement.api;

import com.example.banking.statement.api.dto.GenerateMonthlyStatementRequest;
import com.example.banking.statement.api.dto.MonthlyStatementSingleResponse;
import com.example.banking.statement.application.GenerateMonthlyStatementService;
import com.example.banking.statement.application.GetMonthlyStatementService;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statements/monthly")
public class StatementController {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final GenerateMonthlyStatementService generateMonthlyStatementService;
    private final GetMonthlyStatementService getMonthlyStatementService;

    public StatementController(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            GenerateMonthlyStatementService generateMonthlyStatementService,
            GetMonthlyStatementService getMonthlyStatementService
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.generateMonthlyStatementService = generateMonthlyStatementService;
        this.getMonthlyStatementService = getMonthlyStatementService;
    }

    @PostMapping("/generate")
    public MonthlyStatementSingleResponse generate(
            @Valid @RequestBody GenerateMonthlyStatementRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        return generateMonthlyStatementService.generate(request, actorId, correlationId);
    }

    @GetMapping
    public MonthlyStatementSingleResponse get(
            @RequestParam String accountId,
            @RequestParam String month,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        return getMonthlyStatementService.get(accountId, month, actorId, correlationId);
    }
}

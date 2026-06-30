package com.example.banking.standingorder.api;

import com.example.banking.standingorder.api.dto.CreateStandingOrderRequest;
import com.example.banking.standingorder.api.dto.StandingOrderExecutionListResponse;
import com.example.banking.standingorder.api.dto.StandingOrderExecutionSingleResponse;
import com.example.banking.standingorder.api.dto.StandingOrderListResponse;
import com.example.banking.standingorder.api.dto.StandingOrderResponseMapper;
import com.example.banking.standingorder.api.dto.StandingOrderSingleResponse;
import com.example.banking.standingorder.api.dto.TriggerExecutionRequest;
import com.example.banking.standingorder.api.dto.UpdateStandingOrderRequest;
import com.example.banking.standingorder.application.CreateStandingOrderService;
import com.example.banking.standingorder.application.GetStandingOrderService;
import com.example.banking.standingorder.application.ListStandingOrdersService;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderExecutionService;
import com.example.banking.standingorder.application.StandingOrderLifecycleService;
import com.example.banking.standingorder.application.UpdateStandingOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/standing-orders")
public class StandingOrderController {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final CreateStandingOrderService createStandingOrderService;
    private final ListStandingOrdersService listStandingOrdersService;
    private final GetStandingOrderService getStandingOrderService;
    private final UpdateStandingOrderService updateStandingOrderService;
    private final StandingOrderLifecycleService standingOrderLifecycleService;
    private final StandingOrderExecutionService standingOrderExecutionService;
    private final StandingOrderResponseMapper standingOrderResponseMapper;

    public StandingOrderController(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            CreateStandingOrderService createStandingOrderService,
            ListStandingOrdersService listStandingOrdersService,
            GetStandingOrderService getStandingOrderService,
            UpdateStandingOrderService updateStandingOrderService,
            StandingOrderLifecycleService standingOrderLifecycleService,
            StandingOrderExecutionService standingOrderExecutionService,
            StandingOrderResponseMapper standingOrderResponseMapper
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.createStandingOrderService = createStandingOrderService;
        this.listStandingOrdersService = listStandingOrdersService;
        this.getStandingOrderService = getStandingOrderService;
        this.updateStandingOrderService = updateStandingOrderService;
        this.standingOrderLifecycleService = standingOrderLifecycleService;
        this.standingOrderExecutionService = standingOrderExecutionService;
        this.standingOrderResponseMapper = standingOrderResponseMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StandingOrderSingleResponse create(
            @Valid @RequestBody CreateStandingOrderRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");

        return standingOrderResponseMapper.toSingleResponse(
                createStandingOrderService.create(request, idempotencyKey, actorId, correlationId),
                correlationId);
    }

    @GetMapping
    public StandingOrderListResponse list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        return listStandingOrdersService.list(actorId, status, page, size, correlationId);
    }

    @GetMapping("/{standingOrderId}")
    public StandingOrderSingleResponse getById(
            @PathVariable String standingOrderId,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        return getStandingOrderService.getById(standingOrderId, actorId, correlationId);
    }

    @PatchMapping("/{standingOrderId}")
    public StandingOrderSingleResponse update(
            @PathVariable String standingOrderId,
            @Valid @RequestBody UpdateStandingOrderRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");

        return standingOrderResponseMapper.toSingleResponse(
                updateStandingOrderService.update(standingOrderId, request, actorId, correlationId),
                correlationId);
    }

    @PostMapping("/{standingOrderId}/pause")
    public StandingOrderSingleResponse pause(
            @PathVariable String standingOrderId,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");

        return standingOrderResponseMapper.toSingleResponse(
                standingOrderLifecycleService.pause(standingOrderId, actorId, correlationId),
                correlationId);
    }

    @PostMapping("/{standingOrderId}/resume")
    public StandingOrderSingleResponse resume(
            @PathVariable String standingOrderId,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");

        return standingOrderResponseMapper.toSingleResponse(
                standingOrderLifecycleService.resume(standingOrderId, actorId, correlationId),
                correlationId);
    }

    @PostMapping("/{standingOrderId}/cancel")
    public StandingOrderSingleResponse cancel(
            @PathVariable String standingOrderId,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");

        return standingOrderResponseMapper.toSingleResponse(
                standingOrderLifecycleService.cancel(standingOrderId, actorId, correlationId),
                correlationId);
    }

    @PostMapping("/{standingOrderId}/executions/trigger")
    @ResponseStatus(HttpStatus.CREATED)
    public StandingOrderExecutionSingleResponse trigger(
            @PathVariable String standingOrderId,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) TriggerExecutionRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");

        return standingOrderResponseMapper.toExecutionSingleResponse(
                standingOrderExecutionService.trigger(standingOrderId, idempotencyKey, actorId, correlationId),
                correlationId);
    }

    @GetMapping("/{standingOrderId}/executions")
    public StandingOrderExecutionListResponse listExecutions(
            @PathVariable String standingOrderId,
            @RequestParam(required = false) String outcome,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String actorId = standingOrderAuthorizationService.requireActorId(authentication == null ? null : authentication.getName());
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        return standingOrderExecutionService.listExecutions(standingOrderId, outcome, page, size, actorId, correlationId);
    }
}

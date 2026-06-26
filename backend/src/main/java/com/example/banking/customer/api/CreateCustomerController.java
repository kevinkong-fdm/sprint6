package com.example.banking.customer.api;

import com.example.banking.customer.api.dto.CustomerCreateRequest;
import com.example.banking.customer.api.dto.CustomerResponse;
import com.example.banking.customer.application.CreateCustomerService;
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
@RequestMapping("/customers")
public class CreateCustomerController {

    private final CreateCustomerService createCustomerService;

    public CreateCustomerController(CreateCustomerService createCustomerService) {
        this.createCustomerService = createCustomerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(
            @Valid @RequestBody CustomerCreateRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        String actorId = authentication == null ? "operator" : authentication.getName();
        return createCustomerService.create(request, correlationId, actorId);
    }
}

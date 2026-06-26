package com.example.banking.customer.api;

import com.example.banking.customer.api.dto.CustomerResponse;
import com.example.banking.customer.api.dto.CustomerUpdateRequest;
import com.example.banking.customer.application.UpdateCustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class UpdateCustomerController {

    private final UpdateCustomerService updateCustomerService;

    public UpdateCustomerController(UpdateCustomerService updateCustomerService) {
        this.updateCustomerService = updateCustomerService;
    }

    @PatchMapping("/{customerId}")
    public CustomerResponse update(
            @PathVariable String customerId,
            @Valid @RequestBody CustomerUpdateRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        String actorId = authentication == null ? "operator" : authentication.getName();
        return updateCustomerService.update(customerId, request, correlationId, actorId);
    }
}

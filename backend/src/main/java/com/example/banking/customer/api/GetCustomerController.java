package com.example.banking.customer.api;

import com.example.banking.customer.api.dto.CustomerResponse;
import com.example.banking.customer.application.GetCustomerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class GetCustomerController {

    private final GetCustomerService getCustomerService;

    public GetCustomerController(GetCustomerService getCustomerService) {
        this.getCustomerService = getCustomerService;
    }

    @GetMapping("/{customerId}")
    public CustomerResponse getById(
            @PathVariable String customerId,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        String actorId = authentication == null ? "operator" : authentication.getName();
        return getCustomerService.getById(customerId, correlationId, actorId);
    }
}

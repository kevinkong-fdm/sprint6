package com.example.banking.customer.api;

import com.example.banking.customer.application.DeleteCustomerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class DeleteCustomerController {

    private final DeleteCustomerService deleteCustomerService;

    public DeleteCustomerController(DeleteCustomerService deleteCustomerService) {
        this.deleteCustomerService = deleteCustomerService;
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String customerId,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        String correlationId = (String) servletRequest.getAttribute("correlationId");
        String actorId = authentication == null ? "operator" : authentication.getName();
        deleteCustomerService.delete(customerId, correlationId, actorId);
    }
}

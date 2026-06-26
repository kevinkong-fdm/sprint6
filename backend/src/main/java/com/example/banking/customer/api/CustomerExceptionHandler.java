package com.example.banking.customer.api;

import com.example.banking.customer.api.dto.CustomerErrorResponse;
import com.example.banking.customer.application.CustomerDomainException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.banking.customer")
public class CustomerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomerExceptionHandler.class);

    @ExceptionHandler(CustomerDomainException.class)
    public ResponseEntity<CustomerErrorResponse> handleDomain(CustomerDomainException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatus());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status)
                .body(CustomerErrorResponse.of(ex.getErrorCode(), ex.getMessage(), correlationId(request)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomerErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        if (message.isBlank()) {
            message = defaultValidationMessage(request);
        }

        return ResponseEntity.status(defaultValidationStatus(request))
                .body(CustomerErrorResponse.of(defaultValidationCode(request), message, correlationId(request)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomerErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.status(defaultValidationStatus(request))
                .body(CustomerErrorResponse.of(
                        defaultValidationCode(request),
                        defaultValidationMessage(request),
                        correlationId(request)));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CustomerErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        if (isCreateRequest(request)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CustomerErrorResponse.of(
                            "CUST-CRT-002",
                            "Duplicate customer identity attribute.",
                            correlationId(request)));
        }

        if (isDeleteRequest(request)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CustomerErrorResponse.of(
                            "CUST-DEL-002",
                            "Customer hard delete failed due to cascading dependency deletion error.",
                            correlationId(request)));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CustomerErrorResponse.of("CUST-UPD-001", "Customer update validation failed.", correlationId(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomerErrorResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
        log.error("Unhandled customer error. correlationId={}", correlationId(request), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomerErrorResponse.of(
                        "CUST-SYS-001",
                        "Unexpected server error",
                        correlationId(request)));
    }

    private boolean isCreateRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/customers");
    }

    private boolean isUpdateRequest(HttpServletRequest request) {
        return "PATCH".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/customers/");
    }

    private boolean isDeleteRequest(HttpServletRequest request) {
        return "DELETE".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/customers/");
    }

    private String defaultValidationCode(HttpServletRequest request) {
        if (isCreateRequest(request)) {
            return "CUST-CRT-001";
        }
        return "CUST-UPD-001";
    }

    private HttpStatus defaultValidationStatus(HttpServletRequest request) {
        if (isUpdateRequest(request)) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private String defaultValidationMessage(HttpServletRequest request) {
        if (isCreateRequest(request)) {
            return "Customer creation validation failed.";
        }
        return "Customer update validation failed.";
    }

    private String correlationId(HttpServletRequest request) {
        Object correlationId = request.getAttribute("correlationId");
        return correlationId == null ? "" : correlationId.toString();
    }
}

package com.example.banking.account.api;

import com.example.banking.account.api.dto.AccountErrorResponse;
import com.example.banking.account.application.AccountDomainException;
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

@RestControllerAdvice(basePackages = "com.example.banking.account")
public class AccountExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AccountExceptionHandler.class);

    @ExceptionHandler(AccountDomainException.class)
    public ResponseEntity<AccountErrorResponse> handleDomain(AccountDomainException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatus());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status)
                .body(AccountErrorResponse.of(ex.getErrorCode(), ex.getMessage(), correlationId(request)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AccountErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = defaultValidationMessage(request);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(AccountErrorResponse.of(defaultValidationCode(request), message, correlationId(request)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AccountErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(AccountErrorResponse.of(defaultValidationCode(request), defaultValidationMessage(request), correlationId(request)));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<AccountErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(AccountErrorResponse.of(defaultValidationCode(request), defaultValidationMessage(request), correlationId(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AccountErrorResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
        log.error("Unhandled account error. correlationId={}", correlationId(request), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AccountErrorResponse.of("ACCT-SYS-001", "Unexpected server error", correlationId(request)));
    }

    private String correlationId(HttpServletRequest request) {
        Object correlationId = request.getAttribute("correlationId");
        return correlationId == null ? "" : correlationId.toString();
    }

    private String defaultValidationCode(HttpServletRequest request) {
        if (isUpdateRequest(request)) {
            return "ACCT-UPD-001";
        }
        if (isDeleteRequest(request)) {
            return "ACCT-DEL-002";
        }
        if (isDepositRequest(request)) {
            return "TXN-DEP-001";
        }
        if (isWithdrawalRequest(request)) {
            return "TXN-WDR-001";
        }
        if (isTransferRequest(request)) {
            return "TXN-TRF-001";
        }
        if (isHistoryRequest(request)) {
            return "TXN-HIS-001";
        }
        return "ACCT-CRT-001";
    }

    private String defaultValidationMessage(HttpServletRequest request) {
        if (isUpdateRequest(request)) {
            return "Account update validation failed.";
        }
        if (isDeleteRequest(request)) {
            return "Account delete not allowed due to pending movement activity.";
        }
        if (isDepositRequest(request)) {
            return "Deposit amount validation failed.";
        }
        if (isWithdrawalRequest(request)) {
            return "Withdrawal amount validation failed.";
        }
        if (isTransferRequest(request)) {
            return "Transfer amount validation failed.";
        }
        if (isHistoryRequest(request)) {
            return "Transaction history filter validation failed.";
        }
        return "Account creation validation failed.";
    }

    private boolean isUpdateRequest(HttpServletRequest request) {
        return "PATCH".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/accounts/");
    }

    private boolean isDeleteRequest(HttpServletRequest request) {
        return "DELETE".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/accounts/");
    }

    private boolean isDepositRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().startsWith("/accounts/")
                && request.getRequestURI().endsWith("/deposits");
    }

    private boolean isWithdrawalRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().startsWith("/accounts/")
                && request.getRequestURI().endsWith("/withdrawals");
    }

    private boolean isTransferRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().equals("/transfers");
    }

    private boolean isHistoryRequest(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().startsWith("/accounts/")
                && request.getRequestURI().endsWith("/transactions");
    }
}

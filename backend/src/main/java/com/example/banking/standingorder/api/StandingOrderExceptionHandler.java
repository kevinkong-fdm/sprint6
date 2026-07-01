package com.example.banking.standingorder.api;

import com.example.banking.standingorder.api.dto.FeatureErrorResponse;
import com.example.banking.standingorder.application.StandingOrderDomainException;
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

@RestControllerAdvice(basePackages = {
        "com.example.banking.standingorder",
        "com.example.banking.statement",
        "com.example.banking.insights"
})
public class StandingOrderExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(StandingOrderExceptionHandler.class);

    @ExceptionHandler(StandingOrderDomainException.class)
    public ResponseEntity<FeatureErrorResponse> handleDomain(StandingOrderDomainException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatus());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status)
                .body(FeatureErrorResponse.of(ex.getErrorCode(), ex.getMessage(), correlationId(request)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FeatureErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        if (message.isBlank()) {
            message = defaultValidationMessage(request);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(FeatureErrorResponse.of(defaultValidationCode(request), message, correlationId(request)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<FeatureErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(FeatureErrorResponse.of(defaultValidationCode(request), defaultValidationMessage(request), correlationId(request)));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<FeatureErrorResponse> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(FeatureErrorResponse.of(defaultValidationCode(request), defaultValidationMessage(request), correlationId(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FeatureErrorResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
        log.error("Unhandled feature error. correlationId={}", correlationId(request), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FeatureErrorResponse.of("SYS-FEAT-001", "Unexpected feature service error.", correlationId(request)));
    }

    private String defaultValidationCode(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.startsWith("/standing-orders") && "POST".equalsIgnoreCase(request.getMethod())) {
            return "SO-SET-001";
        }
        if (uri.startsWith("/standing-orders") && "PATCH".equalsIgnoreCase(request.getMethod())) {
            return "SO-UPD-001";
        }
        if (uri.startsWith("/statements/monthly")) {
            return "STMT-001";
        }
        if (uri.startsWith("/insights/spending")) {
            return "INS-001";
        }
        return "SO-SET-001";
    }

    private String defaultValidationMessage(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.startsWith("/standing-orders") && "POST".equalsIgnoreCase(request.getMethod())) {
            return "Standing-order setup validation failed.";
        }
        if (uri.startsWith("/standing-orders") && "PATCH".equalsIgnoreCase(request.getMethod())) {
            return "Standing-order update validation failed.";
        }
        if (uri.startsWith("/statements/monthly")) {
            return "Monthly statement period validation failed.";
        }
        if (uri.startsWith("/insights/spending")) {
            return "Spending-insight filter validation failed.";
        }
        return "Standing-order setup validation failed.";
    }

    private String correlationId(HttpServletRequest request) {
        Object correlationId = request.getAttribute("correlationId");
        return correlationId == null ? "" : correlationId.toString();
    }
}

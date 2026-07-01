package com.example.banking.auth.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.banking.auth.application.DomainException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleDomainAndUnknownStatusFallback() {
        MockHttpServletRequest request = request("corr-1");

        ResponseEntity<ErrorResponse> known =
                handler.handleDomain(new DomainException("AUTH-LOGIN-001", "Bad login", 401), request);
        ResponseEntity<ErrorResponse> unknown =
                handler.handleDomain(new DomainException("AUTH-UNK-001", "Unknown", 999), request);

        assertEquals(HttpStatus.UNAUTHORIZED, known.getStatusCode());
        assertEquals("AUTH-LOGIN-001", known.getBody().errorCode());
        assertEquals("corr-1", known.getBody().correlationId());

        assertEquals(HttpStatus.BAD_REQUEST, unknown.getStatusCode());
        assertEquals("AUTH-UNK-001", unknown.getBody().errorCode());
    }

    @Test
    void shouldHandleValidationWithExplicitAndFallbackMessage() {
        BeanPropertyBindingResult withError = new BeanPropertyBindingResult(new Object(), "request");
        withError.addError(new FieldError("request", "email", "Email invalid"));
        MethodArgumentNotValidException explicit = mock(MethodArgumentNotValidException.class);
        when(explicit.getBindingResult()).thenReturn(withError);

        BeanPropertyBindingResult withoutErrors = new BeanPropertyBindingResult(new Object(), "request");
        MethodArgumentNotValidException fallback = mock(MethodArgumentNotValidException.class);
        when(fallback.getBindingResult()).thenReturn(withoutErrors);

        ResponseEntity<ErrorResponse> explicitResponse = handler.handleValidation(explicit, request("corr-2"));
        ResponseEntity<ErrorResponse> fallbackResponse = handler.handleValidation(fallback, request(null));

        assertEquals(HttpStatus.BAD_REQUEST, explicitResponse.getStatusCode());
        assertEquals("AUTH-REG-001", explicitResponse.getBody().errorCode());
        assertEquals("Email invalid", explicitResponse.getBody().message());

        assertEquals(HttpStatus.BAD_REQUEST, fallbackResponse.getStatusCode());
        assertEquals("Validation failed", fallbackResponse.getBody().message());
    }

    @Test
    void shouldHandleDataIntegrityAndUnhandled() {
        ResponseEntity<ErrorResponse> conflict =
                handler.handleDataIntegrity(new DataIntegrityViolationException("duplicate"), request("corr-3"));
        ResponseEntity<ErrorResponse> unhandled =
                handler.handleUnhandled(new RuntimeException("boom"), request("corr-4"));

        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        assertEquals("AUTH-REG-002", conflict.getBody().errorCode());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, unhandled.getStatusCode());
        assertNotNull(unhandled.getBody());
        assertEquals("AUTH-SYS-001", unhandled.getBody().errorCode());
        assertEquals("corr-4", unhandled.getBody().correlationId());
    }

    private MockHttpServletRequest request(String correlationId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (correlationId != null) {
            request.setAttribute("correlationId", correlationId);
        }
        return request;
    }
}

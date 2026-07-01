package com.example.banking.customer.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.banking.customer.api.dto.CustomerErrorResponse;
import com.example.banking.customer.application.CustomerDomainException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class CustomerExceptionHandlerTest {

    private final CustomerExceptionHandler handler = new CustomerExceptionHandler();

    @Test
    void shouldHandleDomainAndUnknownStatusFallback() {
        ResponseEntity<CustomerErrorResponse> known = handler.handleDomain(
                new CustomerDomainException.CreateValidationException("bad"),
                request("POST", "/customers", "corr-1"));

        ResponseEntity<CustomerErrorResponse> unknown =
                handler.handleDomain(new UnknownStatusException(), request("POST", "/customers", null));

        assertEquals(HttpStatus.BAD_REQUEST, known.getStatusCode());
        assertEquals("CUST-CRT-001", known.getBody().errorCode());
        assertEquals("corr-1", known.getBody().correlationId());

        assertEquals(HttpStatus.BAD_REQUEST, unknown.getStatusCode());
        assertEquals("CUST-UNK-001", unknown.getBody().errorCode());
    }

    @Test
    void shouldHandleValidationWithFieldMessageAndFallbackMessage() {
        BeanPropertyBindingResult withError = new BeanPropertyBindingResult(new Object(), "request");
        withError.addError(new FieldError("request", "email", "Email invalid"));

        MethodArgumentNotValidException explicit = mock(MethodArgumentNotValidException.class);
        when(explicit.getBindingResult()).thenReturn(withError);

        BeanPropertyBindingResult withoutErrors = new BeanPropertyBindingResult(new Object(), "request");
        MethodArgumentNotValidException fallback = mock(MethodArgumentNotValidException.class);
        when(fallback.getBindingResult()).thenReturn(withoutErrors);

        ResponseEntity<CustomerErrorResponse> updateValidation =
                handler.handleValidation(explicit, request("PATCH", "/customers/cust-1", null));
        ResponseEntity<CustomerErrorResponse> createValidation =
                handler.handleValidation(fallback, request("POST", "/customers", null));

        assertEquals(HttpStatus.BAD_REQUEST, updateValidation.getStatusCode());
        assertEquals("CUST-UPD-001", updateValidation.getBody().errorCode());
        assertEquals("Email invalid", updateValidation.getBody().message());

        assertEquals(HttpStatus.BAD_REQUEST, createValidation.getStatusCode());
        assertEquals("CUST-CRT-001", createValidation.getBody().errorCode());
        assertEquals("Customer creation validation failed.", createValidation.getBody().message());
    }

    @Test
    void shouldHandleUnreadableAndDataIntegrityByRoute() {
        ResponseEntity<CustomerErrorResponse> unreadableCreate =
                handler.handleUnreadable(new HttpMessageNotReadableException("bad"), request("POST", "/customers", "corr-2"));
        ResponseEntity<CustomerErrorResponse> unreadableUpdate =
                handler.handleUnreadable(new HttpMessageNotReadableException("bad"), request("PATCH", "/customers/cust-1", "corr-3"));

        ResponseEntity<CustomerErrorResponse> createConflict =
                handler.handleDataIntegrity(new DataIntegrityViolationException("duplicate"), request("POST", "/customers", null));
        ResponseEntity<CustomerErrorResponse> deleteConflict =
                handler.handleDataIntegrity(new DataIntegrityViolationException("fk"), request("DELETE", "/customers/cust-1", null));
        ResponseEntity<CustomerErrorResponse> updateValidation =
                handler.handleDataIntegrity(new DataIntegrityViolationException("other"), request("PATCH", "/customers/cust-1", null));

        assertEquals("CUST-CRT-001", unreadableCreate.getBody().errorCode());
        assertEquals("CUST-UPD-001", unreadableUpdate.getBody().errorCode());

        assertEquals(HttpStatus.CONFLICT, createConflict.getStatusCode());
        assertEquals("CUST-CRT-002", createConflict.getBody().errorCode());

        assertEquals(HttpStatus.CONFLICT, deleteConflict.getStatusCode());
        assertEquals("CUST-DEL-002", deleteConflict.getBody().errorCode());

        assertEquals(HttpStatus.BAD_REQUEST, updateValidation.getStatusCode());
        assertEquals("CUST-UPD-001", updateValidation.getBody().errorCode());
    }

    @Test
    void shouldHandleUnhandled() {
        ResponseEntity<CustomerErrorResponse> response =
                handler.handleUnhandled(new RuntimeException("boom"), request("POST", "/customers", null));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CUST-SYS-001", response.getBody().errorCode());
    }

    private MockHttpServletRequest request(String method, String uri, String correlationId) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRequestURI(uri);
        if (correlationId != null) {
            request.setAttribute("correlationId", correlationId);
        }
        return request;
    }

    private static final class UnknownStatusException extends CustomerDomainException {
        private UnknownStatusException() {
            super("CUST-UNK-001", "Unknown status", 999);
        }
    }
}

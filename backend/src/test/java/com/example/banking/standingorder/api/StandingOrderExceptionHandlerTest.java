package com.example.banking.standingorder.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.banking.standingorder.api.dto.FeatureErrorResponse;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class StandingOrderExceptionHandlerTest {

    private final StandingOrderExceptionHandler handler = new StandingOrderExceptionHandler();

    @Test
    void shouldHandleDomainAndUnknownStatusFallback() {
        ResponseEntity<FeatureErrorResponse> known = handler.handleDomain(
                new StandingOrderDomainException.CreateValidationException("bad"),
                request("POST", "/standing-orders", "corr-1"));

        ResponseEntity<FeatureErrorResponse> unknown =
                handler.handleDomain(new UnknownStatusException(), request("POST", "/standing-orders", null));

        assertEquals(HttpStatus.BAD_REQUEST, known.getStatusCode());
        assertEquals("SO-SET-001", known.getBody().errorCode());
        assertEquals("corr-1", known.getBody().correlationId());

        assertEquals(HttpStatus.BAD_REQUEST, unknown.getStatusCode());
        assertEquals("FEAT-UNK-001", unknown.getBody().errorCode());
    }

    @Test
    void shouldHandleValidationWithExplicitFieldMessage() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        result.addError(new FieldError("request", "amount", "Amount invalid"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(result);

        ResponseEntity<FeatureErrorResponse> response =
                handler.handleValidation(ex, request("POST", "/standing-orders", null));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SO-SET-001", response.getBody().errorCode());
        assertEquals("Amount invalid", response.getBody().message());
    }

    @Test
    void shouldMapUnreadableRequestsToExpectedFeatureCodes() {
        List<Case> cases = List.of(
                new Case("POST", "/standing-orders", "SO-SET-001", "Standing-order setup validation failed."),
                new Case("PATCH", "/standing-orders/so-1", "SO-UPD-001", "Standing-order update validation failed."),
                new Case("GET", "/statements/monthly", "STMT-001", "Monthly statement period validation failed."),
                new Case("GET", "/insights/spending", "INS-001", "Spending-insight filter validation failed."),
                new Case("GET", "/unknown", "SO-SET-001", "Standing-order setup validation failed."));

        for (Case item : cases) {
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
            MethodArgumentNotValidException validationEx = mock(MethodArgumentNotValidException.class);
            when(validationEx.getBindingResult()).thenReturn(bindingResult);

            ResponseEntity<FeatureErrorResponse> validation =
                    handler.handleValidation(validationEx, request(item.method, item.uri, null));
            ResponseEntity<FeatureErrorResponse> unreadable =
                    handler.handleUnreadable(new HttpMessageNotReadableException("bad"), request(item.method, item.uri, null));

            assertEquals(HttpStatus.BAD_REQUEST, validation.getStatusCode());
            assertEquals(item.code, validation.getBody().errorCode());
            assertEquals(item.message, validation.getBody().message());

            assertEquals(HttpStatus.BAD_REQUEST, unreadable.getStatusCode());
            assertEquals(item.code, unreadable.getBody().errorCode());
            assertEquals(item.message, unreadable.getBody().message());
        }
    }

    @Test
    void shouldHandleDataIntegrityAndUnhandled() {
        ResponseEntity<FeatureErrorResponse> integrity = handler.handleIntegrity(
                new DataIntegrityViolationException("constraint"),
                request("GET", "/insights/spending", null));

        ResponseEntity<FeatureErrorResponse> unhandled =
                handler.handleUnhandled(new RuntimeException("boom"), request("POST", "/standing-orders", null));

        assertEquals(HttpStatus.BAD_REQUEST, integrity.getStatusCode());
        assertEquals("INS-001", integrity.getBody().errorCode());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, unhandled.getStatusCode());
        assertEquals("SYS-FEAT-001", unhandled.getBody().errorCode());
    }

    private MockHttpServletRequest request(String method, String uri, String correlationId) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRequestURI(uri);
        if (correlationId != null) {
            request.setAttribute("correlationId", correlationId);
        }
        return request;
    }

    private record Case(String method, String uri, String code, String message) {
    }

    private static final class UnknownStatusException extends StandingOrderDomainException {
        private UnknownStatusException() {
            super("FEAT-UNK-001", "Unknown status", 999);
        }
    }
}

package com.example.banking.account.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.banking.account.api.dto.AccountErrorResponse;
import com.example.banking.account.application.AccountDomainException;
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

class AccountExceptionHandlerTest {

    private final AccountExceptionHandler handler = new AccountExceptionHandler();

    @Test
    void shouldHandleDomainExceptionAndCorrelation() {
        MockHttpServletRequest request = request("POST", "/accounts", "corr-1");

        ResponseEntity<AccountErrorResponse> response =
                handler.handleDomain(new AccountDomainException.DepositValidationException(), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TXN-DEP-001", response.getBody().errorCode());
        assertEquals("corr-1", response.getBody().correlationId());
    }

    @Test
    void shouldFallbackToBadRequestForUnknownDomainStatus() {
        MockHttpServletRequest request = request("POST", "/accounts", null);

        ResponseEntity<AccountErrorResponse> response = handler.handleDomain(new UnknownStatusException(), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCT-UNK-001", response.getBody().errorCode());
    }

    @Test
    void shouldHandleValidationWithExplicitFieldMessages() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "amount", "Amount invalid"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<AccountErrorResponse> response =
                handler.handleValidation(ex, request("POST", "/accounts", "corr-2"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCT-CRT-001", response.getBody().errorCode());
        assertEquals("Amount invalid", response.getBody().message());
    }

    @Test
    void shouldMapUnreadableRequestsToExpectedValidationCodes() {
        List<Case> cases = List.of(
                new Case("PATCH", "/accounts/acc-1", "ACCT-UPD-001", "Account update validation failed."),
                new Case("DELETE", "/accounts/acc-1", "ACCT-DEL-002", "Account delete not allowed due to pending movement activity."),
                new Case("POST", "/accounts/acc-1/deposits", "TXN-DEP-001", "Deposit amount validation failed."),
                new Case("POST", "/accounts/acc-1/withdrawals", "TXN-WDR-001", "Withdrawal amount validation failed."),
                new Case("POST", "/transfers", "TXN-TRF-001", "Transfer amount validation failed."),
                new Case("GET", "/accounts/acc-1/transactions", "TXN-HIS-001", "Transaction history filter validation failed."),
                new Case("POST", "/accounts", "ACCT-CRT-001", "Account creation validation failed."));

        for (Case item : cases) {
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);

            ResponseEntity<AccountErrorResponse> validation = handler.handleValidation(ex, request(item.method, item.uri, null));
            ResponseEntity<AccountErrorResponse> unreadable =
                    handler.handleUnreadable(new HttpMessageNotReadableException("bad payload"), request(item.method, item.uri, null));

            assertEquals(HttpStatus.BAD_REQUEST, validation.getStatusCode());
            assertEquals(item.code, validation.getBody().errorCode());
            assertEquals(item.message, validation.getBody().message());

            assertEquals(HttpStatus.BAD_REQUEST, unreadable.getStatusCode());
            assertEquals(item.code, unreadable.getBody().errorCode());
            assertEquals(item.message, unreadable.getBody().message());
        }
    }

    @Test
    void shouldHandleDataIntegrityAndUnhandledErrors() {
        ResponseEntity<AccountErrorResponse> integrity = handler.handleDataIntegrity(
                new DataIntegrityViolationException("constraint"),
                request("POST", "/accounts/acc-1/deposits", "corr-3"));

        ResponseEntity<AccountErrorResponse> unhandled =
                handler.handleUnhandled(new RuntimeException("boom"), request("POST", "/accounts", null));

        assertEquals(HttpStatus.BAD_REQUEST, integrity.getStatusCode());
        assertEquals("TXN-DEP-001", integrity.getBody().errorCode());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, unhandled.getStatusCode());
        assertEquals("ACCT-SYS-001", unhandled.getBody().errorCode());
        assertEquals("", unhandled.getBody().correlationId());
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

    private static final class UnknownStatusException extends AccountDomainException {
        private UnknownStatusException() {
            super("ACCT-UNK-001", "Unknown status", 999);
        }
    }
}

package com.example.banking.auth.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void shouldPropagateIncomingCorrelationId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/login");
        request.addHeader(CorrelationIdFilter.CORRELATION_HEADER, "corr-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seenInChain = new AtomicReference<>();

        FilterChain chain = (req, res) -> seenInChain.set(MDC.get("correlationId"));

        filter.doFilter(request, response, chain);

        assertEquals("corr-1", request.getAttribute("correlationId"));
        assertEquals("corr-1", response.getHeader(CorrelationIdFilter.CORRELATION_HEADER));
        assertEquals("corr-1", seenInChain.get());
        assertNull(MDC.get("correlationId"));
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
        });

        String correlationId = (String) request.getAttribute("correlationId");
        assertNotNull(correlationId);
        assertEquals(correlationId, response.getHeader(CorrelationIdFilter.CORRELATION_HEADER));
    }
}

package com.example.banking.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.auth.application.JwtTokenService;
import com.example.banking.auth.domain.AccountStatus;
import com.example.banking.auth.infrastructure.UserAccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private Claims claims;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenService, userAccountRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateForValidActiveAccessToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/accounts/acc-1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtTokenService.parse("token-1")).thenReturn(claims);
        when(claims.get("tokenType", String.class)).thenReturn("access");
        when(claims.getSubject()).thenReturn("user-1");
        when(userAccountRepository.existsByIdAndStatusNot("user-1", AccountStatus.SUSPENDED)).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user-1", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void shouldSkipAuthenticationForNonAccessToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/accounts/acc-1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-1");

        when(jwtTokenService.parse("token-1")).thenReturn(claims);
        when(claims.get("tokenType", String.class)).thenReturn("refresh");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userAccountRepository, never()).existsByIdAndStatusNot(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldClearSecurityContextOnJwtError() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/accounts/acc-1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer broken-token");

        when(jwtTokenService.parse("broken-token")).thenThrow(new JwtException("invalid"));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldIgnoreMissingAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/accounts/acc-1");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtTokenService, never()).parse(org.mockito.ArgumentMatchers.anyString());
    }
}

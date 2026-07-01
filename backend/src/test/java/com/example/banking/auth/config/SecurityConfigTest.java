package com.example.banking.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();

    @Mock
    private HttpSecurity httpSecurity;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private DefaultSecurityFilterChain securityFilterChain;

    @Test
    void shouldProvideExpectedCorsConfiguration() {
        CorsConfigurationSource source = config.corsConfigurationSource();

        CorsConfiguration cors = ((UrlBasedCorsConfigurationSource) source)
                .getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest("GET", "/auth/login"));

        assertEquals(true, cors.getAllowedOrigins().contains("http://localhost:5173"));
        assertEquals(true, cors.getAllowedMethods().contains("PATCH"));
        assertEquals(true, cors.getExposedHeaders().contains("Authorization"));
        assertEquals(true, cors.getAllowCredentials());
    }

    @Test
    void shouldBuildSecurityFilterChain() throws Exception {
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.cors(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.httpBasic(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class))
                .thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(securityFilterChain);

        SecurityFilterChain chain = config.securityFilterChain(httpSecurity, jwtAuthenticationFilter);

        assertSame(securityFilterChain, chain);
        verify(httpSecurity).csrf(any());
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        verify(httpSecurity).build();
    }
}

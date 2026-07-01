package com.example.banking.auth.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.auth.api.GlobalExceptionHandler;
import com.example.banking.auth.api.TokenRefreshController;
import com.example.banking.auth.application.DomainException;
import com.example.banking.auth.application.RefreshTokenService;
import com.example.banking.auth.config.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = com.example.banking.TestBootApplication.class)
@WebMvcTest(controllers = TokenRefreshController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, TokenRefreshController.class})
class TokenRefreshRotationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @Test
    void shouldRejectReuse() throws Exception {
        when(refreshTokenService.refresh(any(), any()))
                .thenThrow(new DomainException("AUTH-TOKEN-002", "Refresh token reuse detected.", 401));

        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"abcdefghijklmnopqrstuvwxyz123456"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}

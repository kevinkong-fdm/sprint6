package com.example.banking.auth.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.auth.api.PasswordResetController;
import com.example.banking.auth.application.DomainException;
import com.example.banking.auth.application.PasswordResetRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = false)
class PasswordResetThrottleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PasswordResetRequestService passwordResetRequestService;

    @Test
    void shouldEnforceThrottle() throws Exception {
        when(passwordResetRequestService.requestReset(any(), any(), any(), any()))
                .thenThrow(new DomainException("AUTH-RESET-002", "Password reset request throttled.", 429));

        mockMvc.perform(post("/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com"}
                                """))
                .andExpect(status().isTooManyRequests());
    }
}

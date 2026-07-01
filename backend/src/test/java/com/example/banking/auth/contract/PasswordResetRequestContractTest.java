package com.example.banking.auth.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.auth.api.GlobalExceptionHandler;
import com.example.banking.auth.api.PasswordResetController;
import com.example.banking.auth.api.dto.AcceptedResponse;
import com.example.banking.auth.application.DomainException;
import com.example.banking.auth.application.PasswordResetRequestService;
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
@WebMvcTest(controllers = PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, PasswordResetController.class})
class PasswordResetRequestContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private PasswordResetRequestService passwordResetRequestService;

    @Test
    void shouldReturnAccepted() throws Exception {
        when(passwordResetRequestService.requestReset(any(), any(), any(), any()))
                .thenReturn(new AcceptedResponse("If the account exists, reset instructions will be sent.", "corr-1"));

        mockMvc.perform(post("/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnThrottleError() throws Exception {
        when(passwordResetRequestService.requestReset(any(), any(), any(), any()))
                .thenThrow(new DomainException("AUTH-RESET-002", "Password reset request throttled.", 429));

        mockMvc.perform(post("/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.errorCode").value("AUTH-RESET-002"));
    }
}

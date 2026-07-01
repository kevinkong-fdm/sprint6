package com.example.banking.auth.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.auth.api.AuthRegistrationController;
import com.example.banking.auth.api.GlobalExceptionHandler;
import com.example.banking.auth.api.dto.RegisterResponse;
import com.example.banking.auth.application.DomainException;
import com.example.banking.auth.application.RegistrationService;
import com.example.banking.auth.config.JwtAuthenticationFilter;
import java.time.Instant;
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
@WebMvcTest(controllers = AuthRegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, AuthRegistrationController.class})
class RegisterContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RegistrationService registrationService;

    @Test
    void shouldCreateAccount() throws Exception {
        when(registrationService.register(any(), any()))
                .thenReturn(new RegisterResponse("user-1", "alice@example.com", Instant.parse("2026-06-25T10:00:00Z")));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com","password":"StrongPass!234"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void shouldMapDuplicateEmail() throws Exception {
        when(registrationService.register(any(), any()))
                .thenThrow(new DomainException("AUTH-REG-002", "Duplicate account identifier.", 409));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com","password":"StrongPass!234"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("AUTH-REG-002"));
    }
}

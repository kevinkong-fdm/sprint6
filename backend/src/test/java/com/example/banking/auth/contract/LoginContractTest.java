package com.example.banking.auth.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.auth.api.AuthLoginController;
import com.example.banking.auth.api.GlobalExceptionHandler;
import com.example.banking.auth.api.dto.AuthTokenResponse;
import com.example.banking.auth.application.DomainException;
import com.example.banking.auth.application.LoginService;
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
@WebMvcTest(controllers = AuthLoginController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, AuthLoginController.class})
class LoginContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private LoginService loginService;

    @Test
    void shouldAuthenticate() throws Exception {
        when(loginService.login(any(), any()))
                                .thenReturn(new AuthTokenResponse("Bearer", "access", "refresh", 3600, 2_592_000, "user-1"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com","password":"StrongPass!234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldMapInvalidCredentials() throws Exception {
        when(loginService.login(any(), any()))
                .thenThrow(new DomainException("AUTH-LOGIN-001", "Invalid credentials.", 401));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"alice@example.com","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH-LOGIN-001"));
    }
}

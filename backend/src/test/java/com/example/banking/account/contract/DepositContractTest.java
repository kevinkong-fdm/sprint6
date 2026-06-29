package com.example.banking.account.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.account.api.AccountExceptionHandler;
import com.example.banking.account.api.AccountMovementController;
import com.example.banking.account.api.dto.MovementResponse;
import com.example.banking.account.application.AccountAuthorizationService;
import com.example.banking.account.application.AccountDomainException;
import com.example.banking.account.application.AccountMovementService;
import com.example.banking.account.application.GetTransactionHistoryService;
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
@WebMvcTest(controllers = AccountMovementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({AccountExceptionHandler.class, AccountMovementController.class})
class DepositContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountAuthorizationService accountAuthorizationService;

    @MockBean
    private AccountMovementService accountMovementService;

    @MockBean
    private GetTransactionHistoryService getTransactionHistoryService;

    @Test
    void shouldDepositFunds() throws Exception {
        when(accountAuthorizationService.requireActorId(any())).thenReturn("11111111-1111-1111-1111-111111111111");
        when(accountMovementService.deposit(any(), any(), any(), any())).thenReturn(new MovementResponse(
                "dddddddd-dddd-dddd-dddd-dddddddddddd",
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                "DEPOSIT",
                "25.0000",
                "125.0000",
                Instant.parse("2026-06-29T01:00:00Z")));

        mockMvc.perform(post("/accounts/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount":25.0,
                                  "idempotencyKey":"deposit-1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movementType").value("DEPOSIT"));
    }

    @Test
    void shouldMapDepositValidationError() throws Exception {
        when(accountAuthorizationService.requireActorId(any())).thenReturn("11111111-1111-1111-1111-111111111111");
        when(accountMovementService.deposit(any(), any(), any(), any()))
                .thenThrow(new AccountDomainException.DepositValidationException());

        mockMvc.perform(post("/accounts/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount":0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("TXN-DEP-001"));
    }
}

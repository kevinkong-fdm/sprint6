package com.example.banking.account.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.account.api.AccountExceptionHandler;
import com.example.banking.account.api.AccountMovementController;
import com.example.banking.account.api.dto.TransactionHistoryItemResponse;
import com.example.banking.account.api.dto.TransactionHistoryResponse;
import com.example.banking.account.application.AccountAuthorizationService;
import com.example.banking.account.application.AccountDomainException;
import com.example.banking.account.application.AccountMovementService;
import com.example.banking.account.application.GetTransactionHistoryService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = com.example.banking.TestBootApplication.class)
@WebMvcTest(controllers = AccountMovementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({AccountExceptionHandler.class, AccountMovementController.class})
class TransactionHistoryContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountAuthorizationService accountAuthorizationService;

    @MockBean
    private AccountMovementService accountMovementService;

    @MockBean
    private GetTransactionHistoryService getTransactionHistoryService;

    @Test
    void shouldReturnTransactionHistory() throws Exception {
        when(accountAuthorizationService.requireActorId(any())).thenReturn("11111111-1111-1111-1111-111111111111");
        when(getTransactionHistoryService.history(any(), any(), any(), any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(new TransactionHistoryResponse(
                        "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                        List.of(new TransactionHistoryItemResponse(
                                "dddddddd-dddd-dddd-dddd-dddddddddddd",
                                "DEPOSIT",
                                "25.0000",
                                "125.0000",
                                "POSTED",
                                Instant.parse("2026-06-29T01:00:00Z"),
                                Instant.parse("2026-06-29T01:00:00Z"),
                                null)),
                        1,
                        50,
                        1));

        mockMvc.perform(get("/accounts/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].movementType").value("DEPOSIT"));
    }

    @Test
    void shouldMapHistoryValidationError() throws Exception {
        when(accountAuthorizationService.requireActorId(any())).thenReturn("11111111-1111-1111-1111-111111111111");
        when(getTransactionHistoryService.history(any(), any(), any(), any(), any(), any(Integer.class), any(Integer.class)))
                .thenThrow(new AccountDomainException.HistoryValidationException());

        mockMvc.perform(get("/accounts/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/transactions")
                        .param("from", "2026-06-30T00:00:00Z")
                        .param("to", "2026-06-29T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("TXN-HIS-001"));
    }
}

package com.example.banking.account.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.account.api.AccountExceptionHandler;
import com.example.banking.account.api.TransferController;
import com.example.banking.account.api.dto.MovementResponse;
import com.example.banking.account.api.dto.TransferResponse;
import com.example.banking.account.application.AccountAuthorizationService;
import com.example.banking.account.application.AccountDomainException;
import com.example.banking.account.application.TransferFundsService;
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
@WebMvcTest(controllers = TransferController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({AccountExceptionHandler.class, TransferController.class})
class TransferContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountAuthorizationService accountAuthorizationService;

    @MockBean
    private TransferFundsService transferFundsService;

    @Test
    void shouldTransferFunds() throws Exception {
        when(accountAuthorizationService.requireActorId(any())).thenReturn("11111111-1111-1111-1111-111111111111");
        when(transferFundsService.transfer(any(), any(), any())).thenReturn(new TransferResponse(
                "ffffffff-ffff-ffff-ffff-ffffffffffff",
                new MovementResponse(
                        "11111111-2222-3333-4444-555555555555",
                        "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                        "TRANSFER_DEBIT",
                        "15.0000",
                        "85.0000",
                        Instant.parse("2026-06-29T01:00:00Z")),
                new MovementResponse(
                        "66666666-7777-8888-9999-000000000000",
                        "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                        "TRANSFER_CREDIT",
                        "15.0000",
                        "215.0000",
                        Instant.parse("2026-06-29T01:00:00Z"))));

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                                  "destinationAccountId":"bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                                  "amount":15.0,
                                  "idempotencyKey":"transfer-1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transferId").value("ffffffff-ffff-ffff-ffff-ffffffffffff"));
    }

    @Test
    void shouldMapPairingConflict() throws Exception {
        when(accountAuthorizationService.requireActorId(any())).thenReturn("11111111-1111-1111-1111-111111111111");
        when(transferFundsService.transfer(any(), any(), any()))
                .thenThrow(new AccountDomainException.TransferPairingException());

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                                  "destinationAccountId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                                  "amount":15.0
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("TXN-TRF-002"));
    }
}

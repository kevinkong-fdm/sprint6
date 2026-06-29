package com.example.banking.account.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.account.api.AccountExceptionHandler;
import com.example.banking.account.api.TransferController;
import com.example.banking.account.application.AccountAuthorizationService;
import com.example.banking.account.application.AccountDomainException;
import com.example.banking.account.application.TransferFundsService;
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
class TransferIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountAuthorizationService accountAuthorizationService;

    @MockBean
    private TransferFundsService transferFundsService;

    @Test
    void shouldRejectInsufficientTransferFunds() throws Exception {
        when(accountAuthorizationService.requireActorId(any())).thenReturn("11111111-1111-1111-1111-111111111111");
        when(transferFundsService.transfer(any(), any(), any()))
                .thenThrow(new AccountDomainException.InsufficientTransferFundsException());

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                                  "destinationAccountId":"bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                                  "amount":999999.0
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("TXN-TRF-003"));
    }
}

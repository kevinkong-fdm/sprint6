package com.example.banking.account.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.account.api.AccountController;
import com.example.banking.account.api.AccountExceptionHandler;
import com.example.banking.account.api.dto.AccountListResponse;
import com.example.banking.account.api.dto.AccountResponse;
import com.example.banking.account.application.AccountAuthorizationService;
import com.example.banking.account.application.CreateAccountService;
import com.example.banking.account.application.DeleteAccountWorkflowService;
import com.example.banking.account.application.GetAccountService;
import com.example.banking.account.application.ListAccountsService;
import com.example.banking.account.application.UpdateAccountService;
import java.time.Instant;
import java.util.List;
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
@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({AccountExceptionHandler.class, AccountController.class})
class AccountCreateRetrieveListIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountAuthorizationService accountAuthorizationService;

    @MockBean
    private CreateAccountService createAccountService;

    @MockBean
    private ListAccountsService listAccountsService;

    @MockBean
    private GetAccountService getAccountService;

    @MockBean
    private UpdateAccountService updateAccountService;

    @MockBean
    private DeleteAccountWorkflowService deleteAccountWorkflowService;

    @Test
    void shouldRejectInvalidCreatePayload() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountType":""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ACCT-CRT-001"));
    }

    @Test
    void shouldCreateThenList() throws Exception {
        when(accountAuthorizationService.requireActorId(any())).thenReturn("11111111-1111-1111-1111-111111111111");
        when(createAccountService.create(any(), any())).thenReturn(new AccountResponse(
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                "11111111-1111-1111-1111-111111111111",
                "CHECKING",
                "Primary",
                "USD",
                "100.0000",
                "100.0000",
                "ACTIVE",
                Instant.parse("2026-06-29T00:00:00Z"),
                Instant.parse("2026-06-29T00:00:00Z")));
        when(listAccountsService.list(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(new AccountListResponse(List.of(new AccountResponse(
                        "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                        "11111111-1111-1111-1111-111111111111",
                        "CHECKING",
                        "Primary",
                        "USD",
                        "100.0000",
                        "100.0000",
                        "ACTIVE",
                        Instant.parse("2026-06-29T00:00:00Z"),
                        Instant.parse("2026-06-29T00:00:00Z"))), 1, 20, 1));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountType":"CHECKING",
                                  "nickname":"Primary"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].accountType").value("CHECKING"));
    }
}

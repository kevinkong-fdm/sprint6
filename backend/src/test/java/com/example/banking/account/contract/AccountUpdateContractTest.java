package com.example.banking.account.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.account.api.AccountController;
import com.example.banking.account.api.AccountExceptionHandler;
import com.example.banking.account.api.dto.AccountResponse;
import com.example.banking.account.application.AccountAuthorizationService;
import com.example.banking.account.application.AccountDomainException;
import com.example.banking.account.application.CreateAccountService;
import com.example.banking.account.application.DeleteAccountWorkflowService;
import com.example.banking.account.application.GetAccountService;
import com.example.banking.account.application.ListAccountsService;
import com.example.banking.account.application.UpdateAccountService;
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
@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({AccountExceptionHandler.class, AccountController.class})
class AccountUpdateContractTest {

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
    void shouldUpdateNickname() throws Exception {
        when(accountAuthorizationService.requireActorId(any())).thenReturn("11111111-1111-1111-1111-111111111111");
        when(updateAccountService.update(any(), any(), any())).thenReturn(new AccountResponse(
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                "11111111-1111-1111-1111-111111111111",
                "CHECKING",
                "Payroll",
                "USD",
                "100.0000",
                "100.0000",
                "ACTIVE",
                Instant.parse("2026-06-29T00:00:00Z"),
                Instant.parse("2026-06-29T01:00:00Z")));

        mockMvc.perform(patch("/accounts/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname":"Payroll"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("Payroll"));
    }

    @Test
    void shouldMapImmutableFieldError() throws Exception {
        when(accountAuthorizationService.requireActorId(any())).thenReturn("11111111-1111-1111-1111-111111111111");
        when(updateAccountService.update(any(), any(), any()))
                .thenThrow(new AccountDomainException.ImmutableFieldUpdateException());

        mockMvc.perform(patch("/accounts/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountType":"SAVINGS"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("ACCT-UPD-002"));
    }
}

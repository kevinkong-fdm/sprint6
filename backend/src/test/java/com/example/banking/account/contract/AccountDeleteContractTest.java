package com.example.banking.account.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.account.api.AccountController;
import com.example.banking.account.api.AccountExceptionHandler;
import com.example.banking.account.application.AccountAuthorizationService;
import com.example.banking.account.application.AccountDomainException;
import com.example.banking.account.application.CreateAccountService;
import com.example.banking.account.application.DeleteAccountWorkflowService;
import com.example.banking.account.application.GetAccountService;
import com.example.banking.account.application.ListAccountsService;
import com.example.banking.account.application.UpdateAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = com.example.banking.TestBootApplication.class)
@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({AccountExceptionHandler.class, AccountController.class})
class AccountDeleteContractTest {

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
    void shouldDeleteAccount() throws Exception {
        when(accountAuthorizationService.requireActorId(any())).thenReturn("11111111-1111-1111-1111-111111111111");
        doNothing().when(deleteAccountWorkflowService).delete(any(), any(), any(), any());

        mockMvc.perform(delete("/accounts/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldMapDeleteEligibilityConflict() throws Exception {
        when(accountAuthorizationService.requireActorId(any())).thenReturn("11111111-1111-1111-1111-111111111111");
        doThrow(new AccountDomainException.DeleteEligibilityException(
                "Account delete not allowed due to pending movement activity.",
                409)).when(deleteAccountWorkflowService).delete(any(), any(), any(), any());

        mockMvc.perform(delete("/accounts/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("ACCT-DEL-002"));
    }
}

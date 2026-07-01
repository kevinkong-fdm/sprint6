package com.example.banking.statement.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.statement.api.StatementController;
import com.example.banking.statement.application.GenerateMonthlyStatementService;
import com.example.banking.statement.application.GetMonthlyStatementService;
import com.example.banking.statement.application.ListMonthlyStatementsService;
import com.example.banking.standingorder.api.StandingOrderExceptionHandler;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderDomainException;
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
@WebMvcTest(controllers = StatementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({StandingOrderExceptionHandler.class, StatementController.class})
class MonthlyStatementValidationContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @MockBean
    private GenerateMonthlyStatementService generateMonthlyStatementService;

    @MockBean
    private GetMonthlyStatementService getMonthlyStatementService;

    @MockBean
    private ListMonthlyStatementsService listMonthlyStatementsService;

    @Test
    void shouldRejectInvalidGeneratePayload() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("actor-1");

        mockMvc.perform(post("/statements/monthly/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "acc-1",
                                  "month": "2026-15"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("STMT-001"));
    }

    @Test
    void shouldMapStatementValidationError() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("actor-1");
        when(getMonthlyStatementService.get(any(), any(), any(), any()))
                .thenThrow(new StandingOrderDomainException.StatementValidationException());

        mockMvc.perform(get("/statements/monthly")
                        .param("accountId", "acc-1")
                        .param("month", "2026-04"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("STMT-001"));
    }
}

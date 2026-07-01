package com.example.banking.statement.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.statement.api.StatementController;
import com.example.banking.statement.api.dto.MonthlyStatementListResponse;
import com.example.banking.statement.api.dto.MonthlyStatementResponse;
import com.example.banking.statement.api.dto.MonthlyStatementSingleResponse;
import com.example.banking.statement.api.dto.StatementLineItemResponse;
import com.example.banking.statement.application.GenerateMonthlyStatementService;
import com.example.banking.statement.application.GetMonthlyStatementService;
import com.example.banking.statement.application.ListMonthlyStatementsService;
import com.example.banking.standingorder.api.StandingOrderExceptionHandler;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
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
@WebMvcTest(controllers = StatementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({StandingOrderExceptionHandler.class, StatementController.class})
class MonthlyStatementContractTest {

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
    void shouldGenerateMonthlyStatement() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("actor-1");
        when(generateMonthlyStatementService.generate(any(), any(), any()))
                .thenReturn(singleResponse("acc-1", "2026-05"));

        mockMvc.perform(post("/statements/monthly/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "acc-1",
                                  "month": "2026-05"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value("acc-1"))
                .andExpect(jsonPath("$.data.month").value("2026-05"));
    }

    @Test
    void shouldRetrieveMonthlyStatement() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("actor-1");
        when(getMonthlyStatementService.get(any(), any(), any(), any()))
                .thenReturn(singleResponse("acc-1", "2026-04"));

        mockMvc.perform(get("/statements/monthly")
                        .param("accountId", "acc-1")
                        .param("month", "2026-04"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value("acc-1"))
                .andExpect(jsonPath("$.data.month").value("2026-04"));
    }

    @Test
    void shouldListGeneratedMonthlyHistory() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("actor-1");
        when(listMonthlyStatementsService.list(any(), any(), any()))
                .thenReturn(new MonthlyStatementListResponse(
                        "corr-1",
                        Instant.parse("2026-06-30T00:00:00Z"),
                        List.of(statement("acc-1", "2026-06"))));

        mockMvc.perform(get("/statements/monthly/history")
                        .param("accountId", "acc-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].month").value("2026-06"));
    }

    private MonthlyStatementSingleResponse singleResponse(String accountId, String month) {
        return new MonthlyStatementSingleResponse(
                "corr-1",
                Instant.parse("2026-06-30T00:00:00Z"),
                statement(accountId, month));
    }

    private MonthlyStatementResponse statement(String accountId, String month) {
        return new MonthlyStatementResponse(
                accountId,
                "cust-1",
                month,
                "AEST",
                "100.0000",
                "120.0000",
                "15.0000",
                "35.0000",
                List.of(new StatementLineItemResponse(
                        "txn-1",
                        Instant.parse("2026-06-01T00:00:00Z"),
                        "CREDIT",
                        "20.0000",
                        "120.0000",
                        "Salary")),
                Instant.parse("2026-06-30T00:00:00Z"));
    }
}

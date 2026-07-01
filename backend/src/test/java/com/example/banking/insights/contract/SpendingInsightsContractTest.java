package com.example.banking.insights.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.insights.api.InsightsController;
import com.example.banking.insights.api.dto.SpendingInsightsCategoryResponse;
import com.example.banking.insights.api.dto.SpendingInsightsDataResponse;
import com.example.banking.insights.api.dto.SpendingInsightsSingleResponse;
import com.example.banking.insights.api.dto.SpendingInsightsTotalsResponse;
import com.example.banking.insights.application.GetSpendingInsightsService;
import com.example.banking.standingorder.api.StandingOrderExceptionHandler;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import java.time.Instant;
import java.time.LocalDate;
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
@WebMvcTest(controllers = InsightsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({StandingOrderExceptionHandler.class, InsightsController.class})
class SpendingInsightsContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @MockBean
    private GetSpendingInsightsService getSpendingInsightsService;

    @Test
    void shouldReturnSpendingInsights() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("actor-1");
        when(getSpendingInsightsService.getInsights(any(), any(), any(), any(), any(), any()))
                .thenReturn(new SpendingInsightsSingleResponse(
                        "corr-1",
                        Instant.parse("2026-06-30T00:00:00Z"),
                        new SpendingInsightsDataResponse(
                                LocalDate.parse("2026-05-01"),
                                LocalDate.parse("2026-05-31"),
                                "PREVIOUS_PERIOD",
                                false,
                                "NONE",
                                List.of(new SpendingInsightsCategoryResponse(
                                        "TRANSFER",
                                        "120.0000",
                                        "100.0000",
                                        "20.0000",
                                        20.0)),
                                new SpendingInsightsTotalsResponse("120.0000", "100.0000", "20.0000", 20.0))));

        mockMvc.perform(get("/insights/spending")
                        .param("periodStart", "2026-05-01")
                        .param("periodEnd", "2026-05-31")
                        .param("comparisonMode", "PREVIOUS_PERIOD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comparisonMode").value("PREVIOUS_PERIOD"))
                .andExpect(jsonPath("$.data.insufficientData").value(false))
                .andExpect(jsonPath("$.data.categories[0].category").value("TRANSFER"));
    }
}

package com.example.banking.insights.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.insights.api.InsightsController;
import com.example.banking.insights.application.GetSpendingInsightsService;
import com.example.banking.standingorder.api.StandingOrderExceptionHandler;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderDomainException;
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
class SpendingInsightsValidationContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @MockBean
    private GetSpendingInsightsService getSpendingInsightsService;

    @Test
    void shouldMapInsightsValidationFailure() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("actor-1");
        when(getSpendingInsightsService.getInsights(any(), any(), any(), any(), any(), any()))
                .thenThrow(new StandingOrderDomainException.InsightsValidationException());

        mockMvc.perform(get("/insights/spending")
                        .param("periodStart", "2026-05-01")
                        .param("periodEnd", "2026-05-31")
                        .param("comparisonMode", "PREVIOUS_PERIOD"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INS-001"));
    }
}

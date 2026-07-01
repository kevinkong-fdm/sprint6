package com.example.banking.standingorder.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.standingorder.api.StandingOrderController;
import com.example.banking.standingorder.api.StandingOrderExceptionHandler;
import com.example.banking.standingorder.api.dto.StandingOrderResponseMapper;
import com.example.banking.standingorder.application.CreateStandingOrderService;
import com.example.banking.standingorder.application.GetStandingOrderService;
import com.example.banking.standingorder.application.ListStandingOrdersService;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderDomainException;
import com.example.banking.standingorder.application.StandingOrderExecutionService;
import com.example.banking.standingorder.application.StandingOrderLifecycleService;
import com.example.banking.standingorder.application.UpdateStandingOrderService;
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
@WebMvcTest(controllers = StandingOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({StandingOrderExceptionHandler.class, StandingOrderController.class})
class StandingOrderDestinationValidationContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @MockBean
    private CreateStandingOrderService createStandingOrderService;

    @MockBean
    private ListStandingOrdersService listStandingOrdersService;

    @MockBean
    private GetStandingOrderService getStandingOrderService;

    @MockBean
    private UpdateStandingOrderService updateStandingOrderService;

    @MockBean
    private StandingOrderLifecycleService standingOrderLifecycleService;

    @MockBean
    private StandingOrderExecutionService standingOrderExecutionService;

    @MockBean
    private StandingOrderResponseMapper standingOrderResponseMapper;

    @Test
    void shouldRejectExternalDestination() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("cust-1");
        when(createStandingOrderService.create(any(), any(), any(), any()))
                .thenThrow(new StandingOrderDomainException.DestinationInternalOnlyException());

        mockMvc.perform(post("/standing-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId":"src-1",
                                  "destinationAccountId":"dst-1",
                                  "amount":10.0000,
                                  "frequency":"DAILY",
                                  "startDate":"2026-06-01",
                                  "executionTime":"09:00",
                                  "externalBankDetails":{
                                    "bsb":"123-456",
                                    "accountNumber":"99887766"
                                  }
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("SO-SET-004"));
    }

    @Test
    void shouldRejectCrossCustomerDestination() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("cust-1");
        when(createStandingOrderService.create(any(), any(), any(), any()))
                .thenThrow(new StandingOrderDomainException.DestinationOwnershipMismatchException());

        mockMvc.perform(post("/standing-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId":"src-1",
                                  "destinationAccountId":"dst-other",
                                  "amount":10.0000,
                                  "frequency":"DAILY",
                                  "startDate":"2026-06-01",
                                  "executionTime":"09:00"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("SO-SET-005"));
    }
}

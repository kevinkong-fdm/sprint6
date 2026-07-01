package com.example.banking.standingorder.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.standingorder.api.StandingOrderController;
import com.example.banking.standingorder.api.StandingOrderExceptionHandler;
import com.example.banking.standingorder.api.dto.StandingOrderResponse;
import com.example.banking.standingorder.api.dto.StandingOrderResponseMapper;
import com.example.banking.standingorder.api.dto.StandingOrderSingleResponse;
import com.example.banking.standingorder.application.CreateStandingOrderService;
import com.example.banking.standingorder.application.GetStandingOrderService;
import com.example.banking.standingorder.application.ListStandingOrdersService;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
import com.example.banking.standingorder.application.StandingOrderExecutionService;
import com.example.banking.standingorder.application.StandingOrderLifecycleService;
import com.example.banking.standingorder.application.UpdateStandingOrderService;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
class StandingOrderLifecycleIntegrationTest {

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
    void shouldExecuteCreateUpdatePauseResumeCancelFlow() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("cust-1");

        StandingOrderEntity created = standingOrder("so-1", "ACTIVE");
        StandingOrderEntity updated = standingOrder("so-1", "ACTIVE");
        StandingOrderEntity paused = standingOrder("so-1", "PAUSED");
        StandingOrderEntity resumed = standingOrder("so-1", "ACTIVE");
        StandingOrderEntity canceled = standingOrder("so-1", "CANCELED");

        when(createStandingOrderService.create(any(), any(), any(), any())).thenReturn(created);
        when(updateStandingOrderService.update(any(), any(), any(), any())).thenReturn(updated);
        when(standingOrderLifecycleService.pause(any(), any(), any())).thenReturn(paused);
        when(standingOrderLifecycleService.resume(any(), any(), any())).thenReturn(resumed);
        when(standingOrderLifecycleService.cancel(any(), any(), any())).thenReturn(canceled);
        when(standingOrderResponseMapper.toSingleResponse(any(StandingOrderEntity.class), any()))
                .thenAnswer(invocation -> {
                    StandingOrderEntity entity = invocation.getArgument(0, StandingOrderEntity.class);
                    return response(entity.getStandingOrderId(), entity.getStatus().name());
                });

        mockMvc.perform(post("/standing-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId":"src-1",
                                  "destinationAccountId":"dst-1",
                                  "amount":20.0000,
                                  "frequency":"DAILY",
                                  "startDate":"2026-06-01",
                                  "executionTime":"09:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(patch("/standing-orders/so-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount":25.0000,
                                  "executionTime":"08:30"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(post("/standing-orders/so-1/pause"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAUSED"));

        mockMvc.perform(post("/standing-orders/so-1/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(post("/standing-orders/so-1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"));
    }

    private StandingOrderEntity standingOrder(String standingOrderId, String status) {
        StandingOrderEntity entity = StandingOrderEntity.create(
                "cust-1",
                "src-1",
                "dst-1",
                new BigDecimal("20.0000"),
                StandingOrderFrequency.DAILY,
                LocalDate.parse("2026-06-01"),
                null,
                null,
                null,
                "09:00",
                Instant.parse("2026-06-02T00:00:00Z"),
                "AEST",
                "idem-1");

        if ("PAUSED".equals(status)) {
            entity.pause();
        } else if ("CANCELED".equals(status)) {
            entity.cancel();
        }

        org.springframework.test.util.ReflectionTestUtils.setField(entity, "standingOrderId", standingOrderId);
        return entity;
    }

    private StandingOrderSingleResponse response(String standingOrderId, String status) {
        return new StandingOrderSingleResponse(
                "corr-1",
                Instant.parse("2026-06-30T00:00:00Z"),
                new StandingOrderResponse(
                        standingOrderId,
                        "cust-1",
                        "src-1",
                        "dst-1",
                        "20.0000",
                        "DAILY",
                        LocalDate.parse("2026-06-01"),
                        null,
                        null,
                        null,
                        "09:00",
                        status,
                        "AEST",
                        Instant.parse("2026-06-02T00:00:00Z"),
                        null,
                        Instant.parse("2026-06-01T00:00:00Z"),
                        Instant.parse("2026-06-01T00:00:00Z")));
    }
}

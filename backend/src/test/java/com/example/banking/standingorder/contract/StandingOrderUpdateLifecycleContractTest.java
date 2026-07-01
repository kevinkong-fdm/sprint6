package com.example.banking.standingorder.contract;

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
class StandingOrderUpdateLifecycleContractTest {

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
    void shouldUpdateStandingOrder() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("cust-1");
        when(updateStandingOrderService.update(any(), any(), any(), any())).thenReturn(sampleEntity("so-1", "ACTIVE"));
        when(standingOrderResponseMapper.toSingleResponse(any(StandingOrderEntity.class), any()))
                .thenReturn(singleResponse("so-1", "ACTIVE"));

        mockMvc.perform(patch("/standing-orders/so-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount":25.0000,
                                  "executionTime":"08:30"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.standingOrderId").value("so-1"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void shouldPauseStandingOrder() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("cust-1");
        when(standingOrderLifecycleService.pause(any(), any(), any())).thenReturn(sampleEntity("so-1", "PAUSED"));
        when(standingOrderResponseMapper.toSingleResponse(any(StandingOrderEntity.class), any()))
                .thenReturn(singleResponse("so-1", "PAUSED"));

        mockMvc.perform(post("/standing-orders/so-1/pause"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAUSED"));
    }

    @Test
    void shouldResumeStandingOrder() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("cust-1");
        when(standingOrderLifecycleService.resume(any(), any(), any())).thenReturn(sampleEntity("so-1", "ACTIVE"));
        when(standingOrderResponseMapper.toSingleResponse(any(StandingOrderEntity.class), any()))
                .thenReturn(singleResponse("so-1", "ACTIVE"));

        mockMvc.perform(post("/standing-orders/so-1/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void shouldCancelStandingOrder() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("cust-1");
        when(standingOrderLifecycleService.cancel(any(), any(), any())).thenReturn(sampleEntity("so-1", "CANCELED"));
        when(standingOrderResponseMapper.toSingleResponse(any(StandingOrderEntity.class), any()))
                .thenReturn(singleResponse("so-1", "CANCELED"));

        mockMvc.perform(post("/standing-orders/so-1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"));
    }

    private StandingOrderEntity sampleEntity(String standingOrderId, String status) {
        StandingOrderEntity entity = StandingOrderEntity.create(
                "cust-1",
                "src-1",
                "dst-1",
                new BigDecimal("25.0000"),
                StandingOrderFrequency.DAILY,
                LocalDate.parse("2026-06-01"),
                null,
                null,
                null,
                "08:30",
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

    private StandingOrderSingleResponse singleResponse(String standingOrderId, String status) {
        return new StandingOrderSingleResponse(
                "corr-1",
                Instant.parse("2026-06-30T00:00:00Z"),
                new StandingOrderResponse(
                        standingOrderId,
                        "cust-1",
                        "src-1",
                        "dst-1",
                        "25.0000",
                        "DAILY",
                        LocalDate.parse("2026-06-01"),
                        null,
                        null,
                        null,
                        "08:30",
                        status,
                        "AEST",
                        Instant.parse("2026-06-02T00:00:00Z"),
                        null,
                        Instant.parse("2026-06-01T00:00:00Z"),
                        Instant.parse("2026-06-01T00:00:00Z")));
    }
}

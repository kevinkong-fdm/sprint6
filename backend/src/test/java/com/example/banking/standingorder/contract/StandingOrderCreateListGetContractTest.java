package com.example.banking.standingorder.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.standingorder.api.StandingOrderController;
import com.example.banking.standingorder.api.StandingOrderExceptionHandler;
import com.example.banking.standingorder.api.dto.StandingOrderListData;
import com.example.banking.standingorder.api.dto.StandingOrderListResponse;
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
@WebMvcTest(controllers = StandingOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({StandingOrderExceptionHandler.class, StandingOrderController.class})
class StandingOrderCreateListGetContractTest {

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
    void shouldCreateStandingOrder() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("cust-1");
        when(createStandingOrderService.create(any(), any(), any(), any())).thenReturn(sampleEntity("so-1"));
        when(standingOrderResponseMapper.toSingleResponse(any(StandingOrderEntity.class), any()))
                .thenReturn(singleResponse("so-1", "ACTIVE"));

        mockMvc.perform(post("/standing-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "idem-1")
                        .content("""
                                {
                                  "sourceAccountId":"src-1",
                                  "destinationAccountId":"dst-1",
                                  "amount":10.0000,
                                  "frequency":"DAILY",
                                  "startDate":"2026-06-01",
                                  "executionTime":"09:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.standingOrderId").value("so-1"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.frequency").value("DAILY"));
    }

    @Test
    void shouldListStandingOrders() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("cust-1");
        when(listStandingOrdersService.list(any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(new StandingOrderListResponse(
                        "corr-1",
                        Instant.parse("2026-06-30T00:00:00Z"),
                        new StandingOrderListData(List.of(standingOrder("so-1", "ACTIVE")), 1, 20, 1)));

        mockMvc.perform(get("/standing-orders")
                        .param("status", "ACTIVE")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].standingOrderId").value("so-1"))
                .andExpect(jsonPath("$.data.items[0].status").value("ACTIVE"));
    }

    @Test
    void shouldGetStandingOrderById() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("cust-1");
        when(getStandingOrderService.getById(any(), any(), any())).thenReturn(singleResponse("so-1", "ACTIVE"));

        mockMvc.perform(get("/standing-orders/so-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.standingOrderId").value("so-1"))
                .andExpect(jsonPath("$.data.destinationAccountId").value("dst-1"));
    }

    private StandingOrderEntity sampleEntity(String standingOrderId) {
        StandingOrderEntity entity = StandingOrderEntity.create(
                "cust-1",
                "src-1",
                "dst-1",
                new BigDecimal("10.0000"),
                StandingOrderFrequency.DAILY,
                LocalDate.parse("2026-06-01"),
                null,
                null,
                null,
                "09:00",
                Instant.parse("2026-06-02T00:00:00Z"),
                "AEST",
                "idem-1");
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "standingOrderId", standingOrderId);
        return entity;
    }

    private StandingOrderSingleResponse singleResponse(String standingOrderId, String status) {
        return new StandingOrderSingleResponse(
                "corr-1",
                Instant.parse("2026-06-30T00:00:00Z"),
                standingOrder(standingOrderId, status));
    }

    private StandingOrderResponse standingOrder(String standingOrderId, String status) {
        return new StandingOrderResponse(
                standingOrderId,
                "cust-1",
                "src-1",
                "dst-1",
                "10.0000",
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
                Instant.parse("2026-06-01T00:00:00Z"));
    }
}

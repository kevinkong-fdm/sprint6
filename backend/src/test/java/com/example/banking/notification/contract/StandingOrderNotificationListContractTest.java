package com.example.banking.notification.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.notification.api.NotificationController;
import com.example.banking.notification.api.dto.NotificationEventResponse;
import com.example.banking.notification.api.dto.NotificationListData;
import com.example.banking.notification.api.dto.NotificationListResponse;
import com.example.banking.notification.application.ListNotificationsService;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = com.example.banking.TestBootApplication.class)
@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({StandingOrderExceptionHandler.class, NotificationController.class})
class StandingOrderNotificationListContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @MockBean
    private ListNotificationsService listNotificationsService;

    @Test
    void shouldListStandingOrderNotifications() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("actor-1");
        when(listNotificationsService.list(any(), any(), any(), any(Integer.class), any(Integer.class), any()))
                .thenReturn(new NotificationListResponse(
                        "corr-1",
                        Instant.parse("2026-06-30T00:00:00Z"),
                        new NotificationListData(
                                List.of(new NotificationEventResponse(
                                        "notif-1",
                                        "so-1",
                                        "exe-1",
                                        "EXECUTION_SUCCESS",
                                        "Success",
                                        "Standing order SO-1 succeeded",
                                        "SENT",
                                        1,
                                        Instant.parse("2026-06-30T00:00:00Z"),
                                        Instant.parse("2026-06-30T00:00:01Z"))),
                                1,
                                20,
                                1)));

        mockMvc.perform(get("/notifications/standing-orders")
                        .param("eventType", "EXECUTION_SUCCESS")
                        .param("dispatchStatus", "SENT")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].eventType").value("EXECUTION_SUCCESS"))
                .andExpect(jsonPath("$.data.items[0].dispatchStatus").value("SENT"));
    }
}

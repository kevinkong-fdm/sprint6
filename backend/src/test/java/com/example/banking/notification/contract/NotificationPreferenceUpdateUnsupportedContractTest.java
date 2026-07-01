package com.example.banking.notification.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.notification.api.NotificationController;
import com.example.banking.notification.application.ListNotificationsService;
import com.example.banking.standingorder.api.StandingOrderExceptionHandler;
import com.example.banking.standingorder.application.StandingOrderAuthorizationService;
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
@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({StandingOrderExceptionHandler.class, NotificationController.class})
class NotificationPreferenceUpdateUnsupportedContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StandingOrderAuthorizationService standingOrderAuthorizationService;

    @MockBean
    private ListNotificationsService listNotificationsService;

    @Test
    void shouldRejectPreferenceUpdateInThisVersion() throws Exception {
        when(standingOrderAuthorizationService.requireActorId(any())).thenReturn("actor-1");

        mockMvc.perform(patch("/notifications/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "standingOrderNotificationsEnabled": false
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("NOTIFY-001"));
    }
}

package com.example.banking.customer.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.customer.api.CustomerExceptionHandler;
import com.example.banking.customer.api.UpdateCustomerController;
import com.example.banking.customer.api.dto.CustomerResponse;
import com.example.banking.customer.application.CustomerDomainException;
import com.example.banking.customer.application.UpdateCustomerService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UpdateCustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({CustomerExceptionHandler.class, UpdateCustomerController.class})
class UpdateCustomerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UpdateCustomerService updateCustomerService;

    @Test
    void shouldUpdateCustomer() throws Exception {
        when(updateCustomerService.update(any(), any(), any(), any()))
                .thenReturn(new CustomerResponse(
                        "11111111-1111-1111-1111-111111111111",
                        "alice@example.com",
                        "Alice",
                        "Ng",
                        "+12065550124",
                        null,
                        "en",
                        List.of(),
                        List.of(),
                        Instant.parse("2026-06-25T10:00:00Z"),
                        Instant.parse("2026-06-25T10:05:00Z")));

        mockMvc.perform(patch("/customers/11111111-1111-1111-1111-111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "+12065550124"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("+12065550124"));
    }

    @Test
    void shouldMapImmutableFieldAttempt() throws Exception {
        when(updateCustomerService.update(any(), any(), any(), any()))
                .thenThrow(new CustomerDomainException.ImmutableFieldUpdateException("Immutable customer field update attempted."));

        mockMvc.perform(patch("/customers/11111111-1111-1111-1111-111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new@example.com"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("CUST-UPD-003"));
    }
}

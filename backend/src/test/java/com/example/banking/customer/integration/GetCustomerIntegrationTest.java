package com.example.banking.customer.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.customer.api.CustomerExceptionHandler;
import com.example.banking.customer.api.GetCustomerController;
import com.example.banking.customer.api.dto.CustomerResponse;
import com.example.banking.customer.application.CustomerDomainException;
import com.example.banking.customer.application.GetCustomerService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GetCustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CustomerExceptionHandler.class)
class GetCustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetCustomerService getCustomerService;

    @Test
    void shouldReturnCustomerWhenPresent() throws Exception {
        when(getCustomerService.getById(any(), any(), any()))
                .thenReturn(new CustomerResponse(
                        "11111111-1111-1111-1111-111111111111",
                        "alice@example.com",
                        "Alice",
                        "Ng",
                        null,
                        null,
                        null,
                        List.of(),
                        List.of(),
                        Instant.parse("2026-06-25T10:00:00Z"),
                        Instant.parse("2026-06-25T10:00:00Z")));

        mockMvc.perform(get("/customers/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void shouldReturnNotFoundForMissingCustomer() throws Exception {
        when(getCustomerService.getById(any(), any(), any()))
                .thenThrow(new CustomerDomainException.GetNotFoundException("Customer not found."));

        mockMvc.perform(get("/customers/99999999-9999-9999-9999-999999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUST-GET-001"));
    }
}

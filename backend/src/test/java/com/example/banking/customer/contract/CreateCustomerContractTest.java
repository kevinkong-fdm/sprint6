package com.example.banking.customer.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.customer.api.CreateCustomerController;
import com.example.banking.customer.api.CustomerExceptionHandler;
import com.example.banking.customer.api.dto.CustomerResponse;
import com.example.banking.customer.application.CreateCustomerService;
import com.example.banking.customer.application.CustomerDomainException;
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

@WebMvcTest(controllers = CreateCustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CustomerExceptionHandler.class)
class CreateCustomerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateCustomerService createCustomerService;

    @Test
    void shouldCreateCustomer() throws Exception {
        when(createCustomerService.create(any(), any(), any()))
                .thenReturn(new CustomerResponse(
                        "11111111-1111-1111-1111-111111111111",
                        "alice@example.com",
                        "Alice",
                        "Ng",
                        "+12065550123",
                        null,
                        "en",
                        List.of(),
                        List.of(),
                        Instant.parse("2026-06-25T10:00:00Z"),
                        Instant.parse("2026-06-25T10:00:00Z")));

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"alice@example.com",
                                  "givenName":"Alice",
                                  "familyName":"Ng"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void shouldMapDuplicateError() throws Exception {
        when(createCustomerService.create(any(), any(), any()))
                .thenThrow(new CustomerDomainException.DuplicateIdentityException("Duplicate customer identity attribute."));

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"alice@example.com",
                                  "givenName":"Alice",
                                  "familyName":"Ng"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CUST-CRT-002"));
    }
}

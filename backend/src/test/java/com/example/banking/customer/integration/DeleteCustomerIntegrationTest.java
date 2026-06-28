package com.example.banking.customer.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.banking.customer.api.CustomerExceptionHandler;
import com.example.banking.customer.api.DeleteCustomerController;
import com.example.banking.customer.application.CustomerDomainException;
import com.example.banking.customer.application.DeleteCustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DeleteCustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({CustomerExceptionHandler.class, DeleteCustomerController.class})
class DeleteCustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeleteCustomerService deleteCustomerService;

    @Test
    void shouldDeleteCustomerAndReturnNoContent() throws Exception {
        mockMvc.perform(delete("/customers/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnCascadeFailureError() throws Exception {
        doThrow(new CustomerDomainException.CascadeDeleteFailureException(
                "Customer hard delete failed due to cascading dependency deletion error."))
                .when(deleteCustomerService)
                .delete(any(), any(), any());

        mockMvc.perform(delete("/customers/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CUST-DEL-002"));
    }
}

package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ErpAppTest
@AutoConfigureMockMvc
class CrmApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageCustomerTicketAssignmentResolutionCloseAndTenantIsolation() throws Exception {
        String customerResponse = mockMvc.perform(post("/crm/api/in/v1/customers")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"countryCode\":\"US\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String customerId = objectMapper.readTree(customerResponse).at("/data/customerId").asText();

        String ticketResponse = mockMvc.perform(post("/crm/api/in/v1/tickets")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + customerId + "\",\"subject\":\"Late delivery\",\"description\":\"Package delayed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn().getResponse().getContentAsString();
        String ticketId = objectMapper.readTree(ticketResponse).at("/data/ticketId").asText();

        mockMvc.perform(patch("/crm/api/in/v1/tickets/" + ticketId + "/resolve")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"Refunded\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TICKET_STATUS_INVALID"));

        mockMvc.perform(patch("/crm/api/in/v1/tickets/" + ticketId + "/assign")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignee\":\"agent-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"));

        mockMvc.perform(patch("/crm/api/in/v1/tickets/" + ticketId + "/resolve")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"Refunded\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        mockMvc.perform(patch("/crm/api/in/v1/tickets/" + ticketId + "/close")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        mockMvc.perform(get("/crm/api/in/v1/tickets")
                        .header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}

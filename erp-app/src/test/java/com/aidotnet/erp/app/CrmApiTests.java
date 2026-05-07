package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
        String tenantId = uniqueTenant("crm-ticket");
        String otherTenantId = uniqueTenant("crm-ticket-other");
        String customerResponse = mockMvc.perform(post("/crm/api/in/v1/customers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"countryCode\":\"US\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String customerId = objectMapper.readTree(customerResponse).at("/data/customerId").asText();

        String ticketResponse = mockMvc.perform(post("/crm/api/in/v1/tickets")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + customerId + "\",\"subject\":\"Late delivery\",\"description\":\"Package delayed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn().getResponse().getContentAsString();
        String ticketId = objectMapper.readTree(ticketResponse).at("/data/ticketId").asText();

        mockMvc.perform(patch("/crm/api/in/v1/tickets/" + ticketId + "/resolve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"Refunded\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TICKET_STATUS_INVALID"));

        mockMvc.perform(patch("/crm/api/in/v1/tickets/" + ticketId + "/assign")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignee\":\"agent-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"));

        mockMvc.perform(patch("/crm/api/in/v1/tickets/" + ticketId + "/resolve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"Refunded\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        mockMvc.perform(patch("/crm/api/in/v1/tickets/" + ticketId + "/close")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        mockMvc.perform(get("/crm/api/in/v1/tickets")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    /**
     * PG 集成测试会复用同一 Spring 上下文和数据库实例。
     * 为避免不同用例之间租户数据串扰，每个场景使用独立租户编码。
     */
    private String uniqueTenant(String scenario) {
        return scenario + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

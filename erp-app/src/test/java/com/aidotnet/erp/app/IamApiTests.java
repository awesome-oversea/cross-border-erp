package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ErpAppTest
@AutoConfigureMockMvc
class IamApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginVerifyRbacTenantAndAuditFlow() throws Exception {
        String loginResponse = mockMvc.perform(post("/iam/api/in/v1/auth/login")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("X-Trace-Id", "trace-iam")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tenantId").value("tenant-demo"))
                .andExpect(jsonPath("$.data.permissions[0]").exists())
                .andReturn().getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String token = loginJson.at("/data/token").asText();

        mockMvc.perform(post("/iam/api/in/v1/auth/verify")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"));

        mockMvc.perform(get("/iam/api/in/v1/users")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(post("/iam/api/in/v1/users")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"operator\",\"password\":\"pwd123456\",\"roles\":[\"ADMIN\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value("tenant-demo"))
                .andExpect(jsonPath("$.data.username").value("operator"));

        mockMvc.perform(post("/iam/api/in/v1/tenants")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantId\":\"tenant-disabled\",\"name\":\"Disabled Tenant\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(patch("/iam/api/in/v1/tenants/tenant-disabled/disable")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISABLED"));

        mockMvc.perform(post("/iam/api/in/v1/auth/login")
                        .header("X-Tenant-Id", "tenant-disabled")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TENANT_DISABLED"));

        mockMvc.perform(get("/iam/api/in/v1/audits")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tenantId").value("tenant-demo"));
    }
}

package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class DashboardApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageDashboardMetricsCreateUpdateQueryAndTenantIsolation() throws Exception {
        String tenantId = uniqueTenant("dashboard-metrics");
        String otherTenantId = uniqueTenant("dashboard-metrics-other");
        String response = mockMvc.perform(post("/dashboard/api/in/v1/metrics")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metricCode\":\"today.sales\",\"metricName\":\"Today Sales\",\"metricValue\":1200.50,\"unit\":\"USD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCode").value("today.sales"))
                .andReturn().getResponse().getContentAsString();
        String metricId = objectMapper.readTree(response).at("/data/metricId").asText();

        mockMvc.perform(post("/dashboard/api/in/v1/metrics")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metricCode\":\"today.sales\",\"metricName\":\"Duplicated\",\"metricValue\":1,\"unit\":\"USD\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("METRIC_DUPLICATED"));

        mockMvc.perform(put("/dashboard/api/in/v1/metrics/" + metricId)
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metricCode\":\"today.sales\",\"metricName\":\"Today Sales\",\"metricValue\":1500.00,\"unit\":\"USD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricValue").value(1500.0));

        mockMvc.perform(get("/dashboard/api/in/v1/metrics/code/today.sales")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricName").value("Today Sales"));

        mockMvc.perform(get("/dashboard/api/in/v1/metrics")
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

package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class DashboardApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageDashboardMetricsCreateUpdateQueryAndTenantIsolation() throws Exception {
        String response = mockMvc.perform(post("/dashboard/api/in/v1/metrics")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metricCode\":\"today.sales\",\"metricName\":\"Today Sales\",\"metricValue\":1200.50,\"unit\":\"USD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricCode").value("today.sales"))
                .andReturn().getResponse().getContentAsString();
        String metricId = objectMapper.readTree(response).at("/data/metricId").asText();

        mockMvc.perform(post("/dashboard/api/in/v1/metrics")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metricCode\":\"today.sales\",\"metricName\":\"Duplicated\",\"metricValue\":1,\"unit\":\"USD\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("METRIC_DUPLICATED"));

        mockMvc.perform(put("/dashboard/api/in/v1/metrics/" + metricId)
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metricCode\":\"today.sales\",\"metricName\":\"Today Sales\",\"metricValue\":1500.00,\"unit\":\"USD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricValue").value(1500.0));

        mockMvc.perform(get("/dashboard/api/in/v1/metrics/code/today.sales")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricName").value("Today Sales"));

        mockMvc.perform(get("/dashboard/api/in/v1/metrics")
                        .header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}

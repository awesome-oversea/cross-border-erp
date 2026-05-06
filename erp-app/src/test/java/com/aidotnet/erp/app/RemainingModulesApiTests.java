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
class RemainingModulesApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageSomAdsFbaAndBiMinimumFlows() throws Exception {
        String storeResponse = mockMvc.perform(post("/som/api/in/v1/stores")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"platform\":\"Amazon\",\"storeCode\":\"US01\",\"storeName\":\"Amazon US\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andReturn().getResponse().getContentAsString();
        String storeId = objectMapper.readTree(storeResponse).at("/data/storeId").asText();
        mockMvc.perform(post("/som/api/in/v1/stores")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"platform\":\"Amazon\",\"storeCode\":\"US01\",\"storeName\":\"Duplicate\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("STORE_DUPLICATED"));
        mockMvc.perform(patch("/som/api/in/v1/stores/" + storeId + "/connect")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONNECTED"));

        String campaignResponse = mockMvc.perform(post("/ads/api/in/v1/campaigns")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"platform\":\"AmazonAds\",\"campaignName\":\"Launch\",\"dailyBudget\":50.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        String campaignId = objectMapper.readTree(campaignResponse).at("/data/campaignId").asText();
        mockMvc.perform(patch("/ads/api/in/v1/campaigns/" + campaignId + "/activate")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        mockMvc.perform(patch("/ads/api/in/v1/campaigns/" + campaignId + "/pause")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAUSED"));

        String fbaResponse = mockMvc.perform(post("/api/fba/shipments")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amazonShipmentId\":\"FBA123\",\"destinationFc\":\"ONT8\",\"plannedQuantity\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        String fbaShipmentId = objectMapper.readTree(fbaResponse).at("/data/fbaShipmentId").asText();
        mockMvc.perform(post("/api/fba/shipments")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amazonShipmentId\":\"FBA123\",\"destinationFc\":\"ONT8\",\"plannedQuantity\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FBA_SHIPMENT_DUPLICATED"));
        mockMvc.perform(patch("/api/fba/shipments/" + fbaShipmentId + "/submit")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
        mockMvc.perform(patch("/api/fba/shipments/" + fbaShipmentId + "/receive")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receivedQuantity\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        mockMvc.perform(post("/api/bi/reports")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportCode\":\"sales.daily\",\"reportName\":\"Daily Sales\",\"dataSource\":\"orders\",\"queryText\":\"select count(*) from orders\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportCode").value("sales.daily"));
        // BI 运行结果按快照返回，用于验证最小链路的真实运行契约。
        mockMvc.perform(post("/api/bi/reports/sales.daily/run")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value("tenant-demo"))
                .andExpect(jsonPath("$.data.snapshotName").value("Daily Sales"))
                .andExpect(jsonPath("$.data.snapshotData").value("{\"dataSource\":\"orders\",\"rows\":0}"))
                .andExpect(jsonPath("$.data.format").value("json"));

        mockMvc.perform(get("/som/api/in/v1/stores").header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
        mockMvc.perform(get("/ads/api/in/v1/campaigns").header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
        mockMvc.perform(get("/api/fba/shipments").header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
        mockMvc.perform(get("/api/bi/reports").header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}

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
class PdmApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageProductMasterDataWithTenantIsolationAndPublishRule() throws Exception {
        String categoryResponse = mockMvc.perform(post("/pdm/api/in/v1/categories")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Electronics\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value("tenant-demo"))
                .andReturn().getResponse().getContentAsString();
        String categoryId = objectMapper.readTree(categoryResponse).at("/data/categoryId").asText();

        String brandResponse = mockMvc.perform(post("/pdm/api/in/v1/brands")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"AIDotNet\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String brandId = objectMapper.readTree(brandResponse).at("/data/brandId").asText();

        String spuResponse = mockMvc.perform(post("/pdm/api/in/v1/products")
                        .header("X-Tenant-Id", "tenant-demo")
                        .header("X-Trace-Id", "trace-pdm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Bluetooth Speaker\",\"categoryId\":\"" + categoryId + "\",\"brandId\":\"" + brandId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        JsonNode spuJson = objectMapper.readTree(spuResponse);
        String spuId = spuJson.at("/data/spuId").asText();

        mockMvc.perform(patch("/pdm/api/in/v1/products/" + spuId + "/publish")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SKU_REQUIRED"));

        mockMvc.perform(post("/pdm/api/in/v1/products/" + spuId + "/skus")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sellerSku\":\"SPK-BLK-US\",\"title\":\"Bluetooth Speaker Black US\",\"weightKg\":0.80,\"declaredValue\":29.99,\"currency\":\"USD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sellerSku").value("SPK-BLK-US"));

        mockMvc.perform(post("/pdm/api/in/v1/products/" + spuId + "/skus")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sellerSku\":\"SPK-BLK-US\",\"title\":\"Duplicate\",\"weightKg\":0.80,\"declaredValue\":29.99,\"currency\":\"USD\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SKU_DUPLICATED"));

        mockMvc.perform(patch("/pdm/api/in/v1/products/" + spuId + "/publish")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(get("/pdm/api/in/v1/products")
                        .header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}

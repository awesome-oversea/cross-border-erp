package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class WmsApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageInventoryReceiveReserveReleaseDeductAndTenantIsolation() throws Exception {
        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"US-LA\",\"name\":\"Los Angeles Warehouse\",\"countryCode\":\"US\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value("tenant-demo"))
                .andReturn().getResponse().getContentAsString();
        String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();
        String stockBody = "{\"warehouseId\":\"" + warehouseId + "\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":10}";

        mockMvc.perform(post("/wms/api/in/v1/inventory/receive")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(10))
                .andExpect(jsonPath("$.data.available").value(10));

        mockMvc.perform(post("/wms/api/in/v1/inventory/reserve")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":\"" + warehouseId + "\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reserved").value(4))
                .andExpect(jsonPath("$.data.available").value(6));

        mockMvc.perform(post("/wms/api/in/v1/inventory/reserve")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":\"" + warehouseId + "\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":7}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVENTORY_NOT_ENOUGH"));

        mockMvc.perform(post("/wms/api/in/v1/inventory/release")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":\"" + warehouseId + "\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reserved").value(3));

        mockMvc.perform(post("/wms/api/in/v1/inventory/deduct")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":\"" + warehouseId + "\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(7))
                .andExpect(jsonPath("$.data.reserved").value(0));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WAREHOUSE_NOT_FOUND"));
    }
}

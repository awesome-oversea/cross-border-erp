package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ErpAppTest
@AutoConfigureMockMvc
class ScmApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageSupplierPurchaseOrderApprovalReceiptAndTenantIsolation() throws Exception {
        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"CN-SZ",
                                  "name":"Shenzhen Inbound Warehouse",
                                  "countryCode":"CN"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();

        String supplierResponse = mockMvc.perform(post("/scm/api/in/v1/suppliers")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Shenzhen Supplier\",\"contactName\":\"Bob\",\"countryCode\":\"CN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value("tenant-demo"))
                .andReturn().getResponse().getContentAsString();
        String supplierId = objectMapper.readTree(supplierResponse).at("/data/supplierId").asText();

        String poResponse = mockMvc.perform(post("/scm/api/in/v1/purchase-orders")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supplierId\":\"" + supplierId + "\",\"currency\":\"USD\",\"lines\":[{\"lineId\":\"L1\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":10,\"unitCost\":12.50}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.totalAmount").value(125.0))
                .andReturn().getResponse().getContentAsString();
        String poId = objectMapper.readTree(poResponse).at("/data/poId").asText();

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/approve")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PO_STATUS_INVALID"));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/submit")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/approve")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/receive")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"warehouseId\":\"" + warehouseId + "\"," +
                                "\"receipts\":[{\"lineId\":\"L1\",\"quantity\":4}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_RECEIVED"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].sellerSku").value("SPK-BLK-US"))
                .andExpect(jsonPath("$.data[0].onHand").value(4))
                .andExpect(jsonPath("$.data[0].reserved").value(0));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory-transactions")
                        .header("X-Tenant-Id", "tenant-demo")
                        .param("sellerSku", "SPK-BLK-US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].transactionType").value("RECEIVE"))
                .andExpect(jsonPath("$.data[0].quantity").value(4))
                .andExpect(jsonPath("$.data[0].referenceType").value("MANUAL_RECEIVE"));

        mockMvc.perform(get("/fms/api/in/v1/cost-events/by-source")
                        .header("X-Tenant-Id", "tenant-demo")
                        .param("sourceType", "SCM_PURCHASE_ORDER")
                        .param("sourceId", poId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].costType").value("PRODUCT_COST"))
                .andExpect(jsonPath("$.data[0].sellerSku").value("SPK-BLK-US"))
                .andExpect(jsonPath("$.data[0].amount").value(50.0));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/receive")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"warehouseId\":\"" + warehouseId + "\"," +
                                "\"receipts\":[{\"lineId\":\"L1\",\"quantity\":7}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PO_RECEIVE_EXCEEDS_ORDERED"));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/receive")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"warehouseId\":\"" + warehouseId + "\"," +
                                "\"receipts\":[{\"lineId\":\"L1\",\"quantity\":6}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECEIVED"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].onHand").value(10))
                .andExpect(jsonPath("$.data[0].reserved").value(0));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory-transactions")
                        .header("X-Tenant-Id", "tenant-demo")
                        .param("sellerSku", "SPK-BLK-US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].quantity", Matchers.containsInAnyOrder(4, 6)));

        mockMvc.perform(get("/fms/api/in/v1/cost-events/by-source")
                        .header("X-Tenant-Id", "tenant-demo")
                        .param("sourceType", "SCM_PURCHASE_ORDER")
                        .param("sourceId", poId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].amount", Matchers.containsInAnyOrder(50.0, 75.0)));

        mockMvc.perform(get("/scm/api/in/v1/purchase-orders")
                        .header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}

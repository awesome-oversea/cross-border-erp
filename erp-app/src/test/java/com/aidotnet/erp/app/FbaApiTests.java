package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ErpAppTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FbaApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageInboundPlanShipmentAndInventoryFlow() throws Exception {
        String inboundPlanResponse = mockMvc.perform(post("/fba/api/in/v1/inbound-plans")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"ONT8",
                                  "planName":"April Replenishment",
                                  "sellerSku":"SKU-001",
                                  "plannedQuantity":12,
                                  "storeId":"AMZ-US-01",
                                  "siteCode":"US"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.planName").value("April Replenishment"))
                .andReturn().getResponse().getContentAsString();
        String inboundPlanId = objectMapper.readTree(inboundPlanResponse).at("/data/planId").asText();

        mockMvc.perform(patch("/fba/api/in/v1/inbound-plans/" + inboundPlanId)
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plannedQuantity":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plannedQuantity").value(10));

        String splitResponse = mockMvc.perform(post("/fba/api/in/v1/inbound-plans/" + inboundPlanId + "/split")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newPlanName":"April Replenishment - Batch B",
                                  "splitQuantity":4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.parentPlan.status").value("SPLIT"))
                .andExpect(jsonPath("$.data.childPlan.plannedQuantity").value(4))
                .andReturn().getResponse().getContentAsString();
        String childPlanId = objectMapper.readTree(splitResponse).at("/data/childPlan/planId").asText();

        mockMvc.perform(patch("/fba/api/in/v1/inbound-plans/" + childPlanId + "/submit")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        String locationResponse = mockMvc.perform(post("/fba/api/in/v1/locations")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Amazon ONT8",
                                  "countryCode":"US",
                                  "address":"California",
                                  "locationType":"FBA",
                                  "status":"ACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.locationType").value("FBA"))
                .andReturn().getResponse().getContentAsString();
        String locationId = objectMapper.readTree(locationResponse).at("/data/locationId").asText();

        String shipmentResponse = mockMvc.perform(post("/fba/api/in/v1/shipments")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amazonShipmentId":"FBA-IN-001",
                                  "destinationFc":"ONT8",
                                  "planId":"%s",
                                  "carrier":"UPS",
                                  "plannedQuantity":4,
                                  "items":[
                                    {
                                      "productId":"PROD-001",
                                      "sellerSku":"SKU-001",
                                      "fnsku":"FNSKU-001",
                                      "quantity":4,
                                      "boxQuantity":2
                                    }
                                  ]
                                }
                                """.formatted(childPlanId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.planId").value(childPlanId))
                .andReturn().getResponse().getContentAsString();
        String shipmentId = objectMapper.readTree(shipmentResponse).at("/data/fbaShipmentId").asText();

        mockMvc.perform(get("/fba/api/in/v1/shipments/" + shipmentId + "/items")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerSku").value("SKU-001"));

        mockMvc.perform(patch("/fba/api/in/v1/shipments/" + shipmentId + "/submit")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        mockMvc.perform(put("/fba/api/in/v1/shipments/" + shipmentId + "/pack")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartonCount":2,
                                  "totalWeight":12.5,
                                  "carrier":"UPS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PACKED"))
                .andExpect(jsonPath("$.data.cartonCount").value(2));

        mockMvc.perform(post("/fba/api/in/v1/shipments/" + shipmentId + "/carton-labels")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartonId":"CTN-001",
                                  "sellerSku":"SKU-001",
                                  "quantityPerCarton":2,
                                  "numberOfCartons":2,
                                  "labelUrl":"https://example.com/label/CTN-001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cartonId").value("CTN-001"));

        mockMvc.perform(put("/fba/api/in/v1/shipments/" + shipmentId + "/ship")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "carrier":"UPS",
                                  "trackingNo":"1Z999AA10123456784"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPED"))
                .andExpect(jsonPath("$.data.trackingNo").value("1Z999AA10123456784"));

        mockMvc.perform(patch("/fba/api/in/v1/shipments/" + shipmentId + "/receive")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receivedQuantity":4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        String inventoryResponse = mockMvc.perform(post("/fba/api/in/v1/inventories/sync")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "productId":"PROD-001",
                                  "sellerSku":"SKU-001",
                                  "fnsku":"FNSKU-001",
                                  "quantity":4,
                                  "storeId":"AMZ-US-01",
                                  "siteCode":"US",
                                  "inventoryAgeDays":15
                                }
                                """.formatted(locationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(4))
                .andReturn().getResponse().getContentAsString();
        String inventoryId = objectMapper.readTree(inventoryResponse).at("/data/inventoryId").asText();

        mockMvc.perform(get("/fba/api/in/v1/inventories")
                        .header("X-Tenant-Id", "tenant-demo")
                        .param("sellerSku", "SKU-001")
                        .param("storeId", "AMZ-US-01")
                        .param("siteCode", "US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].inventoryAgeDays").value(15));

        mockMvc.perform(get("/fba/api/in/v1/inventories/" + inventoryId)
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inventoryId").value(inventoryId));

        mockMvc.perform(get("/fba/api/in/v1/inbound-plans")
                        .header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/fba/api/in/v1/inventories")
                        .header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}

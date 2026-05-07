package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ErpAppTest
@AutoConfigureMockMvc
class OmsApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateFulfillmentPlanReservesInventoryThenShipPersistsPlatformSyncLogs() throws Exception {
        String tenantId = uniqueTenant("oms-loop");
        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"US-NJ",
                                  "name":"New Jersey Warehouse",
                                  "countryCode":"US"
                                }
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();

        mockMvc.perform(post("/wms/api/in/v1/inventory/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "sellerSku":"SPK-BLK-US",
                                  "quantity":5,
                                  "referenceType":"BOOTSTRAP",
                                  "referenceId":"seed-1"
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/tms/api/v1/carriers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"UPS",
                                  "name":"UPS",
                                  "countryCode":"US",
                                  "apiEnabled":true
                                }
                                """))
                .andExpect(status().isOk());

        String orderResponse = mockMvc.perform(post("/oms/api/in/v1/orders/import")
                        .header("X-Tenant-Id", tenantId)
                        .header("X-Trace-Id", "trace-loop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platform":"Amazon",
                                  "platformOrderNo":"AMZ-LOOP-100",
                                  "buyerName":"Alice",
                                  "countryCode":"US",
                                  "shippingAddress":"1 Main St, Newark, NJ",
                                  "currency":"USD",
                                  "lines":[
                                    {
                                      "lineId":"L1",
                                      "sellerSku":"SPK-BLK-US",
                                      "title":"Speaker",
                                      "quantity":2,
                                      "unitPrice":29.99
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String orderId = objectMapper.readTree(orderResponse).at("/data/orderId").asText();

        mockMvc.perform(patch("/oms/api/in/v1/orders/" + orderId + "/paid")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));

        mockMvc.perform(post("/oms/api/in/v1/orders/" + orderId + "/fulfillment-plans/generate")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PLANNED"))
                .andExpect(jsonPath("$.data.packages.length()").value(1))
                .andExpect(jsonPath("$.data.packages[0].status").value("READY"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].onHand").value(5))
                .andExpect(jsonPath("$.data[0].reserved").value(2));

        mockMvc.perform(patch("/oms/api/in/v1/orders/" + orderId + "/ship")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));

        mockMvc.perform(get("/oms/api/in/v1/orders/" + orderId + "/platform-shipments/logs")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("SUCCESS"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].onHand").value(3))
                .andExpect(jsonPath("$.data[0].reserved").value(0));
    }

    @Test
    void importOrderWithIdempotencyTenantIsolationAndStatusFlow() throws Exception {
        String tenantId = uniqueTenant("oms-flow");
        String otherTenantId = uniqueTenant("oms-flow-other");
        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"US-LA",
                                  "name":"Los Angeles Warehouse",
                                  "countryCode":"US"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();

        mockMvc.perform(post("/wms/api/in/v1/inventory/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "sellerSku":"SPK-BLK-US",
                                  "quantity":5,
                                  "referenceType":"BOOTSTRAP",
                                  "referenceId":"seed-oms-1"
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/tms/api/v1/carriers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"UPS",
                                  "name":"UPS",
                                  "countryCode":"US",
                                  "apiEnabled":true
                                }
                                """))
                .andExpect(status().isOk());

        String body = "{\"platform\":\"Amazon\",\"platformOrderNo\":\"AMZ-10001\",\"buyerName\":\"Alice\",\"countryCode\":\"US\",\"shippingAddress\":\"1 Main St, Newark, NJ\",\"currency\":\"USD\",\"lines\":[{\"lineId\":\"L1\",\"sellerSku\":\"SPK-BLK-US\",\"title\":\"Speaker\",\"quantity\":2,\"unitPrice\":29.99}]}";
        String response = mockMvc.perform(post("/oms/api/in/v1/orders/import")
                        .header("X-Tenant-Id", tenantId)
                        .header("X-Trace-Id", "trace-oms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.totalAmount").value(59.98))
                .andReturn().getResponse().getContentAsString();
        String orderId = objectMapper.readTree(response).at("/data/orderId").asText();

        mockMvc.perform(post("/oms/api/in/v1/orders/import")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ORDER_DUPLICATED"));

        mockMvc.perform(patch("/oms/api/in/v1/orders/" + orderId + "/ship")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ORDER_STATUS_INVALID"));

        mockMvc.perform(patch("/oms/api/in/v1/orders/" + orderId + "/paid")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));

        mockMvc.perform(post("/oms/api/in/v1/orders/" + orderId + "/fulfillment-plans/generate")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PLANNED"));

        mockMvc.perform(patch("/oms/api/in/v1/orders/" + orderId + "/ship")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));

        mockMvc.perform(patch("/oms/api/in/v1/orders/" + orderId + "/cancel")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ORDER_STATUS_INVALID"));

        mockMvc.perform(get("/oms/api/in/v1/orders")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    /**
     * PG 集成测试会复用 Spring 上下文与数据库实例。
     * 为避免跨用例、跨测试类的租户数据串扰，每个场景使用独立租户编号。
     */
    private String uniqueTenant(String scenario) {
        return scenario + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

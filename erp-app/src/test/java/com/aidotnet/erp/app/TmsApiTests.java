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
class TmsApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageCarrierShipmentTrackingDeliveryAndTenantIsolation() throws Exception {
        String tenantId = uniqueTenant("tms-shipment");
        String otherTenantId = uniqueTenant("tms-shipment-other");
        String carrierResponse = mockMvc.perform(post("/tms/api/v1/carriers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"UPS\",\"name\":\"UPS\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String carrierId = objectMapper.readTree(carrierResponse).at("/data/carrierId").asText();

        String shipmentBody = "{\"orderId\":\"order-1\",\"carrierId\":\"" + carrierId + "\",\"trackingNo\":\"1Z999\",\"destinationCountry\":\"US\"}";
        String shipmentResponse = mockMvc.perform(post("/tms/api/v1/shipments")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shipmentBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andReturn().getResponse().getContentAsString();
        String shipmentId = objectMapper.readTree(shipmentResponse).at("/data/shipmentId").asText();

        mockMvc.perform(post("/tms/api/v1/shipments")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shipmentBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TRACKING_NO_DUPLICATED"));

        mockMvc.perform(patch("/tms/api/v1/shipments/" + shipmentId + "/tracking")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_TRANSIT\",\"location\":\"LA\",\"description\":\"Arrived at facility\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.data.trackingEvents.length()").value(1));

        mockMvc.perform(patch("/tms/api/v1/shipments/" + shipmentId + "/tracking")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DELIVERED\",\"location\":\"NY\",\"description\":\"Delivered\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELIVERED"));

        mockMvc.perform(patch("/tms/api/v1/shipments/" + shipmentId + "/cancel")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SHIPMENT_STATUS_INVALID"));

        mockMvc.perform(get("/tms/api/v1/shipments")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void manageCarrierMethodsAndAuthorizationVisibilityShouldSatisfyP2037() throws Exception {
        String tenantId = uniqueTenant("tms-methods");
        String carrierBody = """
                {
                  "code":"4PX",
                  "name":"4PX Express",
                  "countryCode":"CN",
                  "type":"EXPRESS",
                  "contactPerson":"Li Wei",
                  "phone":"13800000000",
                  "status":"ACTIVE",
                  "apiEnabled":true,
                  "featured":true,
                  "authorizationStatus":"AUTHORIZED",
                  "authorizationValidUntil":"2030-12-31T00:00:00Z",
                  "lastAuthorizedAt":"2030-01-01T00:00:00Z"
                }
                """;

        String carrierResponse = mockMvc.perform(post("/tms/api/v1/carriers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carrierBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("4PX"))
                .andExpect(jsonPath("$.data.authorizationStatus").value("AUTHORIZED"))
                .andExpect(jsonPath("$.data.featured").value(true))
                .andReturn().getResponse().getContentAsString();
        String carrierId = objectMapper.readTree(carrierResponse).at("/data/carrierId").asText();

        mockMvc.perform(get("/tms/api/v1/carriers")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].carrierId").value(carrierId))
                .andExpect(jsonPath("$.data[0].authorizationStatus").value("AUTHORIZED"))
                .andExpect(jsonPath("$.data[0].featured").value(true))
                .andExpect(jsonPath("$.data[0].apiEnabled").value(true));

        String methodBody = """
                {
                  "methodCode":"AIR_STD",
                  "methodName":"Air Standard",
                  "transportMode":"AIR",
                  "rateType":"WEIGHT_BASED",
                  "estimatedDaysMin":5,
                  "estimatedDaysMax":7,
                  "enabled":true
                }
                """;

        String methodResponse = mockMvc.perform(post("/tms/api/v1/carriers/" + carrierId + "/methods")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(methodBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.methodCode").value("AIR_STD"))
                .andExpect(jsonPath("$.data.transportMode").value("AIR"))
                .andExpect(jsonPath("$.data.rateType").value("WEIGHT_BASED"))
                .andReturn().getResponse().getContentAsString();
        String methodId = objectMapper.readTree(methodResponse).at("/data/methodId").asText();

        mockMvc.perform(get("/tms/api/v1/carriers/" + carrierId + "/methods")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].methodId").value(methodId))
                .andExpect(jsonPath("$.data[0].methodCode").value("AIR_STD"))
                .andExpect(jsonPath("$.data[0].methodName").value("Air Standard"));

        String channelRuleBody = """
                {
                  "zoneCode":"US_WEST",
                  "weightMinKg":0,
                  "weightMaxKg":2,
                  "baseCost":55.50,
                  "costPerKg":8.80,
                  "currency":"USD",
                  "effectiveFrom":"2030-01-01T00:00:00Z",
                  "effectiveTo":"2030-12-31T00:00:00Z"
                }
                """;

        mockMvc.perform(post("/tms/api/v1/carriers/" + carrierId + "/methods/" + methodId + "/channel-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(channelRuleBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.zoneCode").value("US_WEST"))
                .andExpect(jsonPath("$.data.baseCost").value(55.5))
                .andExpect(jsonPath("$.data.costPerKg").value(8.8));

        mockMvc.perform(get("/tms/api/v1/carriers/" + carrierId + "/methods/" + methodId + "/channel-rules")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].zoneCode").value("US_WEST"))
                .andExpect(jsonPath("$.data[0].currency").value("USD"));
    }

    @Test
    void estimateFreightBatchShipmentsAndCompareActualFreightShouldSatisfyP2038() throws Exception {
        String tenantId = uniqueTenant("tms-freight");
        String carrierResponse = mockMvc.perform(post("/tms/api/v1/carriers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"YW",
                                  "name":"Yanwen",
                                  "countryCode":"CN",
                                  "type":"STANDARD",
                                  "status":"ACTIVE",
                                  "apiEnabled":true,
                                  "authorizationStatus":"AUTHORIZED"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String carrierId = objectMapper.readTree(carrierResponse).at("/data/carrierId").asText();

        String methodResponse = mockMvc.perform(post("/tms/api/v1/carriers/" + carrierId + "/methods")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "methodCode":"CN_US_STD",
                                  "methodName":"CN to US Standard",
                                  "transportMode":"AIR",
                                  "rateType":"WEIGHT_BASED",
                                  "estimatedDaysMin":6,
                                  "estimatedDaysMax":9,
                                  "enabled":true
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String methodId = objectMapper.readTree(methodResponse).at("/data/methodId").asText();

        mockMvc.perform(post("/tms/api/v1/carriers/" + carrierId + "/methods/" + methodId + "/channel-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originCountry":"CN",
                                  "destinationCountry":"US",
                                  "zoneCode":"US_MAIN",
                                  "weightMinKg":0,
                                  "weightMaxKg":5,
                                  "baseCost":55.50,
                                  "costPerKg":8.80,
                                  "currency":"USD",
                                  "effectiveFrom":"2020-01-01T00:00:00Z",
                                  "effectiveTo":"2030-12-31T00:00:00Z"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/tms/api/v1/shipping-rates/estimate")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "carrierId":"%s",
                                  "shippingMethodId":"%s",
                                  "originCountry":"CN",
                                  "destinationCountry":"US",
                                  "weight":1.25
                                }
                                """.formatted(carrierId, methodId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.carrierId").value(carrierId))
                .andExpect(jsonPath("$.data.shippingMethodId").value(methodId))
                .andExpect(jsonPath("$.data.zoneCode").value("US_MAIN"))
                .andExpect(jsonPath("$.data.chargeableWeight").value(1.25))
                .andExpect(jsonPath("$.data.estimatedFreight").value(66.5))
                .andExpect(jsonPath("$.data.currency").value("USD"));

        String shipmentResponse = mockMvc.perform(post("/tms/api/v1/shipments")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId":"order-p2038",
                                  "warehouseId":"wh-cn-1",
                                  "carrierId":"%s",
                                  "shippingMethodId":"%s",
                                  "trackingNo":"YW2038001",
                                  "destinationCountry":"US",
                                  "weight":1.25
                                }
                                """.formatted(carrierId, methodId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.estimatedFreight").value(66.5))
                .andExpect(jsonPath("$.data.estimatedFreightCurrency").value("USD"))
                .andReturn().getResponse().getContentAsString();
        String shipmentId = objectMapper.readTree(shipmentResponse).at("/data/shipmentId").asText();

        String batchResponse = mockMvc.perform(post("/tms/api/v1/shipping-batches")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "carrierId":"%s",
                                  "shipmentIds":["%s"]
                                }
                                """.formatted(carrierId, shipmentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        String batchId = objectMapper.readTree(batchResponse).at("/data/batchId").asText();

        mockMvc.perform(patch("/tms/api/v1/shipping-batches/" + batchId + "/submit")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        mockMvc.perform(patch("/tms/api/v1/shipping-batches/" + batchId + "/in-transit")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"));

        mockMvc.perform(get("/tms/api/v1/shipments")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].shipmentId").value(shipmentId))
                .andExpect(jsonPath("$.data[0].status").value("IN_TRANSIT"));

        mockMvc.perform(patch("/tms/api/v1/shipping-batches/" + batchId + "/complete")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(post("/tms/api/v1/shipping-costs")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shipmentId":"%s",
                                  "carrierId":"%s",
                                  "freightCost":70.00,
                                  "fuelSurcharge":5.00,
                                  "otherFees":1.00,
                                  "currency":"USD"
                                }
                                """.formatted(shipmentId, carrierId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCost").value(76.0));

        mockMvc.perform(get("/tms/api/v1/shipments/" + shipmentId + "/freight-difference")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shipmentId").value(shipmentId))
                .andExpect(jsonPath("$.data.estimatedFreight").value(66.5))
                .andExpect(jsonPath("$.data.actualFreight").value(76.0))
                .andExpect(jsonPath("$.data.freightDifference").value(9.5))
                .andExpect(jsonPath("$.data.currency").value("USD"));

        mockMvc.perform(get("/tms/api/v1/shipping-batches/" + batchId + "/freight-differences")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].shipmentId").value(shipmentId))
                .andExpect(jsonPath("$.data[0].freightDifference").value(9.5));
    }

    /**
     * PG 集成测试会复用 Spring 上下文与数据库实例。
     * 为避免跨用例、跨测试类的租户数据串扰，每个场景使用独立租户编号。
     */
    private String uniqueTenant(String scenario) {
        return scenario + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

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
class TmsApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageCarrierShipmentTrackingDeliveryAndTenantIsolation() throws Exception {
        String carrierResponse = mockMvc.perform(post("/tms/api/in/v1/carriers")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"UPS\",\"name\":\"UPS\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String carrierId = objectMapper.readTree(carrierResponse).at("/data/carrierId").asText();

        String shipmentBody = "{\"orderId\":\"order-1\",\"carrierId\":\"" + carrierId + "\",\"trackingNo\":\"1Z999\",\"destinationCountry\":\"US\"}";
        String shipmentResponse = mockMvc.perform(post("/tms/api/in/v1/shipments")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shipmentBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andReturn().getResponse().getContentAsString();
        String shipmentId = objectMapper.readTree(shipmentResponse).at("/data/shipmentId").asText();

        mockMvc.perform(post("/tms/api/in/v1/shipments")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shipmentBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TRACKING_NO_DUPLICATED"));

        mockMvc.perform(patch("/tms/api/in/v1/shipments/" + shipmentId + "/tracking")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_TRANSIT\",\"location\":\"LA\",\"description\":\"Arrived at facility\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.data.trackingEvents.length()").value(1));

        mockMvc.perform(patch("/tms/api/in/v1/shipments/" + shipmentId + "/tracking")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DELIVERED\",\"location\":\"NY\",\"description\":\"Delivered\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELIVERED"));

        mockMvc.perform(patch("/tms/api/in/v1/shipments/" + shipmentId + "/cancel")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SHIPMENT_STATUS_INVALID"));

        mockMvc.perform(get("/tms/api/in/v1/shipments")
                        .header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}

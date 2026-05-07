package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
        String tenantId = uniqueTenant("wms-balance");
        String otherTenantId = uniqueTenant("wms-balance-other");
        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"US-LA\",\"name\":\"Los Angeles Warehouse\",\"countryCode\":\"US\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value(tenantId))
                .andReturn().getResponse().getContentAsString();
        String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();
        String stockBody = "{\"warehouseId\":\"" + warehouseId + "\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":10}";

        mockMvc.perform(post("/wms/api/in/v1/inventory/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(10))
                .andExpect(jsonPath("$.data.available").value(10));

        mockMvc.perform(post("/wms/api/in/v1/inventory/reserve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":\"" + warehouseId + "\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reserved").value(4))
                .andExpect(jsonPath("$.data.available").value(6));

        mockMvc.perform(post("/wms/api/in/v1/inventory/reserve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":\"" + warehouseId + "\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":7}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVENTORY_NOT_ENOUGH"));

        mockMvc.perform(post("/wms/api/in/v1/inventory/release")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":\"" + warehouseId + "\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reserved").value(3));

        mockMvc.perform(post("/wms/api/in/v1/inventory/deduct")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":\"" + warehouseId + "\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(7))
                .andExpect(jsonPath("$.data.reserved").value(0));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WAREHOUSE_NOT_FOUND"));
    }

    @Test
    void receiveInboundQualityCheckAndPutawayShouldSplitGoodAndDefectiveInventory() throws Exception {
        String tenantId = uniqueTenant("wms-inbound");
        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"CN-HZ",
                                  "name":"Hangzhou QC Warehouse",
                                  "countryCode":"CN"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();

        String inboundResponse = mockMvc.perform(post("/wms/api/in/v1/inbound-orders")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "referenceType":"PURCHASE_ORDER",
                                  "referenceId":"PO-IN-1001",
                                  "remark":"采购收货入库",
                                  "lines":[
                                    {
                                      "sellerSku":"QC-SKU-001",
                                      "locationId":"A-01-01",
                                      "expectedQuantity":10,
                                      "unitCost":18.50,
                                      "batchNo":"LOT-20260507"
                                    }
                                  ]
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        String inboundOrderId = objectMapper.readTree(inboundResponse).at("/data/orderId").asText();

        String linesResponse = mockMvc.perform(get("/wms/api/in/v1/inbound-orders/" + inboundOrderId + "/lines")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        String lineId = objectMapper.readTree(linesResponse).at("/data/0/lineId").asText();

        mockMvc.perform(post("/wms/api/in/v1/inbound-orders/" + inboundOrderId + "/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receipts":[
                                    {
                                      "lineId":"%s",
                                      "receivedQuantity":10
                                    }
                                  ]
                                }
                                """.formatted(lineId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].sellerSku").value("QC-SKU-001"))
                .andExpect(jsonPath("$.data[0].onHand").value(10))
                .andExpect(jsonPath("$.data[0].frozen").value(10));

        mockMvc.perform(get("/wms/api/in/v1/inventory/QC-SKU-001/availability")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(10))
                .andExpect(jsonPath("$.data.available").value(0));

        mockMvc.perform(post("/wms/api/in/v1/quality-checks")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "inboundOrderId":"%s",
                                  "sellerSku":"QC-SKU-001",
                                  "sampleQuantity":10,
                                  "passQuantity":7,
                                  "failQuantity":3,
                                  "inspector":"qc-user-1",
                                  "remark":"3件外观破损"
                                }
                                """.formatted(warehouseId, inboundOrderId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").value("PARTIAL_PASS"))
                .andExpect(jsonPath("$.data.passQuantity").value(7))
                .andExpect(jsonPath("$.data.failQuantity").value(3));

        mockMvc.perform(get("/wms/api/in/v1/inbound-orders/" + inboundOrderId + "/quality-checks")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].result").value("PARTIAL_PASS"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].onHand").value(10))
                .andExpect(jsonPath("$.data[0].frozen").value(3));

        mockMvc.perform(get("/wms/api/in/v1/inventory/QC-SKU-001/availability")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(10))
                .andExpect(jsonPath("$.data.available").value(7));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory-transactions")
                        .header("X-Tenant-Id", tenantId)
                        .param("sellerSku", "QC-SKU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].referenceType", Matchers.hasItems(
                        "INBOUND_RECEIVE", "INBOUND_INSPECTION_HOLD", "QUALITY_CHECK_RELEASE")));
    }

    @Test
    void outboundPickPackWeighShipShouldDeductReservedInventoryAndMarkPackageShipped() throws Exception {
        String tenantId = uniqueTenant("wms-outbound");
        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"US-NJ",
                                  "name":"New Jersey Fulfillment Center",
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
                                  "sellerSku":"OUT-SKU-001",
                                  "quantity":8,
                                  "referenceType":"MANUAL_RECEIVE",
                                  "referenceId":"RCV-OUT-1001"
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(8))
                .andExpect(jsonPath("$.data.available").value(8));

        mockMvc.perform(post("/wms/api/in/v1/inventory/reserve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "sellerSku":"OUT-SKU-001",
                                  "quantity":5,
                                  "referenceType":"OMS_ALLOCATION",
                                  "referenceId":"SO-OUT-1001"
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reserved").value(5))
                .andExpect(jsonPath("$.data.available").value(3));

        String outboundResponse = mockMvc.perform(post("/wms/api/in/v1/outbound-orders")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "referenceType":"SALES_ORDER",
                                  "referenceId":"SO-OUT-1001",
                                  "remark":"销售订单出库",
                                  "lines":[
                                    {
                                      "sellerSku":"OUT-SKU-001",
                                      "locationId":"PK-01-01",
                                      "requiredQuantity":5,
                                      "batchNo":"OUT-LOT-20260507"
                                    }
                                  ]
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        String outboundOrderId = objectMapper.readTree(outboundResponse).at("/data/orderId").asText();

        String linesResponse = mockMvc.perform(get("/wms/api/in/v1/outbound-orders/" + outboundOrderId + "/lines")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        String lineId = objectMapper.readTree(linesResponse).at("/data/0/lineId").asText();

        mockMvc.perform(post("/wms/api/in/v1/outbound-orders/" + outboundOrderId + "/pick")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "picks":[
                                    {
                                      "lineId":"%s",
                                      "pickedQuantity":5
                                    }
                                  ]
                                }
                                """.formatted(lineId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PICKING"));

        String packageResponse = mockMvc.perform(post("/wms/api/in/v1/outbound-orders/" + outboundOrderId + "/packages")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "remark":"订单打包出库",
                                  "lines":[
                                    {
                                      "lineId":"%s",
                                      "quantity":5
                                    }
                                  ]
                                }
                                """.formatted(lineId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PACKED"))
                .andReturn().getResponse().getContentAsString();
        String packageId = objectMapper.readTree(packageResponse).at("/data/packageId").asText();

        mockMvc.perform(get("/wms/api/in/v1/outbound-orders/" + outboundOrderId + "/packages")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("PACKED"));

        mockMvc.perform(post("/wms/api/in/v1/packages/" + packageId + "/weigh")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "weightKg":2.35
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("WEIGHED"))
                .andExpect(jsonPath("$.data.weightKg").value(2.35));

        mockMvc.perform(post("/wms/api/in/v1/packages/" + packageId + "/ship")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "carrierCode":"DHL",
                                  "trackingNo":"DHL-OUT-1001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPED"))
                .andExpect(jsonPath("$.data.carrierCode").value("DHL"))
                .andExpect(jsonPath("$.data.trackingNo").value("DHL-OUT-1001"));

        mockMvc.perform(get("/wms/api/in/v1/outbound-orders/" + outboundOrderId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));

        mockMvc.perform(get("/wms/api/in/v1/outbound-orders/" + outboundOrderId + "/packages")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("SHIPPED"))
                .andExpect(jsonPath("$.data[0].weightKg").value(2.35));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerSku").value("OUT-SKU-001"))
                .andExpect(jsonPath("$.data[0].onHand").value(3))
                .andExpect(jsonPath("$.data[0].reserved").value(0))
                .andExpect(jsonPath("$.data[0].available").value(3));

        mockMvc.perform(get("/wms/api/in/v1/inventory/OUT-SKU-001/availability")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(3))
                .andExpect(jsonPath("$.data.reserved").value(0))
                .andExpect(jsonPath("$.data.available").value(3));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory-transactions")
                        .header("X-Tenant-Id", tenantId)
                        .param("sellerSku", "OUT-SKU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].referenceType", Matchers.hasItems(
                        "MANUAL_RECEIVE", "OMS_ALLOCATION", "OUTBOUND_SHIP")));
    }

    @Test
    void transferShipReceiveShouldTrackInTransitAndReceiptInventory() throws Exception {
        String tenantId = uniqueTenant("wms-transfer");
        String fromWarehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"CN-SZ",
                                  "name":"Shenzhen Main Warehouse",
                                  "countryCode":"CN"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String fromWarehouseId = objectMapper.readTree(fromWarehouseResponse).at("/data/warehouseId").asText();

        String toWarehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"US-LAX",
                                  "name":"Los Angeles Transit Warehouse",
                                  "countryCode":"US"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String toWarehouseId = objectMapper.readTree(toWarehouseResponse).at("/data/warehouseId").asText();

        mockMvc.perform(post("/wms/api/in/v1/inventory/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "sellerSku":"TR-SKU-001",
                                  "quantity":6,
                                  "referenceType":"MANUAL_RECEIVE",
                                  "referenceId":"RCV-TR-1001"
                                }
                                """.formatted(fromWarehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(6))
                .andExpect(jsonPath("$.data.available").value(6));

        String transferResponse = mockMvc.perform(post("/wms/api/in/v1/transfers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromWarehouseId":"%s",
                                  "toWarehouseId":"%s",
                                  "remark":"cross-border replenishment transfer",
                                  "lines":[
                                    {
                                      "sellerSku":"TR-SKU-001",
                                      "transferQuantity":4,
                                      "unitCost":25.80,
                                      "batchNo":"TR-LOT-20260507"
                                    }
                                  ]
                                }
                                """.formatted(fromWarehouseId, toWarehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        String transferId = objectMapper.readTree(transferResponse).at("/data/transferId").asText();

        String transferLinesResponse = mockMvc.perform(get("/wms/api/in/v1/transfers/" + transferId + "/lines")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        String transferLineId = objectMapper.readTree(transferLinesResponse).at("/data/0/lineId").asText();

        mockMvc.perform(patch("/wms/api/in/v1/transfers/" + transferId + "/ship")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + fromWarehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerSku").value("TR-SKU-001"))
                .andExpect(jsonPath("$.data[0].onHand").value(2))
                .andExpect(jsonPath("$.data[0].inTransit").value(0));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + toWarehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].sellerSku").value("TR-SKU-001"))
                .andExpect(jsonPath("$.data[0].onHand").value(0))
                .andExpect(jsonPath("$.data[0].inTransit").value(4))
                .andExpect(jsonPath("$.data[0].available").value(0));

        mockMvc.perform(patch("/wms/api/in/v1/transfers/" + transferId + "/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {
                                    "lineId":"%s",
                                    "receivedQuantity":4
                                  }
                                ]
                                """.formatted(transferLineId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECEIVED"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + toWarehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerSku").value("TR-SKU-001"))
                .andExpect(jsonPath("$.data[0].onHand").value(4))
                .andExpect(jsonPath("$.data[0].inTransit").value(0))
                .andExpect(jsonPath("$.data[0].available").value(4));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + fromWarehouseId + "/inventory-transactions")
                        .header("X-Tenant-Id", tenantId)
                        .param("sellerSku", "TR-SKU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].referenceType", Matchers.hasItems(
                        "MANUAL_RECEIVE", "TRANSFER_OUT")));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + toWarehouseId + "/inventory-transactions")
                        .header("X-Tenant-Id", tenantId)
                        .param("sellerSku", "TR-SKU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].referenceType", Matchers.hasItems(
                        "TRANSFER_IN_TRANSIT", "TRANSFER_IN_RECEIVE")));
    }

    @Test
    void stockCheckCountCompleteAdjustShouldRecordGainAndLoss() throws Exception {
        String tenantId = uniqueTenant("wms-stock-check");
        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"CN-NB",
                                  "name":"Ningbo Inventory Check Warehouse",
                                  "countryCode":"CN"
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
                                  "sellerSku":"CHK-SKU-GAIN",
                                  "quantity":5,
                                  "referenceType":"MANUAL_RECEIVE",
                                  "referenceId":"RCV-CHK-1001"
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(5));

        mockMvc.perform(post("/wms/api/in/v1/inventory/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "sellerSku":"CHK-SKU-LOSS",
                                  "quantity":4,
                                  "referenceType":"MANUAL_RECEIVE",
                                  "referenceId":"RCV-CHK-1002"
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onHand").value(4));

        String checkOrderResponse = mockMvc.perform(post("/wms/api/in/v1/stock-checks")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "checkType":"FULL_CHECK",
                                  "checkedBy":"checker-001",
                                  "remark":"monthly inventory counting",
                                  "lines":[
                                    {
                                      "sellerSku":"CHK-SKU-GAIN",
                                      "locationId":"A-01-01"
                                    },
                                    {
                                      "sellerSku":"CHK-SKU-LOSS",
                                      "locationId":"A-01-02"
                                    }
                                  ]
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        String checkOrderId = objectMapper.readTree(checkOrderResponse).at("/data/checkOrderId").asText();

        String checkLinesResponse = mockMvc.perform(get("/wms/api/in/v1/stock-checks/" + checkOrderId + "/lines")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].systemQuantity", Matchers.containsInAnyOrder(5, 4)))
                .andReturn().getResponse().getContentAsString();
        String gainLineId = objectMapper.readTree(checkLinesResponse).findValuesAsText("sellerSku").get(0)
                .equals("CHK-SKU-GAIN")
                ? objectMapper.readTree(checkLinesResponse).at("/data/0/lineId").asText()
                : objectMapper.readTree(checkLinesResponse).at("/data/1/lineId").asText();
        String lossLineId = objectMapper.readTree(checkLinesResponse).findValuesAsText("sellerSku").get(0)
                .equals("CHK-SKU-LOSS")
                ? objectMapper.readTree(checkLinesResponse).at("/data/0/lineId").asText()
                : objectMapper.readTree(checkLinesResponse).at("/data/1/lineId").asText();

        mockMvc.perform(patch("/wms/api/in/v1/stock-checks/" + checkOrderId + "/lines/" + gainLineId + "/count")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actualQuantity":7
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COUNTED"))
                .andExpect(jsonPath("$.data.difference").value(2));

        mockMvc.perform(patch("/wms/api/in/v1/stock-checks/" + checkOrderId + "/lines/" + lossLineId + "/count")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actualQuantity":0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COUNTED"))
                .andExpect(jsonPath("$.data.difference").value(-4));

        mockMvc.perform(patch("/wms/api/in/v1/stock-checks/" + checkOrderId + "/complete")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(patch("/wms/api/in/v1/stock-checks/" + checkOrderId + "/adjust")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ADJUSTED"));

        mockMvc.perform(get("/wms/api/in/v1/stock-checks/" + checkOrderId + "/lines")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].status", Matchers.everyItem(Matchers.is("ADJUSTED"))))
                .andExpect(jsonPath("$.data[*].difference", Matchers.containsInAnyOrder(2, -4)));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[?(@.sellerSku=='CHK-SKU-GAIN')].onHand").value(Matchers.contains(7)))
                .andExpect(jsonPath("$.data[?(@.sellerSku=='CHK-SKU-LOSS')].onHand").value(Matchers.contains(0)));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory-transactions")
                        .header("X-Tenant-Id", tenantId)
                        .param("sellerSku", "CHK-SKU-GAIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].referenceType", Matchers.hasItems(
                        "MANUAL_RECEIVE", "STOCK_CHECK_GAIN")));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory-transactions")
                        .header("X-Tenant-Id", tenantId)
                        .param("sellerSku", "CHK-SKU-LOSS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].referenceType", Matchers.hasItems(
                        "MANUAL_RECEIVE", "STOCK_CHECK_LOSS")));
    }

    @Test
    void defectiveReturnProcessShouldUpdateDefectiveLedgerAndInventory() throws Exception {
        String tenantId = uniqueTenant("wms-defective-return");
        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"CN-QD",
                                  "name":"Qingdao Defective Warehouse",
                                  "countryCode":"CN"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();

        String inboundResponse = mockMvc.perform(post("/wms/api/in/v1/inbound-orders")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "referenceType":"PURCHASE_ORDER",
                                  "referenceId":"PO-DEF-1001",
                                  "remark":"defective return inbound",
                                  "lines":[
                                    {
                                      "sellerSku":"DEF-SKU-001",
                                      "locationId":"D-01-01",
                                      "expectedQuantity":10,
                                      "unitCost":19.80,
                                      "batchNo":"DEF-LOT-20260507"
                                    }
                                  ]
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String inboundOrderId = objectMapper.readTree(inboundResponse).at("/data/orderId").asText();

        String inboundLinesResponse = mockMvc.perform(get("/wms/api/in/v1/inbound-orders/" + inboundOrderId + "/lines")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String inboundLineId = objectMapper.readTree(inboundLinesResponse).at("/data/0/lineId").asText();

        mockMvc.perform(post("/wms/api/in/v1/inbound-orders/" + inboundOrderId + "/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receipts":[
                                    {
                                      "lineId":"%s",
                                      "receivedQuantity":10
                                    }
                                  ]
                                }
                                """.formatted(inboundLineId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(post("/wms/api/in/v1/quality-checks")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "inboundOrderId":"%s",
                                  "sellerSku":"DEF-SKU-001",
                                  "sampleQuantity":10,
                                  "passQuantity":6,
                                  "failQuantity":4,
                                  "inspector":"qc-def-001",
                                  "remark":"four units need supplier return"
                                }
                                """.formatted(warehouseId, inboundOrderId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").value("PARTIAL_PASS"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerSku").value("DEF-SKU-001"))
                .andExpect(jsonPath("$.data[0].onHand").value(10))
                .andExpect(jsonPath("$.data[0].frozen").value(4))
                .andExpect(jsonPath("$.data[0].available").value(6));

        String defectiveReturnResponse = mockMvc.perform(post("/wms/api/in/v1/defective-returns")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "poId":"PO-DEF-1001",
                                  "supplierId":"SUP-DEF-001",
                                  "sellerSku":"DEF-SKU-001",
                                  "quantity":4,
                                  "reason":"packaging broken",
                                  "remark":"request supplier return"
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.quantity").value(4))
                .andReturn().getResponse().getContentAsString();
        String defectiveReturnId = objectMapper.readTree(defectiveReturnResponse).at("/data/returnId").asText();

        mockMvc.perform(patch("/wms/api/in/v1/defective-returns/" + defectiveReturnId + "/process")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierReply":"RETURN",
                                  "processedBy":"buyer-001",
                                  "remark":"return goods to supplier"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.supplierReply").value("RETURN"))
                .andExpect(jsonPath("$.data.processedBy").value("buyer-001"));

        mockMvc.perform(get("/wms/api/in/v1/defective-returns/" + defectiveReturnId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.returnId").value(defectiveReturnId))
                .andExpect(jsonPath("$.data.poId").value("PO-DEF-1001"))
                .andExpect(jsonPath("$.data.supplierId").value("SUP-DEF-001"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.supplierReply").value("RETURN"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerSku").value("DEF-SKU-001"))
                .andExpect(jsonPath("$.data[0].onHand").value(6))
                .andExpect(jsonPath("$.data[0].frozen").value(0))
                .andExpect(jsonPath("$.data[0].available").value(6));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory-transactions")
                        .header("X-Tenant-Id", tenantId)
                        .param("sellerSku", "DEF-SKU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].referenceType", Matchers.hasItems(
                        "INBOUND_RECEIVE", "QUALITY_CHECK_RELEASE", "DEFECTIVE_RETURN_OUTBOUND")));
    }

    @Test
    void productRepairCompleteShouldReturnQualifiedInventoryFromDefectiveStock() throws Exception {
        String tenantId = uniqueTenant("wms-product-repair");
        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"CN-SH",
                                  "name":"Shanghai Repair Warehouse",
                                  "countryCode":"CN"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();

        String inboundResponse = mockMvc.perform(post("/wms/api/in/v1/inbound-orders")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "referenceType":"PURCHASE_ORDER",
                                  "referenceId":"PO-RPR-1001",
                                  "remark":"repair inbound",
                                  "lines":[
                                    {
                                      "sellerSku":"RPR-SKU-001",
                                      "locationId":"R-01-01",
                                      "expectedQuantity":8,
                                      "unitCost":12.60,
                                      "batchNo":"RPR-LOT-20260507"
                                    }
                                  ]
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String inboundOrderId = objectMapper.readTree(inboundResponse).at("/data/orderId").asText();

        String inboundLinesResponse = mockMvc.perform(get("/wms/api/in/v1/inbound-orders/" + inboundOrderId + "/lines")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String inboundLineId = objectMapper.readTree(inboundLinesResponse).at("/data/0/lineId").asText();

        mockMvc.perform(post("/wms/api/in/v1/inbound-orders/" + inboundOrderId + "/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receipts":[
                                    {
                                      "lineId":"%s",
                                      "receivedQuantity":8
                                    }
                                  ]
                                }
                                """.formatted(inboundLineId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(post("/wms/api/in/v1/quality-checks")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "inboundOrderId":"%s",
                                  "sellerSku":"RPR-SKU-001",
                                  "sampleQuantity":8,
                                  "passQuantity":5,
                                  "failQuantity":3,
                                  "inspector":"qc-rpr-001",
                                  "remark":"three units need repair"
                                }
                                """.formatted(warehouseId, inboundOrderId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").value("PARTIAL_PASS"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerSku").value("RPR-SKU-001"))
                .andExpect(jsonPath("$.data[0].onHand").value(8))
                .andExpect(jsonPath("$.data[0].frozen").value(3))
                .andExpect(jsonPath("$.data[0].available").value(5));

        String repairResponse = mockMvc.perform(post("/wms/api/in/v1/product-repairs")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "warehouseId":"%s",
                                  "sellerSku":"RPR-SKU-001",
                                  "quantity":3,
                                  "supplierId":"SUP-RPR-001",
                                  "reason":"surface damage",
                                  "remark":"send defective stock for repair"
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_REPAIR"))
                .andExpect(jsonPath("$.data.outboundQuantity").value(3))
                .andReturn().getResponse().getContentAsString();
        String repairId = objectMapper.readTree(repairResponse).at("/data/repairId").asText();

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerSku").value("RPR-SKU-001"))
                .andExpect(jsonPath("$.data[0].onHand").value(5))
                .andExpect(jsonPath("$.data[0].frozen").value(0))
                .andExpect(jsonPath("$.data[0].available").value(5));

        mockMvc.perform(patch("/wms/api/in/v1/product-repairs/" + repairId + "/complete")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "qcResult":"PASS",
                                  "inboundQuantity":2,
                                  "processedBy":"repair-qc-001",
                                  "remark":"one unit scrapped during repair"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.qcResult").value("PASS"))
                .andExpect(jsonPath("$.data.inboundQuantity").value(2))
                .andExpect(jsonPath("$.data.processedBy").value("repair-qc-001"));

        mockMvc.perform(get("/wms/api/in/v1/product-repairs/" + repairId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.repairId").value(repairId))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.outboundQuantity").value(3))
                .andExpect(jsonPath("$.data.inboundQuantity").value(2));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerSku").value("RPR-SKU-001"))
                .andExpect(jsonPath("$.data[0].onHand").value(7))
                .andExpect(jsonPath("$.data[0].frozen").value(0))
                .andExpect(jsonPath("$.data[0].available").value(7));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory-transactions")
                        .header("X-Tenant-Id", tenantId)
                        .param("sellerSku", "RPR-SKU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].referenceType", Matchers.hasItems(
                        "INBOUND_RECEIVE", "QUALITY_CHECK_RELEASE", "PRODUCT_REPAIR_OUTBOUND", "PRODUCT_REPAIR_COMPLETE")));
    }

    /**
     * PG 集成测试会复用同一 Spring 上下文和数据库实例。
     * 为避免不同用例之间租户数据串扰，每个场景使用独立租户编码。
     */
    private String uniqueTenant(String scenario) {
        return scenario + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

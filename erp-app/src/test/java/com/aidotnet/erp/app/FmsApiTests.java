package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@ErpAppTest
@AutoConfigureMockMvc
class FmsApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageReceivableConfirmPaymentAndTenantIsolation() throws Exception {
        String body = "{\"sourceType\":\"ORDER\",\"sourceId\":\"order-100\",\"customerName\":\"Alice\",\"currency\":\"USD\",\"amount\":100.00}";
        String response = mockMvc.perform(post("/fms/api/in/v1/receivables")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        String receivableId = objectMapper.readTree(response).at("/data/receivableId").asText();

        mockMvc.perform(post("/fms/api/in/v1/receivables")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RECEIVABLE_DUPLICATED"));

        mockMvc.perform(post("/fms/api/in/v1/receivables/" + receivableId + "/payments")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":40.00,\"paymentMethod\":\"PAYPAL\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RECEIVABLE_STATUS_INVALID"));

        mockMvc.perform(patch("/fms/api/in/v1/receivables/" + receivableId + "/confirm")
                        .header("X-Tenant-Id", "tenant-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        mockMvc.perform(post("/fms/api/in/v1/receivables/" + receivableId + "/payments")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":40.00,\"paymentMethod\":\"PAYPAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_PAID"))
                .andExpect(jsonPath("$.data.paidAmount").value(40.0));

        mockMvc.perform(post("/fms/api/in/v1/receivables/" + receivableId + "/payments")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":70.00,\"paymentMethod\":\"PAYPAL\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_EXCEEDS_RECEIVABLE"));

        mockMvc.perform(post("/fms/api/in/v1/receivables/" + receivableId + "/payments")
                        .header("X-Tenant-Id", "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":60.00,\"paymentMethod\":\"PAYPAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));

        mockMvc.perform(get("/fms/api/in/v1/receivables")
                        .header("X-Tenant-Id", "tenant-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void recordCostEventsAndCalculateProfit() throws Exception {
        String productCostResponse = mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", "tenant-finance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"PRODUCT_COST",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-100",
                                  "sellerSku":"SKU-100",
                                  "marketplaceId":"AMAZON",
                                  "currency":"USD",
                                  "amount":30.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.costType").value("PRODUCT_COST"))
                .andReturn().getResponse().getContentAsString();
        String costEventId = objectMapper.readTree(productCostResponse).at("/data/costEventId").asText();

        mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", "tenant-finance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"SHIPPING_COST",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-100",
                                  "sellerSku":"SKU-100",
                                  "marketplaceId":"AMAZON",
                                  "currency":"USD",
                                  "amount":10.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.costType").value("SHIPPING_COST"));

        mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", "tenant-finance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"PRODUCT_COST",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-200",
                                  "sellerSku":"SKU-100",
                                  "marketplaceId":"AMAZON",
                                  "currency":"USD",
                                  "amount":8.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.costType").value("PRODUCT_COST"));

        mockMvc.perform(get("/fms/api/in/v1/cost-events/" + costEventId)
                        .header("X-Tenant-Id", "tenant-finance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.costEventId").value(costEventId))
                .andExpect(jsonPath("$.data.sourceId").value("ORD-100"));

        mockMvc.perform(get("/fms/api/in/v1/cost-events/by-source")
                        .header("X-Tenant-Id", "tenant-finance")
                        .param("sourceType", "OMS_ORDER")
                        .param("sourceId", "ORD-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(get("/fms/api/in/v1/cost-events/by-source")
                        .header("X-Tenant-Id", "tenant-finance")
                        .param("sourceType", "OMS_ORDER")
                        .param("sourceId", "ORD-200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].amount").value(8.0));

        mockMvc.perform(post("/fms/api/in/v1/profit-statements")
                        .header("X-Tenant-Id", "tenant-finance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sellerSku":"SKU-100",
                                  "marketplaceId":"AMAZON",
                                  "orderId":"ORD-100",
                                  "revenue":80.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCost").value(40.0))
                .andExpect(jsonPath("$.data.grossProfit").value(40.0))
                .andExpect(jsonPath("$.data.grossMargin").value(50.0));

        mockMvc.perform(post("/fms/api/in/v1/profit-statements")
                        .header("X-Tenant-Id", "tenant-finance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sellerSku":"SKU-100",
                                  "marketplaceId":"AMAZON",
                                  "orderId":"ORD-200",
                                  "revenue":20.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCost").value(8.0))
                .andExpect(jsonPath("$.data.grossProfit").value(12.0))
                .andExpect(jsonPath("$.data.grossMargin").value(60.0));

        mockMvc.perform(get("/fms/api/in/v1/profit-statements/by-sku")
                        .header("X-Tenant-Id", "tenant-finance")
                        .param("sellerSku", "SKU-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].orderId", Matchers.containsInAnyOrder("ORD-100", "ORD-200")));
    }

    @Test
    void importPlatformBillCreateCostEventAndReconcile() throws Exception {
        mockMvc.perform(post("/fms/api/in/v1/platform-bills/import")
                        .header("X-Tenant-Id", "tenant-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "billId":"bill-commission-100",
                                  "platform":"AMAZON",
                                  "store":"store-us",
                                  "billType":"COMMISSION",
                                  "period":"2026-04",
                                  "sellerSku":"SKU-200",
                                  "marketplaceId":"AMAZON-US",
                                  "currency":"USD",
                                  "amount":5.00,
                                  "rawData":{
                                    "externalBillNo":"EXT-100",
                                    "lineType":"commission"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.billId").value("bill-commission-100"))
                .andExpect(jsonPath("$.data.status").value("IMPORTED"));

        mockMvc.perform(get("/fms/api/in/v1/platform-bills/bill-commission-100")
                        .header("X-Tenant-Id", "tenant-bill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.platform").value("AMAZON"))
                .andExpect(jsonPath("$.data.billType").value("COMMISSION"));

        mockMvc.perform(get("/fms/api/in/v1/platform-bills")
                        .header("X-Tenant-Id", "tenant-bill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/fms/api/in/v1/cost-events/by-source")
                        .header("X-Tenant-Id", "tenant-bill")
                        .param("sourceType", "PLATFORM_BILL")
                        .param("sourceId", "bill-commission-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].costType").value("COMMISSION"))
                .andExpect(jsonPath("$.data[0].sellerSku").value("SKU-200"));

        mockMvc.perform(post("/fms/api/in/v1/platform-bills/bill-commission-100/reconcile")
                        .header("X-Tenant-Id", "tenant-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "settlementId":"settlement-100"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PLATFORM_SETTLEMENT_NOT_FOUND"));

        mockMvc.perform(post("/fms/api/in/v1/platform-settlements")
                        .header("X-Tenant-Id", "tenant-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "settlementId":"settlement-100",
                                  "platform":"AMAZON",
                                  "store":"store-us",
                                  "settlementType":"REGULAR",
                                  "amount":5.00,
                                  "currency":"USD",
                                  "settlementDate":"2026-04-20"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.settlementId").value("settlement-100"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.withdrawalStatus").value("UNREQUESTED"))
                .andExpect(jsonPath("$.data.forexStatus").value("UNSETTLED"));

        mockMvc.perform(post("/fms/api/in/v1/platform-bills/bill-commission-100/reconcile")
                        .header("X-Tenant-Id", "tenant-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "settlementId":"settlement-100"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECONCILED"))
                .andExpect(jsonPath("$.data.settlementId").value("settlement-100"));

        mockMvc.perform(get("/fms/api/in/v1/platform-settlements")
                        .header("X-Tenant-Id", "tenant-bill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/fms/api/in/v1/platform-settlements/settlement-100")
                        .header("X-Tenant-Id", "tenant-bill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECONCILED"))
                .andExpect(jsonPath("$.data.reconciledAmount").value(5.0))
                .andExpect(jsonPath("$.data.linkedBillCount").value(1));

        mockMvc.perform(get("/fms/api/in/v1/platform-settlements/settlement-100/bills")
                        .header("X-Tenant-Id", "tenant-bill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].billId").value("bill-commission-100"));

        mockMvc.perform(get("/fms/api/in/v1/receivables/by-source")
                        .header("X-Tenant-Id", "tenant-bill")
                        .param("sourceType", "PLATFORM_SETTLEMENT")
                        .param("sourceId", "settlement-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.amount").value(5.0));

        mockMvc.perform(post("/fms/api/in/v1/platform-settlements/settlement-100/receive")
                        .header("X-Tenant-Id", "tenant-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount":5.00,
                                  "paymentMethod":"BANK_TRANSFER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECEIVED"))
                .andExpect(jsonPath("$.data.receivedAmount").value(5.0));

        mockMvc.perform(get("/fms/api/in/v1/platform-bills/bill-commission-100")
                        .header("X-Tenant-Id", "tenant-bill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SETTLED"));

        mockMvc.perform(get("/fms/api/in/v1/receivables/by-source")
                        .header("X-Tenant-Id", "tenant-bill")
                        .param("sourceType", "PLATFORM_SETTLEMENT")
                        .param("sourceId", "settlement-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.paidAmount").value(5.0));

        mockMvc.perform(post("/fms/api/in/v1/platform-settlements/settlement-100/withdraw")
                        .header("X-Tenant-Id", "tenant-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "withdrawalStatus":"REQUESTED",
                                  "withdrawalReference":"wd-100"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.withdrawalStatus").value("REQUESTED"));

        mockMvc.perform(post("/fms/api/in/v1/platform-settlements/settlement-100/forex")
                        .header("X-Tenant-Id", "tenant-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "forexStatus":"PROCESSING"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.forexStatus").value("PROCESSING"));

        mockMvc.perform(post("/fms/api/in/v1/platform-settlements/settlement-100/withdraw")
                        .header("X-Tenant-Id", "tenant-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "withdrawalStatus":"COMPLETED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.withdrawalStatus").value("COMPLETED"));

        mockMvc.perform(post("/fms/api/in/v1/platform-settlements/settlement-100/forex")
                        .header("X-Tenant-Id", "tenant-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "forexStatus":"SETTLED",
                                  "forexRate":7.25
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.forexStatus").value("SETTLED"))
                .andExpect(jsonPath("$.data.forexRate").value(7.25));
    }

    @Test
    void createAggregationRuleAggregateOrderCostAndPersistProfitResult() throws Exception {
        String productCostResponse = mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", "tenant-engine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"PRODUCT_COST",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-ENGINE-100",
                                  "sellerSku":"SKU-ENGINE-100",
                                  "marketplaceId":"AMAZON",
                                  "currency":"USD",
                                  "amount":30.00
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String productCostEventId = objectMapper.readTree(productCostResponse).at("/data/costEventId").asText();

        String shippingCostResponse = mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", "tenant-engine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"SHIPPING_COST",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-ENGINE-100",
                                  "sellerSku":"SKU-ENGINE-100",
                                  "marketplaceId":"AMAZON",
                                  "currency":"USD",
                                  "amount":10.00
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String shippingCostEventId = objectMapper.readTree(shippingCostResponse).at("/data/costEventId").asText();

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", "tenant-engine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"product-cost-order-rule",
                                  "costSource":"PRODUCT_COST",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"ORDER",
                                  "priority":100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ruleName").value("product-cost-order-rule"))
                .andExpect(jsonPath("$.data.costSource").value("PRODUCT_COST"))
                .andExpect(jsonPath("$.data.targetDimension").value("ORDER"));

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", "tenant-engine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"shipping-cost-order-rule",
                                  "costSource":"SHIPPING_COST",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"ORDER",
                                  "priority":90
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ruleName").value("shipping-cost-order-rule"))
                .andExpect(jsonPath("$.data.costSource").value("SHIPPING_COST"));

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-events/" + productCostEventId + "/aggregate")
                        .header("X-Tenant-Id", "tenant-engine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].targetDimension").value("ORDER"))
                .andExpect(jsonPath("$.data[0].targetId").value("ORD-ENGINE-100"))
                .andExpect(jsonPath("$.data[0].amountInBaseCurrency").value(30.0));

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-events/" + shippingCostEventId + "/aggregate")
                        .header("X-Tenant-Id", "tenant-engine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].targetId").value("ORD-ENGINE-100"))
                .andExpect(jsonPath("$.data[0].amountInBaseCurrency").value(10.0));

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-results")
                        .header("X-Tenant-Id", "tenant-engine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dimensionType":"ORDER",
                                  "dimensionId":"ORD-ENGINE-100",
                                  "sellerSku":"SKU-ENGINE-100",
                                  "orderId":"ORD-ENGINE-100",
                                  "marketplaceId":"AMAZON",
                                  "revenue":100.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dimensionType").value("ORDER"))
                .andExpect(jsonPath("$.data.dimensionId").value("ORD-ENGINE-100"))
                .andExpect(jsonPath("$.data.productCost").value(30.0))
                .andExpect(jsonPath("$.data.shippingCost").value(10.0))
                .andExpect(jsonPath("$.data.totalCost").value(40.0))
                .andExpect(jsonPath("$.data.grossProfit").value(60.0))
                .andExpect(jsonPath("$.data.grossMargin").value(0.6));

        mockMvc.perform(get("/fms/api/in/v1/engine/profit-results")
                        .header("X-Tenant-Id", "tenant-engine")
                        .param("dimensionType", "ORDER")
                        .param("dimensionId", "ORD-ENGINE-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].orderId").value("ORD-ENGINE-100"));

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-deviation-alerts/detect")
                        .header("X-Tenant-Id", "tenant-engine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "threshold":0.70
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].dimensionId").value("ORD-ENGINE-100"))
                .andExpect(jsonPath("$.data[0].status").value("OPEN"));

        mockMvc.perform(get("/fms/api/in/v1/engine/profit-deviation-alerts")
                        .header("X-Tenant-Id", "tenant-engine")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].actualMargin").value(0.6));
    }

    @Test
    void aggregateCostByStoreAndChannelAndManageDeviationLifecycle() throws Exception {
        String tenantId = "tenant-fms-dimension-100";

        String productCostResponse = mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"PRODUCT_COST",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-DIM-100",
                                  "sellerSku":"SKU-DIM-100",
                                  "storeId":"store-us-100",
                                  "channelCode":"AMAZON",
                                  "marketplaceId":"AMAZON-US",
                                  "currency":"USD",
                                  "amount":40.00
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String productCostEventId = objectMapper.readTree(productCostResponse).at("/data/costEventId").asText();

        String commissionCostResponse = mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"COMMISSION",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-DIM-100",
                                  "sellerSku":"SKU-DIM-100",
                                  "storeId":"store-us-100",
                                  "channelCode":"AMAZON",
                                  "marketplaceId":"AMAZON-US",
                                  "currency":"USD",
                                  "amount":10.00
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String commissionCostEventId = objectMapper.readTree(commissionCostResponse).at("/data/costEventId").asText();

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"store-product-rule",
                                  "costSource":"PRODUCT_COST",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"STORE",
                                  "priority":100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetDimension").value("STORE"));

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"channel-product-rule",
                                  "costSource":"PRODUCT_COST",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"CHANNEL",
                                  "priority":90
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetDimension").value("CHANNEL"));

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"store-commission-rule",
                                  "costSource":"COMMISSION",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"STORE",
                                  "priority":100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetDimension").value("STORE"));

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"channel-commission-rule",
                                  "costSource":"COMMISSION",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"CHANNEL",
                                  "priority":90
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetDimension").value("CHANNEL"));

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-events/" + productCostEventId + "/aggregate")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].targetDimension").value("STORE"))
                .andExpect(jsonPath("$.data[0].targetId").value("store-us-100"))
                .andExpect(jsonPath("$.data[1].targetDimension").value("CHANNEL"))
                .andExpect(jsonPath("$.data[1].targetId").value("AMAZON"));

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-events/" + commissionCostEventId + "/aggregate")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].targetDimension").value("STORE"))
                .andExpect(jsonPath("$.data[0].targetId").value("store-us-100"))
                .andExpect(jsonPath("$.data[1].targetDimension").value("CHANNEL"))
                .andExpect(jsonPath("$.data[1].targetId").value("AMAZON"));

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-results")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dimensionType":"STORE",
                                  "dimensionId":"store-us-100",
                                  "sellerSku":"SKU-DIM-100",
                                  "orderId":"ORD-DIM-100",
                                  "storeId":"store-us-100",
                                  "marketplaceId":"AMAZON-US",
                                  "revenue":100.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dimensionType").value("STORE"))
                .andExpect(jsonPath("$.data.dimensionId").value("store-us-100"))
                .andExpect(jsonPath("$.data.productCost").value(40.0))
                .andExpect(jsonPath("$.data.commission").value(10.0))
                .andExpect(jsonPath("$.data.totalCost").value(50.0))
                .andExpect(jsonPath("$.data.grossProfit").value(50.0))
                .andExpect(jsonPath("$.data.grossMargin").value(0.5));

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-results")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dimensionType":"CHANNEL",
                                  "dimensionId":"AMAZON",
                                  "sellerSku":"SKU-DIM-100",
                                  "orderId":"ORD-DIM-100",
                                  "storeId":"store-us-100",
                                  "marketplaceId":"AMAZON-US",
                                  "revenue":100.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dimensionType").value("CHANNEL"))
                .andExpect(jsonPath("$.data.dimensionId").value("AMAZON"))
                .andExpect(jsonPath("$.data.totalCost").value(50.0))
                .andExpect(jsonPath("$.data.grossMargin").value(0.5));

        mockMvc.perform(get("/fms/api/in/v1/engine/profit-results/by-dimension")
                        .header("X-Tenant-Id", tenantId)
                        .param("dimensionType", "STORE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].dimensionId").value("store-us-100"));

        mockMvc.perform(get("/fms/api/in/v1/engine/profit-summary")
                        .header("X-Tenant-Id", tenantId)
                        .param("dimensionType", "STORE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dimensionType").value("STORE"))
                .andExpect(jsonPath("$.data.totalRevenue").value(100.0))
                .andExpect(jsonPath("$.data.totalProfit").value(50.0))
                .andExpect(jsonPath("$.data.avgMargin").value(0.5))
                .andExpect(jsonPath("$.data.resultCount").value(1));

        String detectResponse = mockMvc.perform(post("/fms/api/in/v1/engine/profit-deviation-alerts/detect")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "threshold":0.60
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].status").value("OPEN"))
                .andReturn().getResponse().getContentAsString();
        String alertId = objectMapper.readTree(detectResponse).at("/data/0/alertId").asText();

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-deviation-alerts/" + alertId + "/acknowledge")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alertId").value(alertId))
                .andExpect(jsonPath("$.data.status").value("ACKNOWLEDGED"));

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-deviation-alerts/" + alertId + "/resolve")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alertId").value(alertId))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        mockMvc.perform(get("/fms/api/in/v1/engine/profit-deviation-alerts")
                        .header("X-Tenant-Id", tenantId)
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].alertId").value(alertId));
    }

    @Test
    void exposeUnifiedV1ProfitAnalysisAndForexRiskAlerts() throws Exception {
        String tenantId = "tenant-fms-profit-read-" + java.util.UUID.randomUUID();
        String otherTenantId = "tenant-fms-profit-read-other-" + java.util.UUID.randomUUID();

        mockMvc.perform(post("/fms/api/in/v1/forex-rates")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromCurrency":"USD",
                                  "toCurrency":"CNY",
                                  "rate":7.10,
                                  "effectiveDate":"2026-05-01",
                                  "source":"MANUAL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rate").value(7.10));

        mockMvc.perform(post("/fms/api/in/v1/forex-rates")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromCurrency":"USD",
                                  "toCurrency":"CNY",
                                  "rate":7.50,
                                  "effectiveDate":"2026-05-02",
                                  "source":"MANUAL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rate").value(7.50));

        String productCostResponse = mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"PRODUCT_COST",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-READ-200",
                                  "sellerSku":"SKU-READ-200",
                                  "storeId":"store-us-200",
                                  "channelCode":"AMAZON",
                                  "marketplaceId":"AMAZON-US",
                                  "currency":"USD",
                                  "amount":55.00
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String productCostEventId = objectMapper.readTree(productCostResponse).at("/data/costEventId").asText();

        String commissionCostResponse = mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"COMMISSION",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-READ-200",
                                  "sellerSku":"SKU-READ-200",
                                  "storeId":"store-us-200",
                                  "channelCode":"AMAZON",
                                  "marketplaceId":"AMAZON-US",
                                  "currency":"USD",
                                  "amount":15.00
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String commissionCostEventId = objectMapper.readTree(commissionCostResponse).at("/data/costEventId").asText();

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"profit-read-store-product-rule",
                                  "costSource":"PRODUCT_COST",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"STORE",
                                  "priority":100
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"profit-read-channel-product-rule",
                                  "costSource":"PRODUCT_COST",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"CHANNEL",
                                  "priority":90
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"profit-read-market-product-rule",
                                  "costSource":"PRODUCT_COST",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"MARKETPLACE",
                                  "priority":80
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"profit-read-store-commission-rule",
                                  "costSource":"COMMISSION",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"STORE",
                                  "priority":100
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"profit-read-channel-commission-rule",
                                  "costSource":"COMMISSION",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"CHANNEL",
                                  "priority":90
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"profit-read-market-commission-rule",
                                  "costSource":"COMMISSION",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"MARKETPLACE",
                                  "priority":80
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-events/" + productCostEventId + "/aggregate")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-events/" + commissionCostEventId + "/aggregate")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-results")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dimensionType":"STORE",
                                  "dimensionId":"store-us-200",
                                  "sellerSku":"SKU-READ-200",
                                  "orderId":"ORD-READ-200",
                                  "storeId":"store-us-200",
                                  "marketplaceId":"AMAZON-US",
                                  "revenue":100.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-results")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dimensionType":"CHANNEL",
                                  "dimensionId":"AMAZON",
                                  "sellerSku":"SKU-READ-200",
                                  "orderId":"ORD-READ-200",
                                  "storeId":"store-us-200",
                                  "marketplaceId":"AMAZON-US",
                                  "revenue":100.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-results")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dimensionType":"MARKETPLACE",
                                  "dimensionId":"AMAZON-US",
                                  "sellerSku":"SKU-READ-200",
                                  "orderId":"ORD-READ-200",
                                  "storeId":"store-us-200",
                                  "marketplaceId":"AMAZON-US",
                                  "revenue":100.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-deviation-alerts/detect")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "threshold":0.35
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));

        mockMvc.perform(get("/fms/api/v1/profit/by-store")
                        .header("X-Tenant-Id", tenantId)
                        .param("storeId", "store-us-200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.totalRevenue").value(100.0))
                .andExpect(jsonPath("$.data.totalGrossProfit").value(30.0))
                .andExpect(jsonPath("$.data.alertCount").value(1))
                .andExpect(jsonPath("$.data.openAlertCount").value(1))
                .andExpect(jsonPath("$.data.results[0].dimensionType").value("STORE"))
                .andExpect(jsonPath("$.data.results[0].dimensionId").value("store-us-200"))
                .andExpect(jsonPath("$.data.results[0].latestAlert.status").value("OPEN"));

        mockMvc.perform(get("/fms/api/v1/profit/by-channel")
                        .header("X-Tenant-Id", tenantId)
                        .param("channelCode", "AMAZON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.totalRevenue").value(100.0))
                .andExpect(jsonPath("$.data.results[0].dimensionType").value("CHANNEL"))
                .andExpect(jsonPath("$.data.results[0].dimensionId").value("AMAZON"));

        mockMvc.perform(get("/fms/api/v1/profit/by-market")
                        .header("X-Tenant-Id", tenantId)
                        .param("marketplaceId", "AMAZON-US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.totalRevenue").value(100.0))
                .andExpect(jsonPath("$.data.results[0].dimensionType").value("MARKETPLACE"))
                .andExpect(jsonPath("$.data.results[0].dimensionId").value("AMAZON-US"));

        mockMvc.perform(get("/fms/api/v1/forex/risk-alert")
                        .header("X-Tenant-Id", tenantId)
                        .param("fromCurrency", "USD")
                        .param("toCurrency", "CNY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.latestRate").value(7.5))
                .andExpect(jsonPath("$.data.previousRate").value(7.1))
                .andExpect(jsonPath("$.data.latestEffectiveDate").value("2026-05-02"))
                .andExpect(jsonPath("$.data.previousEffectiveDate").value("2026-05-01"))
                .andExpect(jsonPath("$.data.level").value("HIGH"));

        mockMvc.perform(get("/fms/api/v1/profit/by-store")
                        .header("X-Tenant-Id", otherTenantId)
                        .param("storeId", "store-us-200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(0))
                .andExpect(jsonPath("$.data.results.length()").value(0));
    }

    @Test
    void managePaymentRequestWriteOffAndReconciliation() throws Exception {
        mockMvc.perform(post("/fms/api/in/v1/payment-requests")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId":"pay-100",
                                  "poId":"PO-100",
                                  "supplierId":"SUP-1",
                                  "amount":120.00,
                                  "currency":"USD",
                                  "requestType":"PROCUREMENT",
                                  "requestedBy":"buyer-1",
                                  "approvalFlow":[
                                    {"node":"manager"},
                                    {"node":"finance-director"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").value("pay-100"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.writeoffStatus").value("UNWRITTEN"));

        mockMvc.perform(get("/fms/api/in/v1/payment-requests/pay-100")
                        .header("X-Tenant-Id", "tenant-fms-p2-042"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.supplierId").value("SUP-1"));

        mockMvc.perform(post("/fms/api/in/v1/payment-requests/pay-100/pay")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paidBy":"cashier-1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_REQUEST_STATUS_INVALID"));

        mockMvc.perform(post("/fms/api/in/v1/payment-requests/pay-100/approve")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId":"director-1",
                                  "approvalLevel":2,
                                  "comment":"skip-level"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_REQUEST_APPROVAL_LEVEL_INVALID"));

        mockMvc.perform(post("/fms/api/in/v1/payment-requests/pay-100/approve")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId":"manager-1",
                                  "approvalLevel":1,
                                  "comment":"manager-approved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(post("/fms/api/in/v1/payment-requests/pay-100/approve")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId":"director-1",
                                  "approvalLevel":2,
                                  "comment":"director-approved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(get("/fms/api/in/v1/payment-requests/pay-100/approvals")
                        .header("X-Tenant-Id", "tenant-fms-p2-042"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].approvalLevel").value(1))
                .andExpect(jsonPath("$.data[1].approvalLevel").value(2));

        mockMvc.perform(post("/fms/api/in/v1/write-offs")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "writeoffId":"wo-prepaid-100",
                                  "type":"INBOUND",
                                  "refType":"PAYMENT_REQUEST",
                                  "refId":"pay-100",
                                  "amount":10.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WRITE_OFF_REF_STATUS_INVALID"));

        mockMvc.perform(post("/fms/api/in/v1/payment-requests/pay-100/pay")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paidBy":"cashier-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.paidBy").value("cashier-1"));

        mockMvc.perform(post("/fms/api/in/v1/write-offs")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "writeoffId":"wo-100",
                                  "type":"INBOUND",
                                  "refType":"PAYMENT_REQUEST",
                                  "refId":"pay-100",
                                  "amount":40.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(post("/fms/api/in/v1/write-offs/wo-100/approve")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approvedBy":"finance-manager"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.approvedBy").value("finance-manager"));

        mockMvc.perform(get("/fms/api/in/v1/payment-requests/pay-100")
                        .header("X-Tenant-Id", "tenant-fms-p2-042"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.writeoffStatus").value("PARTIAL"))
                .andExpect(jsonPath("$.data.writeoffAmount").value(40.0));

        mockMvc.perform(get("/fms/api/in/v1/write-offs/by-ref")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .param("refType", "PAYMENT_REQUEST")
                        .param("refId", "pay-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(post("/fms/api/in/v1/reconciliations")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reconId":"recon-invalid-100",
                                  "type":"SUPPLIER",
                                  "partyId":"SUP-1",
                                  "partyName":"Shenzhen Supplier",
                                  "period":"2026-04",
                                  "payableAmount":100.00,
                                  "paidAmount":120.00,
                                  "items":[
                                    {"refNo":"PO-100","amount":100.00}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RECONCILIATION_AMOUNT_INVALID"));

        mockMvc.perform(post("/fms/api/in/v1/reconciliations")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reconId":"recon-supplier-100",
                                  "type":"SUPPLIER",
                                  "partyId":"SUP-1",
                                  "partyName":"Shenzhen Supplier",
                                  "period":"2026-04",
                                  "payableAmount":100.00,
                                  "paidAmount":20.00,
                                  "items":[
                                    {"refNo":"PO-100","amount":100.00}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.balance").value(80.0));

        mockMvc.perform(post("/fms/api/in/v1/reconciliations/recon-supplier-100/dispute")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "differenceItems":[
                                    {"refNo":"PO-100","expected":100.00,"actual":98.00,"reason":"price_diff"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISPUTED"));

        mockMvc.perform(post("/fms/api/in/v1/reconciliations/recon-supplier-100/confirm")
                        .header("X-Tenant-Id", "tenant-fms-p2-042"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECONCILED"));

        mockMvc.perform(get("/fms/api/in/v1/reconciliations/supplier/SUP-1")
                        .header("X-Tenant-Id", "tenant-fms-p2-042"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].reconId").value("recon-supplier-100"));

        mockMvc.perform(post("/fms/api/in/v1/reconciliations")
                        .header("X-Tenant-Id", "tenant-fms-p2-042")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reconId":"recon-logistics-100",
                                  "type":"LOGISTICS",
                                  "partyId":"LSP-1",
                                  "partyName":"Fast Line",
                                  "period":"2026-04",
                                  "payableAmount":55.00,
                                  "items":[
                                    {"refNo":"SHIP-100","amount":55.00}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("LOGISTICS"));

        mockMvc.perform(get("/fms/api/in/v1/reconciliations/logistics/LSP-1")
                        .header("X-Tenant-Id", "tenant-fms-p2-042"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].partyName").value("Fast Line"));
    }

    @Test
    void manageInventoryVoucherTemplateGenerateApproveAndPushVoucher() throws Exception {
        String templateResponse = mockMvc.perform(post("/fms/api/in/v1/voucher-templates")
                        .header("X-Tenant-Id", "tenant-fms-voucher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateName":"采购入库凭证模板",
                                  "businessType":"PURCHASE_INBOUND",
                                  "debitAccount":"1405",
                                  "creditAccount":"2202",
                                  "description":"采购入库自动凭证"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateName").value("采购入库凭证模板"))
                .andExpect(jsonPath("$.data.businessType").value("PURCHASE_INBOUND"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andReturn().getResponse().getContentAsString();
        String templateId = objectMapper.readTree(templateResponse).at("/data/templateId").asText();

        mockMvc.perform(get("/fms/api/in/v1/voucher-templates")
                        .header("X-Tenant-Id", "tenant-fms-voucher")
                        .param("businessType", "PURCHASE_INBOUND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].templateId").value(templateId));

        String voucherResponse = mockMvc.perform(post("/fms/api/in/v1/inventory-vouchers/auto-generate")
                        .header("X-Tenant-Id", "tenant-fms-voucher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessType":"PURCHASE_INBOUND",
                                  "voucherType":"INVENTORY",
                                  "sourceId":"PO-IN-100",
                                  "amount":128.50,
                                  "currency":"USD",
                                  "sellerSku":"SKU-V-100"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.referenceType").value("PURCHASE_INBOUND"))
                .andExpect(jsonPath("$.data.referenceId").value("PO-IN-100"))
                .andExpect(jsonPath("$.data.voucherNumber").isNotEmpty())
                .andExpect(jsonPath("$.data.lines.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        String voucherId = objectMapper.readTree(voucherResponse).at("/data/voucherId").asText();
        String voucherNumber = objectMapper.readTree(voucherResponse).at("/data/voucherNumber").asText();

        mockMvc.perform(get("/fms/api/in/v1/inventory-vouchers")
                        .header("X-Tenant-Id", "tenant-fms-voucher")
                        .param("voucherType", "INVENTORY")
                        .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].voucherId").value(voucherId));

        mockMvc.perform(post("/fms/api/in/v1/vouchers/" + voucherId + "/approve")
                        .header("X-Tenant-Id", "tenant-fms-voucher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approvedBy":"finance.manager"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("POSTED"))
                .andExpect(jsonPath("$.data.postedBy").value("finance.manager"));

        mockMvc.perform(get("/fms/api/out/v1/vouchers")
                        .header("X-Tenant-Id", "tenant-fms-voucher")
                        .param("voucherType", "INVENTORY")
                        .param("status", "POSTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].voucherId").value(voucherId))
                .andExpect(jsonPath("$.data[0].voucherNumber").value(voucherNumber));

        mockMvc.perform(get("/fms/api/out/v1/vouchers/" + voucherId)
                        .header("X-Tenant-Id", "tenant-fms-voucher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.referenceType").value("PURCHASE_INBOUND"))
                .andExpect(jsonPath("$.data.referenceId").value("PO-IN-100"));

        mockMvc.perform(get("/fms/api/out/v1/vouchers/by-reference")
                        .header("X-Tenant-Id", "tenant-fms-voucher")
                        .param("referenceType", "PURCHASE_INBOUND")
                        .param("referenceId", "PO-IN-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].voucherId").value(voucherId));

        mockMvc.perform(get("/fms/api/out/v1/vouchers/summary")
                        .header("X-Tenant-Id", "tenant-fms-voucher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1))
                .andExpect(jsonPath("$.data.totalDebit").value(128.5))
                .andExpect(jsonPath("$.data.totalCredit").value(128.5))
                .andExpect(jsonPath("$.data.byType.PURCHASE_INBOUND").value(128.5));

        mockMvc.perform(post("/fms/api/out/v1/vouchers/" + voucherId + "/push-kingdee")
                        .header("X-Tenant-Id", "tenant-fms-voucher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(get("/fms/api/out/v1/vouchers/" + voucherId)
                        .header("X-Tenant-Id", "tenant-fms-voucher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EXPORTED"))
                .andExpect(jsonPath("$.data.exportBatchId", Matchers.startsWith("KD-")));

        mockMvc.perform(get("/fms/api/out/v1/vouchers/export")
                        .header("X-Tenant-Id", "tenant-fms-voucher")
                        .param("voucherType", "INVENTORY")
                        .param("status", "EXPORTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].voucherId").value(voucherId))
                .andExpect(jsonPath("$.data[0].referenceType").value("PURCHASE_INBOUND"))
                .andExpect(jsonPath("$.data[0].referenceId").value("PO-IN-100"));
    }

    @Test
    void executeOutboundPaymentIdempotentlyAndQueryRealStatus() throws Exception {
        String tenantId = "tenant-fms-outbound-pay";

        mockMvc.perform(post("/fms/api/in/v1/payment-requests")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId":"pay-out-100",
                                  "poId":"PO-OUT-100",
                                  "supplierId":"SUP-OUT-1",
                                  "amount":180.00,
                                  "currency":"USD",
                                  "requestType":"PROCUREMENT",
                                  "requestedBy":"buyer-out-1",
                                  "approvalFlow":[
                                    {"node":"manager"},
                                    {"node":"finance-director"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(post("/fms/api/in/v1/payment-requests/pay-out-100/approve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId":"manager-out-1",
                                  "approvalLevel":1,
                                  "comment":"manager-approved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(post("/fms/api/in/v1/payment-requests/pay-out-100/approve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approverId":"director-out-1",
                                  "approvalLevel":2,
                                  "comment":"director-approved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        String paidAt = "2026-05-07T10:15:30Z";
        mockMvc.perform(post("/fms/api/out/v1/payments/execute")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId":"pay-out-100",
                                  "paidBy":"cashier-out-1",
                                  "paidAt":"%s"
                                }
                                """.formatted(paidAt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentId").value("pay-out-100"))
                .andExpect(jsonPath("$.data.requestId").value("pay-out-100"))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.requestType").value("PROCUREMENT"))
                .andExpect(jsonPath("$.data.amount").value(180.0))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.paidBy").value("cashier-out-1"))
                .andExpect(jsonPath("$.data.paidAt").value(paidAt))
                .andExpect(jsonPath("$.data.writeoffStatus").value("UNWRITTEN"));

        mockMvc.perform(post("/fms/api/out/v1/payments/execute")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId":"pay-out-100",
                                  "paidBy":"cashier-out-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentId").value("pay-out-100"))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.paidBy").value("cashier-out-1"));

        mockMvc.perform(get("/fms/api/out/v1/payments/pay-out-100/status")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentId").value("pay-out-100"))
                .andExpect(jsonPath("$.data.requestId").value("pay-out-100"))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.requestType").value("PROCUREMENT"))
                .andExpect(jsonPath("$.data.amount").value(180.0))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.writeoffStatus").value("UNWRITTEN"));
    }

    @Test
    void exportSettlementReportFromRealPlatformSettlementData() throws Exception {
        String tenantId = "tenant-fms-outbound-settlement";

        mockMvc.perform(post("/fms/api/in/v1/platform-bills/import")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "billId":"bill-out-200",
                                  "platform":"AMAZON",
                                  "store":"US-STORE",
                                  "billType":"COMMISSION",
                                  "period":"2026-04",
                                  "sellerSku":"SKU-OUT-200",
                                  "marketplaceId":"ATVPDKIKX0DER",
                                  "sourceType":"SETTLEMENT",
                                  "sourceId":"ORDER-OUT-200",
                                  "currency":"USD",
                                  "amount":100.00,
                                  "rawData":{"source":"amazon-settlement"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IMPORTED"));

        mockMvc.perform(post("/fms/api/in/v1/platform-settlements")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "settlementId":"settlement-out-200",
                                  "platform":"AMAZON",
                                  "store":"US-STORE",
                                  "settlementType":"REGULAR",
                                  "amount":100.00,
                                  "currency":"USD",
                                  "settlementDate":"2026-04-20"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(post("/fms/api/in/v1/platform-bills/bill-out-200/reconcile")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "settlementId":"settlement-out-200"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECONCILED"));

        mockMvc.perform(post("/fms/api/in/v1/platform-settlements/settlement-out-200/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount":100.00,
                                  "paymentMethod":"BANK_TRANSFER",
                                  "receivedAt":"2026-04-21T08:00:00Z"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECEIVED"))
                .andExpect(jsonPath("$.data.receivedAmount").value(100.0));

        mockMvc.perform(post("/fms/api/out/v1/settlements/report")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "period":"2026-04",
                                  "platform":"AMAZON",
                                  "store":"US-STORE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("2026-04"))
                .andExpect(jsonPath("$.data.platform").value("AMAZON"))
                .andExpect(jsonPath("$.data.store").value("US-STORE"))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.totalAmount").value(100.0))
                .andExpect(jsonPath("$.data.totalReconciledAmount").value(100.0))
                .andExpect(jsonPath("$.data.totalReceivedAmount").value(100.0))
                .andExpect(jsonPath("$.data.totalLinkedBillCount").value(1))
                .andExpect(jsonPath("$.data.statuses.RECEIVED").value(1))
                .andExpect(jsonPath("$.data.settlements.length()").value(1))
                .andExpect(jsonPath("$.data.settlements[0].settlementId").value("settlement-out-200"))
                .andExpect(jsonPath("$.data.settlements[0].status").value("RECEIVED"))
                .andExpect(jsonPath("$.data.settlements[0].withdrawalStatus").value("UNREQUESTED"))
                .andExpect(jsonPath("$.data.settlements[0].forexStatus").value("UNSETTLED"));
    }

    @Test
    void exportProfitReportWithTraceabilityAndDeviationSummary() throws Exception {
        String tenantId = "tenant-fms-outbound-profit";

        String productCostResponse = mockMvc.perform(post("/fms/api/in/v1/cost-events")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costType":"PRODUCT_COST",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-PROFIT-300",
                                  "sellerSku":"SKU-PROFIT-300",
                                  "storeId":"store-profit-1",
                                  "channelCode":"AMAZON",
                                  "marketplaceId":"AMAZON-US",
                                  "currency":"USD",
                                  "amount":60.00
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String productCostEventId = objectMapper.readTree(productCostResponse).at("/data/costEventId").asText();

        mockMvc.perform(post("/fms/api/in/v1/platform-bills/import")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "billId":"bill-profit-300",
                                  "platform":"AMAZON",
                                  "store":"store-profit-1",
                                  "billType":"COMMISSION",
                                  "period":"2026-04",
                                  "sellerSku":"SKU-PROFIT-300",
                                  "marketplaceId":"AMAZON-US",
                                  "sourceType":"SETTLEMENT",
                                  "sourceId":"ORD-PROFIT-300",
                                  "currency":"USD",
                                  "amount":12.00,
                                  "rawData":{"source":"amazon-settlement","lineNo":"1"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.billId").value("bill-profit-300"))
                .andExpect(jsonPath("$.data.status").value("IMPORTED"));

        String commissionCostResponse = mockMvc.perform(get("/fms/api/in/v1/cost-events/by-source")
                        .header("X-Tenant-Id", tenantId)
                        .param("sourceType", "PLATFORM_BILL")
                        .param("sourceId", "bill-profit-300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].costType").value("COMMISSION"))
                .andReturn().getResponse().getContentAsString();
        String commissionCostEventId = objectMapper.readTree(commissionCostResponse).at("/data/0/costEventId").asText();

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"store-product-profit-rule",
                                  "costSource":"PRODUCT_COST",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"STORE",
                                  "priority":100
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleName":"store-commission-profit-rule",
                                  "costSource":"COMMISSION",
                                  "allocationMethod":"DIRECT",
                                  "targetDimension":"STORE",
                                  "priority":90
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-events/" + productCostEventId + "/aggregate")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].targetDimension").value("STORE"))
                .andExpect(jsonPath("$.data[0].targetId").value("store-profit-1"));

        mockMvc.perform(post("/fms/api/in/v1/engine/cost-events/" + commissionCostEventId + "/aggregate")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].targetDimension").value("STORE"))
                .andExpect(jsonPath("$.data[0].targetId").value("store-profit-1"));

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-results")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dimensionType":"STORE",
                                  "dimensionId":"store-profit-1",
                                  "sellerSku":"SKU-PROFIT-300",
                                  "orderId":"ORD-PROFIT-300",
                                  "storeId":"store-profit-1",
                                  "marketplaceId":"AMAZON-US",
                                  "revenue":100.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCost").value(72.0))
                .andExpect(jsonPath("$.data.grossProfit").value(28.0))
                .andExpect(jsonPath("$.data.grossMargin").value(0.28));

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-deviation-alerts/detect")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "threshold":0.35
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].dimensionId").value("store-profit-1"))
                .andExpect(jsonPath("$.data[0].status").value("OPEN"));

        mockMvc.perform(post("/fms/api/out/v1/profits/report")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dimensionType":"STORE",
                                  "dimensionId":"store-profit-1",
                                  "storeId":"store-profit-1",
                                  "currency":"USD",
                                  "alertStatus":"OPEN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dimensionType").value("STORE"))
                .andExpect(jsonPath("$.data.dimensionId").value("store-profit-1"))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.totalRevenue").value(100.0))
                .andExpect(jsonPath("$.data.totalCost").value(72.0))
                .andExpect(jsonPath("$.data.totalGrossProfit").value(28.0))
                .andExpect(jsonPath("$.data.avgGrossMargin").value(0.28))
                .andExpect(jsonPath("$.data.alertCount").value(1))
                .andExpect(jsonPath("$.data.openAlertCount").value(1))
                .andExpect(jsonPath("$.data.currencySummaries.length()").value(1))
                .andExpect(jsonPath("$.data.currencySummaries[0].currency").value("USD"))
                .andExpect(jsonPath("$.data.currencySummaries[0].totalRevenue").value(100.0))
                .andExpect(jsonPath("$.data.results.length()").value(1))
                .andExpect(jsonPath("$.data.results[0].dimensionType").value("STORE"))
                .andExpect(jsonPath("$.data.results[0].dimensionId").value("store-profit-1"))
                .andExpect(jsonPath("$.data.results[0].sellerSku").value("SKU-PROFIT-300"))
                .andExpect(jsonPath("$.data.results[0].costDetails.PRODUCT_COST").value(60.0))
                .andExpect(jsonPath("$.data.results[0].costDetails.COMMISSION").value(12.0))
                .andExpect(jsonPath("$.data.results[0].latestAlert.status").value("OPEN"))
                .andExpect(jsonPath("$.data.results[0].latestAlert.severity").value("LOW"))
                .andExpect(jsonPath("$.data.results[0].costTraces.length()").value(2))
                .andExpect(jsonPath("$.data.results[0].costTraces[*].sourceType",
                        Matchers.containsInAnyOrder("OMS_ORDER", "PLATFORM_BILL")))
                .andExpect(jsonPath("$.data.results[0].costTraces[*].sourceId",
                        Matchers.containsInAnyOrder("ORD-PROFIT-300", "bill-profit-300")));
    }

    @Test
    void managePaymentAggregationLifecycleWithPersistentPgState() throws Exception {
        String tenantId = "tenant-fms-payment";
        String periodStart = LocalDate.now(ZoneOffset.UTC).minusDays(1).toString();
        String periodEnd = LocalDate.now(ZoneOffset.UTC).plusDays(1).toString();

        mockMvc.perform(post("/fms/api/in/v1/payment/channels/register")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "channelCode":"PAYPAL",
                                  "channelName":"PayPal Cross Border",
                                  "channelType":"PAYMENT_GATEWAY",
                                  "config":{
                                    "merchantId":"M-100",
                                    "region":"US"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channelCode").value("PAYPAL"))
                .andExpect(jsonPath("$.data.channelName").value("PayPal Cross Border"))
                .andExpect(jsonPath("$.data.channelType").value("PAYMENT_GATEWAY"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.config.merchantId").value("M-100"));

        mockMvc.perform(get("/fms/api/in/v1/payment/channels")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].channelCode").value("PAYPAL"));

        mockMvc.perform(post("/fms/api/in/v1/payment/accounts/register")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "channelCode":"PAYPAL",
                                  "accountId":"acct-paypal-usd",
                                  "accountName":"PayPal USD Account",
                                  "currency":"USD",
                                  "credentials":{
                                    "clientId":"client-100",
                                    "signingKey":"sign-100"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value("acct-paypal-usd"))
                .andExpect(jsonPath("$.data.channelCode").value("PAYPAL"))
                .andExpect(jsonPath("$.data.balance").value(0.0))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(get("/fms/api/in/v1/payment/accounts/acct-paypal-usd/balance")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value("acct-paypal-usd"))
                .andExpect(jsonPath("$.data.balance").value(0.0))
                .andExpect(jsonPath("$.data.frozenBalance").value(0.0));

        String paymentResponse = mockMvc.perform(post("/fms/api/in/v1/payment/pay")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "channelCode":"PAYPAL",
                                  "businessType":"ORDER_RECEIVABLE",
                                  "businessId":"ORDER-100",
                                  "amount":120.00,
                                  "currency":"USD",
                                  "payParams":{
                                    "source":"amazon",
                                    "store":"US-STORE"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value(tenantId))
                .andExpect(jsonPath("$.data.channelCode").value("PAYPAL"))
                .andExpect(jsonPath("$.data.businessType").value("ORDER_RECEIVABLE"))
                .andExpect(jsonPath("$.data.businessId").value("ORDER-100"))
                .andExpect(jsonPath("$.data.amount").value(120.0))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andReturn().getResponse().getContentAsString();
        String transactionId = objectMapper.readTree(paymentResponse).at("/data/transactionId").asText();

        mockMvc.perform(get("/fms/api/in/v1/payment/accounts/acct-paypal-usd/balance")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(120.0));

        mockMvc.perform(post("/fms/api/in/v1/payment/refund")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionId":"%s",
                                  "refundAmount":20.00,
                                  "reason":"customer-return"
                                }
                                """.formatted(transactionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.businessType").value("REFUND"))
                .andExpect(jsonPath("$.data.amount").value(20.0))
                .andExpect(jsonPath("$.data.status").value("REFUNDED"))
                .andExpect(jsonPath("$.data.refTransactionId").value(transactionId));

        mockMvc.perform(get("/fms/api/in/v1/payment/accounts/acct-paypal-usd/balance")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(100.0));

        mockMvc.perform(post("/fms/api/in/v1/payment/batch-pay")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "channelCode":"PAYPAL",
                                  "items":[
                                    {
                                      "businessType":"SETTLEMENT_RECEIVABLE",
                                      "businessId":"SETTLEMENT-100",
                                      "amount":30.00,
                                      "currency":"USD"
                                    },
                                    {
                                      "businessType":"SETTLEMENT_RECEIVABLE",
                                      "businessId":"SETTLEMENT-101",
                                      "amount":-5.00,
                                      "currency":"USD"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionIds.length()").value(1))
                .andExpect(jsonPath("$.data.totalAmount").value(30.0))
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failCount").value(1));

        mockMvc.perform(get("/fms/api/in/v1/payment/accounts/acct-paypal-usd/balance")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(130.0));

        mockMvc.perform(post("/fms/api/in/v1/payment/settle/withdraw")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId":"acct-paypal-usd",
                                  "amount":50.00,
                                  "fromCurrency":"USD",
                                  "toCurrency":"CNY",
                                  "settlementType":"WITHDRAW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value("acct-paypal-usd"))
                .andExpect(jsonPath("$.data.amount").value(50.0))
                .andExpect(jsonPath("$.data.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.data.toCurrency").value("CNY"))
                .andExpect(jsonPath("$.data.settlementType").value("WITHDRAW"))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));

        mockMvc.perform(get("/fms/api/in/v1/payment/accounts/acct-paypal-usd/balance")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(80.0));

        mockMvc.perform(post("/fms/api/in/v1/payment/settle/amazon-claim")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId":"AMZ-ORDER-100",
                                  "claimAmount":15.00,
                                  "reason":"lost-package"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value("AMZ-ORDER-100"))
                .andExpect(jsonPath("$.data.amount").value(15.0))
                .andExpect(jsonPath("$.data.settlementType").value("AMAZON_CLAIM"))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        mockMvc.perform(get("/fms/api/in/v1/payment/reconcile")
                        .header("X-Tenant-Id", tenantId)
                        .param("channelCode", "PAYPAL")
                        .param("periodStart", periodStart)
                        .param("periodEnd", periodEnd))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channelCode").value("PAYPAL"))
                .andExpect(jsonPath("$.data.totalCount").value(3))
                .andExpect(jsonPath("$.data.totalAmount").value(130.0))
                .andExpect(jsonPath("$.data.refundCount").value(1))
                .andExpect(jsonPath("$.data.discrepancies.length()").value(0));

        mockMvc.perform(get("/fms/api/in/v1/payment/channels")
                        .header("X-Tenant-Id", "tenant-fms-payment-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void managePlatformPaymentAggregationWithExplicitTenantContext() throws Exception {
        String tenantId = "tenant-platform-payment";
        String periodStart = LocalDate.now(ZoneOffset.UTC).minusDays(1).toString();
        String periodEnd = LocalDate.now(ZoneOffset.UTC).plusDays(1).toString();

        mockMvc.perform(post("/platform/fms/api/v1/payment/channels/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "channelCode":"STRIPE",
                                  "channelName":"Stripe Global",
                                  "channelType":"PAYMENT_GATEWAY",
                                  "config":{
                                    "merchantId":"stripe-merchant-1"
                                  }
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channelCode").value("STRIPE"))
                .andExpect(jsonPath("$.data.channelName").value("Stripe Global"))
                .andExpect(jsonPath("$.data.config.merchantId").value("stripe-merchant-1"));

        mockMvc.perform(post("/platform/fms/api/v1/payment/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "channelCode":"STRIPE",
                                  "accountId":"acct-stripe-usd",
                                  "accountName":"Stripe USD Account",
                                  "currency":"USD",
                                  "credentials":{
                                    "apiKey":"sk-test-100"
                                  }
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value("acct-stripe-usd"))
                .andExpect(jsonPath("$.data.balance").value(0.0));

        String paymentResponse = mockMvc.perform(post("/platform/fms/api/v1/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "channelCode":"STRIPE",
                                  "businessType":"ORDER_RECEIVABLE",
                                  "businessId":"ORDER-PLATFORM-100",
                                  "amount":60.00,
                                  "currency":"USD"
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value(tenantId))
                .andExpect(jsonPath("$.data.channelCode").value("STRIPE"))
                .andExpect(jsonPath("$.data.amount").value(60.0))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andReturn().getResponse().getContentAsString();
        String transactionId = objectMapper.readTree(paymentResponse).at("/data/transactionId").asText();

        mockMvc.perform(get("/platform/fms/api/v1/payment/balance")
                        .param("tenantId", tenantId)
                        .param("accountId", "acct-stripe-usd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value("acct-stripe-usd"))
                .andExpect(jsonPath("$.data.balance").value(60.0));

        mockMvc.perform(post("/platform/fms/api/v1/payment/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "transactionId":"%s",
                                  "amount":10.00,
                                  "reason":"platform-adjustment"
                                }
                                """.formatted(tenantId, transactionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.businessType").value("REFUND"))
                .andExpect(jsonPath("$.data.amount").value(10.0))
                .andExpect(jsonPath("$.data.refTransactionId").value(transactionId));

        mockMvc.perform(post("/platform/fms/api/v1/payment/settlement/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "accountId":"acct-stripe-usd",
                                  "amount":20.00,
                                  "fromCurrency":"USD",
                                  "toCurrency":"CNY",
                                  "settlementType":"WITHDRAW"
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value("acct-stripe-usd"))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));

        mockMvc.perform(post("/platform/fms/api/v1/payment/amazon-claim")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "orderId":"ORDER-CLAIM-PLATFORM",
                                  "claimAmount":6.50,
                                  "reason":"carrier-loss"
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value("ORDER-CLAIM-PLATFORM"))
                .andExpect(jsonPath("$.data.settlementType").value("AMAZON_CLAIM"))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        mockMvc.perform(get("/platform/fms/api/v1/payment/reconciliation")
                        .param("tenantId", tenantId)
                        .param("channelCode", "STRIPE")
                        .param("periodStart", periodStart)
                        .param("periodEnd", periodEnd))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channelCode").value("STRIPE"))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.totalAmount").value(50.0))
                .andExpect(jsonPath("$.data.refundCount").value(1))
                .andExpect(jsonPath("$.data.discrepancies.length()").value(0));

        mockMvc.perform(get("/platform/fms/api/v1/payment/channels")
                        .param("tenantId", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].channelCode").value("STRIPE"));

        mockMvc.perform(get("/platform/fms/api/v1/payment/balance")
                        .param("tenantId", tenantId)
                        .param("accountId", "acct-stripe-usd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(30.0));
    }

    @Test
    void managePlatformInvoiceTaxAndVoucherLifecycleWithPersistentFmsServices() throws Exception {
        String tenantId = "tenant-platform-fms";

        String taxRuleResponse = mockMvc.perform(post("/platform/fms/api/v1/tax/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "countryCode":"DE",
                                  "taxType":"VAT",
                                  "taxRate":19.00,
                                  "taxCategory":"STANDARD"
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.countryCode").value("DE"))
                .andExpect(jsonPath("$.data.taxType").value("VAT"))
                .andExpect(jsonPath("$.data.taxRate").value(19.0))
                .andReturn().getResponse().getContentAsString();
        String taxRuleId = objectMapper.readTree(taxRuleResponse).at("/data/ruleId").asText();

        mockMvc.perform(get("/platform/fms/api/v1/tax/rules")
                        .param("tenantId", tenantId)
                        .param("countryCode", "DE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].ruleId").value(taxRuleId));

        mockMvc.perform(post("/platform/fms/api/v1/tax/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "countryCode":"DE",
                                  "taxType":"VAT",
                                  "amount":100.00
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taxAmount").value(19.0))
                .andExpect(jsonPath("$.data.totalAmount").value(119.0));

        String templateResponse = mockMvc.perform(post("/platform/fms/api/v1/voucher-engine/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "templateName":"平台销售出库凭证模板",
                                  "businessType":"SALES_OUTBOUND",
                                  "debitAccount":"6401",
                                  "creditAccount":"1405",
                                  "description":"平台中台共享销售出库凭证模板"
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.businessType").value("SALES_OUTBOUND"))
                .andReturn().getResponse().getContentAsString();
        String templateId = objectMapper.readTree(templateResponse).at("/data/templateId").asText();

        mockMvc.perform(get("/platform/fms/api/v1/voucher-engine/templates")
                        .param("tenantId", tenantId)
                        .param("businessType", "SALES_OUTBOUND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].templateId").value(templateId));

        String voucherResponse = mockMvc.perform(post("/platform/fms/api/v1/voucher-engine/auto-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "businessType":"SALES_OUTBOUND",
                                  "voucherType":"INVENTORY",
                                  "sourceId":"SO-PLATFORM-100",
                                  "amount":88.00,
                                  "currency":"USD",
                                  "sellerSku":"SKU-PLATFORM-100"
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.referenceType").value("SALES_OUTBOUND"))
                .andReturn().getResponse().getContentAsString();
        String voucherId = objectMapper.readTree(voucherResponse).at("/data/voucherId").asText();

        mockMvc.perform(post("/platform/fms/api/v1/voucher-engine/" + voucherId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "approvedBy":"platform.finance.manager"
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("POSTED"))
                .andExpect(jsonPath("$.data.postedBy").value("platform.finance.manager"));

        mockMvc.perform(get("/platform/fms/api/v1/voucher-engine/" + voucherId)
                        .param("tenantId", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.voucherId").value(voucherId))
                .andExpect(jsonPath("$.data.status").value("POSTED"));

        mockMvc.perform(get("/platform/fms/api/v1/voucher-engine/summary")
                        .param("tenantId", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1))
                .andExpect(jsonPath("$.data.totalDebit").value(88.0))
                .andExpect(jsonPath("$.data.totalCredit").value(88.0))
                .andExpect(jsonPath("$.data.byType.SALES_OUTBOUND").value(88.0));

        String invoiceResponse = mockMvc.perform(post("/platform/fms/api/v1/invoice/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "invoiceType":"SALES",
                                  "customerId":"CUST-PLATFORM-100",
                                  "customerName":"Platform Buyer",
                                  "countryCode":"DE",
                                  "currency":"EUR",
                                  "subtotalAmount":100.00,
                                  "taxAmount":19.00,
                                  "taxIdNumber":"DE123456789",
                                  "remark":"平台中台发票生成"
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.totalAmount").value(119.0))
                .andReturn().getResponse().getContentAsString();
        String invoiceId = objectMapper.readTree(invoiceResponse).at("/data/invoiceId").asText();

        mockMvc.perform(post("/platform/fms/api/v1/invoice/" + invoiceId + "/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s"
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ISSUED"));

        mockMvc.perform(post("/platform/fms/api/v1/invoice/" + invoiceId + "/red-flush")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId":"%s",
                                  "reason":"customer-return"
                                }
                                """.formatted(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.invoiceType").value("RED_INVOICE"))
                .andExpect(jsonPath("$.data.totalAmount").value(-119.0))
                .andExpect(jsonPath("$.data.status").value("ISSUED"));

        mockMvc.perform(get("/platform/fms/api/v1/invoice/list")
                        .param("tenantId", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(get("/platform/fms/api/v1/invoice/list")
                        .param("tenantId", "tenant-platform-fms-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void managePlatformFinanceSyncAndInvoiceSettingsWithPersistentFmsServices() throws Exception {
        HttpServer server = createYonyouStubServer();
        server.start();
        try {
            String tenantId = "tenant-platform-fms-sync";
            String apiUrl = "http://127.0.0.1:" + server.getAddress().getPort();

            String configResponse = mockMvc.perform(post("/platform/fms/api/v1/finance-sync/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s",
                                      "financeSystem":"YONYOU",
                                      "apiUrl":"%s",
                                      "apiKey":"platform-key",
                                      "apiSecret":"platform-secret",
                                      "accountSet":"U8-PLATFORM",
                                      "enabled":true,
                                      "mappingRules":{
                                        "voucherType":"INVENTORY",
                                        "store":"DE"
                                      }
                                    }
                                    """.formatted(tenantId, apiUrl)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.financeSystem").value("YONYOU"))
                    .andExpect(jsonPath("$.data.enabled").value(true))
                    .andReturn().getResponse().getContentAsString();
            String configId = objectMapper.readTree(configResponse).at("/data/configId").asText();

            mockMvc.perform(post("/platform/fms/api/v1/finance-sync/configs/" + configId + "/test")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s"
                                    }
                                    """.formatted(tenantId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));

            mockMvc.perform(get("/platform/fms/api/v1/finance-sync/configs")
                            .param("tenantId", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].configId").value(configId))
                    .andExpect(jsonPath("$.data[0].mappingRules.store").value("DE"));

            mockMvc.perform(post("/platform/fms/api/v1/voucher-engine/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s",
                                      "templateName":"平台库存凭证模板",
                                      "businessType":"SALES_OUTBOUND",
                                      "debitAccount":"6401",
                                      "creditAccount":"1405",
                                      "description":"平台财务同步测试模板"
                                    }
                                    """.formatted(tenantId)))
                    .andExpect(status().isOk());

            String voucherResponse = mockMvc.perform(post("/platform/fms/api/v1/voucher-engine/auto-generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s",
                                      "businessType":"SALES_OUTBOUND",
                                      "voucherType":"INVENTORY",
                                      "sourceId":"SO-PLATFORM-200",
                                      "amount":168.00,
                                      "currency":"EUR",
                                      "sellerSku":"SKU-PLATFORM-200"
                                    }
                                    """.formatted(tenantId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("DRAFT"))
                    .andReturn().getResponse().getContentAsString();
            String voucherId = objectMapper.readTree(voucherResponse).at("/data/voucherId").asText();

            mockMvc.perform(post("/platform/fms/api/v1/voucher-engine/" + voucherId + "/approve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s",
                                      "approvedBy":"platform.finance.manager"
                                    }
                                    """.formatted(tenantId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("POSTED"));

            mockMvc.perform(post("/platform/fms/api/v1/finance-sync/vouchers/" + voucherId + "/push")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s",
                                      "financeSystem":"YONYOU"
                                    }
                                    """.formatted(tenantId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));

            mockMvc.perform(get("/platform/fms/api/v1/finance-sync/vouchers")
                            .param("tenantId", tenantId)
                            .param("financeSystem", "YONYOU")
                            .param("syncStatus", "SYNCED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].financeSystem").value("YONYOU"))
                    .andExpect(jsonPath("$.data[0].voucherNumber").value("YY-SYNC-100"));

            String invoiceSettingResponse = mockMvc.perform(post("/platform/fms/api/v1/invoice/settings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s",
                                      "storeId":"store-de",
                                      "marketplaceId":"AMAZON-DE",
                                      "templateId":"invoice-template-de",
                                      "invoiceTitle":"DE Store GmbH",
                                      "taxRegistrationNo":"DE123456789",
                                      "showUnitPrice":true,
                                      "showTaxRate":true,
                                      "showDiscount":false,
                                      "remark":"欧盟销售发票"
                                    }
                                    """.formatted(tenantId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.storeId").value("store-de"))
                    .andExpect(jsonPath("$.data.marketplaceId").value("AMAZON-DE"))
                    .andReturn().getResponse().getContentAsString();
            String settingId = objectMapper.readTree(invoiceSettingResponse).at("/data/settingId").asText();

            mockMvc.perform(get("/platform/fms/api/v1/invoice/settings")
                            .param("tenantId", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].settingId").value(settingId));

            mockMvc.perform(get("/platform/fms/api/v1/invoice/settings/current")
                            .param("tenantId", tenantId)
                            .param("storeId", "store-de")
                            .param("marketplaceId", "AMAZON-DE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.settingId").value(settingId))
                    .andExpect(jsonPath("$.data.invoiceTitle").value("DE Store GmbH"));

            mockMvc.perform(get("/platform/fms/api/v1/invoice/settings")
                            .param("tenantId", "tenant-platform-fms-sync-other"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void retryFailedFinanceSyncAndKeepVoucherExportStateConsistent() throws Exception {
        HttpServer server = createFlakyYonyouStubServer();
        server.start();
        try {
            String tenantId = "tenant-platform-fms-retry";
            String apiUrl = "http://127.0.0.1:" + server.getAddress().getPort();

            mockMvc.perform(post("/platform/fms/api/v1/finance-sync/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s",
                                      "financeSystem":"YONYOU",
                                      "apiUrl":"%s",
                                      "apiKey":"retry-key",
                                      "apiSecret":"retry-secret",
                                      "accountSet":"U8-RETRY",
                                      "enabled":true,
                                      "mappingRules":{
                                        "voucherType":"INVENTORY"
                                      }
                                    }
                                    """.formatted(tenantId, apiUrl)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/platform/fms/api/v1/voucher-engine/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s",
                                      "templateName":"重试同步凭证模板",
                                      "businessType":"SALES_OUTBOUND",
                                      "debitAccount":"6401",
                                      "creditAccount":"1405",
                                      "description":"外部财务推送失败补偿测试"
                                    }
                                    """.formatted(tenantId)))
                    .andExpect(status().isOk());

            String voucherResponse = mockMvc.perform(post("/platform/fms/api/v1/voucher-engine/auto-generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s",
                                      "businessType":"SALES_OUTBOUND",
                                      "voucherType":"INVENTORY",
                                      "sourceId":"SO-PLATFORM-RETRY-100",
                                      "amount":128.00,
                                      "currency":"USD",
                                      "sellerSku":"SKU-PLATFORM-RETRY-100"
                                    }
                                    """.formatted(tenantId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("DRAFT"))
                    .andReturn().getResponse().getContentAsString();
            String voucherId = objectMapper.readTree(voucherResponse).at("/data/voucherId").asText();

            mockMvc.perform(post("/platform/fms/api/v1/voucher-engine/" + voucherId + "/approve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s",
                                      "approvedBy":"platform.finance.retry.manager"
                                    }
                                    """.formatted(tenantId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("POSTED"));

            mockMvc.perform(post("/platform/fms/api/v1/finance-sync/vouchers/" + voucherId + "/push")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s",
                                      "financeSystem":"YONYOU"
                                    }
                                    """.formatted(tenantId)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("FINANCE_SYNC_FAILED"));

            String failedVoucherResponse = mockMvc.perform(get("/platform/fms/api/v1/finance-sync/vouchers")
                            .param("tenantId", tenantId)
                            .param("financeSystem", "YONYOU")
                            .param("syncStatus", "FAILED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].syncStatus").value("FAILED"))
                    .andReturn().getResponse().getContentAsString();
            String externalVoucherId = objectMapper.readTree(failedVoucherResponse).at("/data/0/voucherId").asText();

            mockMvc.perform(get("/platform/fms/api/v1/finance-sync/vouchers/" + externalVoucherId)
                            .param("tenantId", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.voucherId").value(externalVoucherId))
                    .andExpect(jsonPath("$.data.erpVoucherId").value(voucherId))
                    .andExpect(jsonPath("$.data.syncStatus").value("FAILED"))
                    .andExpect(jsonPath("$.data.syncError", Matchers.containsString("500")));

            mockMvc.perform(get("/fms/api/out/v1/vouchers/" + voucherId)
                            .header("X-Tenant-Id", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("POSTED"));

            mockMvc.perform(post("/platform/fms/api/v1/finance-sync/vouchers/" + externalVoucherId + "/retry")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "tenantId":"%s"
                                    }
                                    """.formatted(tenantId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.voucherId").value(externalVoucherId))
                    .andExpect(jsonPath("$.data.syncStatus").value("SYNCED"));

            mockMvc.perform(get("/platform/fms/api/v1/finance-sync/vouchers/" + externalVoucherId)
                            .param("tenantId", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.syncStatus").value("SYNCED"))
                    .andExpect(jsonPath("$.data.syncedAt").isNotEmpty());

            mockMvc.perform(get("/fms/api/out/v1/external-vouchers/" + externalVoucherId)
                            .header("X-Tenant-Id", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.erpVoucherId").value(voucherId))
                    .andExpect(jsonPath("$.data.syncStatus").value("SYNCED"));

            mockMvc.perform(get("/fms/api/out/v1/vouchers/" + voucherId)
                            .header("X-Tenant-Id", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("EXPORTED"))
                    .andExpect(jsonPath("$.data.exportBatchId", Matchers.startsWith("YY-")));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void createFinanceSyncConfigAndPushVoucherToYonyou() throws Exception {
        HttpServer server = createYonyouStubServer();
        server.start();
        try {
            String apiUrl = "http://127.0.0.1:" + server.getAddress().getPort();

            mockMvc.perform(post("/fms/api/in/v1/finance-sync-configs")
                            .header("X-Tenant-Id", "tenant-fms-sync")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "financeSystem":"YONYOU",
                                      "apiUrl":"%s",
                                      "apiKey":"test-key",
                                      "apiSecret":"test-secret",
                                      "accountSet":"U8-TEST",
                                      "enabled":true,
                                      "mappingRules":{
                                        "voucherType":"INVENTORY"
                                      }
                                    }
                                    """.formatted(apiUrl)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.financeSystem").value("YONYOU"))
                    .andExpect(jsonPath("$.data.enabled").value(true));

            mockMvc.perform(get("/fms/api/in/v1/finance-sync-configs")
                            .header("X-Tenant-Id", "tenant-fms-sync"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].financeSystem").value("YONYOU"));

            mockMvc.perform(post("/fms/api/in/v1/voucher-templates")
                            .header("X-Tenant-Id", "tenant-fms-sync")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "templateName":"销售出库凭证模板",
                                      "businessType":"SALES_OUTBOUND",
                                      "debitAccount":"6401",
                                      "creditAccount":"1405",
                                      "description":"销售出库自动凭证"
                                    }
                                    """))
                    .andExpect(status().isOk());

            String voucherResponse = mockMvc.perform(post("/fms/api/in/v1/inventory-vouchers/auto-generate")
                            .header("X-Tenant-Id", "tenant-fms-sync")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "businessType":"SALES_OUTBOUND",
                                      "voucherType":"INVENTORY",
                                      "sourceId":"SO-YY-100",
                                      "amount":88.00,
                                      "currency":"USD",
                                      "sellerSku":"SKU-YY-100"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("DRAFT"))
                    .andReturn().getResponse().getContentAsString();
            String voucherId = objectMapper.readTree(voucherResponse).at("/data/voucherId").asText();

            mockMvc.perform(post("/fms/api/in/v1/vouchers/" + voucherId + "/approve")
                            .header("X-Tenant-Id", "tenant-fms-sync")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "approvedBy":"finance.director"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("POSTED"));

            mockMvc.perform(post("/fms/api/out/v1/vouchers/" + voucherId + "/push-yonyou")
                            .header("X-Tenant-Id", "tenant-fms-sync"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));

            mockMvc.perform(get("/fms/api/out/v1/vouchers/" + voucherId)
                            .header("X-Tenant-Id", "tenant-fms-sync"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("EXPORTED"))
                    .andExpect(jsonPath("$.data.exportBatchId", Matchers.startsWith("YY-")));

            mockMvc.perform(get("/fms/api/out/v1/external-vouchers")
                            .header("X-Tenant-Id", "tenant-fms-sync")
                            .param("financeSystem", "YONYOU")
                            .param("syncStatus", "SYNCED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].financeSystem").value("YONYOU"))
                    .andExpect(jsonPath("$.data[0].voucherType").value("INVENTORY"))
                    .andExpect(jsonPath("$.data[0].voucherNumber").value("YY-SYNC-100"))
                    .andExpect(jsonPath("$.data[0].erpReferenceType").value("SALES_OUTBOUND"))
                    .andExpect(jsonPath("$.data[0].erpReferenceId").value("SO-YY-100"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void submitPmsCostAnomalyApproveAndApplyToProfitLifecycle() throws Exception {
        String tenantId = "tenant-fms-ai-cost";

        mockMvc.perform(post("/api/pms/v1/ai-toggles")
                        .header("tenant_id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "feature_code":"FMS_AI_COST",
                                  "feature_name":"FMS AI Cost",
                                  "domain":"FMS",
                                  "enabled":true,
                                  "description":"Enable PMS to submit FMS AI cost anomaly recommendations.",
                                  "config_json":"{}"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.domain").value("FMS"))
                .andExpect(jsonPath("$.data.enabled").value(true));

        String recommendationResponse = mockMvc.perform(pmsHeaders(post("/api/pms/v1/recommendations"), tenantId, "idem-fms-cost-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recommendation_id":"pms-fms-cost-001",
                                  "domain":"FMS",
                                  "recommendation_type":"AI_COST_AGGREGATION",
                                  "object_type":"PENDING_ACTION",
                                  "target_object_type":"ORDER",
                                  "target_object_id":"ORD-AI-COST-100",
                                  "content":"PMS detected a missing other-cost event and recommends finance approval before applying it to store profitability.",
                                  "score":91.2,
                                  "confidence":0.88,
                                  "evidence_chain_id":"ev-fms-cost-001",
                                  "data_sources":["ERP_COST_LEDGER","PMS_COST_AGENT"],
                                  "risk_flags":["FINANCE_APPROVAL_REQUIRED"],
                                  "explainability":"based on missing cost-event comparison against peer orders",
                                  "requested_action":"CREATE_COST_ANOMALY_FOR_APPROVAL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                .andReturn().getResponse().getContentAsString();
        String erpReferenceId = objectMapper.readTree(recommendationResponse).at("/data/erpReferenceId").asText();

        String submitResponse = mockMvc.perform(pmsHeaders(post("/fms/api/in/v1/pms/cost-anomaly"), tenantId, "idem-fms-cost-submit-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "erpReferenceId":"%s",
                                  "anomalyType":"MISSING_COST",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-AI-COST-100",
                                  "dimensionType":"STORE",
                                  "dimensionId":"store-ai-1",
                                  "sellerSku":"SKU-AI-COST-100",
                                  "storeId":"store-ai-1",
                                  "channelCode":"AMAZON",
                                  "marketplaceId":"AMAZON-US",
                                  "costType":"OTHER",
                                  "suggestedAmount":15.50,
                                  "currency":"USD",
                                  "autoAggregate":true,
                                  "reason":"PMS detected a missing customs-related service fee that was not captured by ERP cost events.",
                                  "evidence":{
                                    "baselineOrderCount":12,
                                    "deviationRate":0.22,
                                    "detectedBy":"pms-cost-agent"
                                  }
                                }
                                """.formatted(erpReferenceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.data.erpReferenceId").value(erpReferenceId))
                .andExpect(jsonPath("$.data.autoAggregate").value(true))
                .andExpect(jsonPath("$.data.suggestedAmount").value(15.5))
                .andReturn().getResponse().getContentAsString();
        String anomalyId = objectMapper.readTree(submitResponse).at("/data/anomalyId").asText();

        mockMvc.perform(pmsHeaders(post("/fms/api/in/v1/pms/cost-anomaly"), tenantId, "idem-fms-cost-submit-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "erpReferenceId":"%s",
                                  "anomalyType":"MISSING_COST",
                                  "sourceType":"OMS_ORDER",
                                  "sourceId":"ORD-AI-COST-100",
                                  "dimensionType":"STORE",
                                  "dimensionId":"store-ai-1",
                                  "sellerSku":"SKU-AI-COST-100",
                                  "storeId":"store-ai-1",
                                  "channelCode":"AMAZON",
                                  "marketplaceId":"AMAZON-US",
                                  "costType":"OTHER",
                                  "suggestedAmount":15.50,
                                  "currency":"USD",
                                  "autoAggregate":true,
                                  "reason":"PMS detected a missing customs-related service fee that was not captured by ERP cost events.",
                                  "evidence":{
                                    "baselineOrderCount":12,
                                    "deviationRate":0.22,
                                    "detectedBy":"pms-cost-agent"
                                  }
                                }
                                """.formatted(erpReferenceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.anomalyId").value(anomalyId))
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));

        mockMvc.perform(get("/fms/api/in/v1/pms/cost-anomaly/" + anomalyId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.anomalyId").value(anomalyId))
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));

        mockMvc.perform(post("/fms/api/in/v1/pms/cost-anomaly/" + anomalyId + "/approve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approvedBy":"finance.ai.manager"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPLIED"))
                .andExpect(jsonPath("$.data.approvedBy").value("finance.ai.manager"))
                .andExpect(jsonPath("$.data.effectiveCostEventId").isNotEmpty())
                .andExpect(jsonPath("$.data.allocationResultIds.length()").value(1));

        mockMvc.perform(get("/fms/api/in/v1/cost-events/by-source")
                        .header("X-Tenant-Id", tenantId)
                        .param("sourceType", "OMS_ORDER")
                        .param("sourceId", "ORD-AI-COST-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].costType").value("OTHER"))
                .andExpect(jsonPath("$.data[0].sellerSku").value("SKU-AI-COST-100"))
                .andExpect(jsonPath("$.data[0].amount").value(15.5));

        mockMvc.perform(post("/fms/api/in/v1/engine/profit-results")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dimensionType":"STORE",
                                  "dimensionId":"store-ai-1",
                                  "sellerSku":"SKU-AI-COST-100",
                                  "orderId":"ORD-AI-COST-100",
                                  "storeId":"store-ai-1",
                                  "marketplaceId":"AMAZON-US",
                                  "revenue":50.00,
                                  "currency":"USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCost").value(15.5))
                .andExpect(jsonPath("$.data.otherCost").value(15.5))
                .andExpect(jsonPath("$.data.grossProfit").value(34.5));

        mockMvc.perform(pmsHeaders(get("/api/pms/v1/feedbacks/pending"), tenantId, "idem-fms-cost-feedback-read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].erpReferenceId").value(erpReferenceId))
                .andExpect(jsonPath("$.data[0].executionStatus").value("EXECUTED"));
    }

    private static MockHttpServletRequestBuilder pmsHeaders(MockHttpServletRequestBuilder builder, String tenantId, String idempotencyKey) {
        return builder.header("tenant_id", tenantId)
                .header("actor_id", "agent-finance-001")
                .header("actor_type", "agent")
                .header("agent_id", "pms-fms-agent-001")
                .header("scope", "store:store-ai-1,marketplace:AMAZON-US,data_level:DETAIL")
                .header("purpose", "ai_cost_anomaly_submit")
                .header("trace_id", "trace-" + idempotencyKey)
                .header("idempotency_key", idempotencyKey)
                .header("source_system", "PMS")
                .header("signature", "mock-signature");
    }

    private HttpServer createYonyouStubServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/u8cloud/api/v1/ping", exchange -> writeJson(exchange, 200, "{\"status\":\"ok\"}"));
        server.createContext("/u8cloud/api/v1/vouchers", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                writeJson(exchange, 405, "{\"code\":\"METHOD_NOT_ALLOWED\"}");
                return;
            }
            writeJson(exchange, 200, "{\"voucherNumber\":\"YY-SYNC-100\"}");
        });
        return server;
    }

    private HttpServer createFlakyYonyouStubServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger pushCount = new AtomicInteger();
        server.createContext("/u8cloud/api/v1/ping", exchange -> writeJson(exchange, 200, "{\"status\":\"ok\"}"));
        server.createContext("/u8cloud/api/v1/vouchers", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                writeJson(exchange, 405, "{\"code\":\"METHOD_NOT_ALLOWED\"}");
                return;
            }
            if (pushCount.incrementAndGet() == 1) {
                writeJson(exchange, 500, "{\"code\":\"TEMPORARY_FAILURE\"}");
                return;
            }
            writeJson(exchange, 200, "{\"voucherNumber\":\"YY-SYNC-RETRY-100\"}");
        });
        return server;
    }

    private void writeJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}

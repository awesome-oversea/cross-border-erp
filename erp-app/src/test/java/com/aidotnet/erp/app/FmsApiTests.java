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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
}

package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class ScmApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manageSupplierPurchaseOrderApprovalReceiptAndTenantIsolation() throws Exception {
        String tenantId = uniqueTenant("scm-flow");
        String otherTenantId = uniqueTenant("scm-flow-other");
        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
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
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Shenzhen Supplier",
                                  "contactName":"Bob",
                                  "countryCode":"CN",
                                  "contacts":[
                                    {
                                      "name":"Bob",
                                      "role":"Sales",
                                      "email":"bob@supplier.test",
                                      "phone":"13800000000",
                                      "primaryContact":true
                                    }
                                  ],
                                  "qualifications":[
                                    {
                                      "qualificationType":"BUSINESS_LICENSE",
                                      "qualificationNo":"BL-001",
                                      "status":"VALID"
                                    }
                                  ],
                                  "score":{
                                    "qualityScore":95,
                                    "deliveryScore":96,
                                    "priceScore":92,
                                    "serviceScore":94,
                                    "evaluationCount":3
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.supplier.tenantId").value(tenantId))
                .andExpect(jsonPath("$.data.contacts[0].name").value("Bob"))
                .andExpect(jsonPath("$.data.qualifications[0].qualificationType").value("BUSINESS_LICENSE"))
                .andExpect(jsonPath("$.data.score.overallScore").value(94.5))
                .andReturn().getResponse().getContentAsString();
        String supplierId = objectMapper.readTree(supplierResponse).at("/data/supplier/supplierId").asText();

        mockMvc.perform(get("/scm/api/in/v1/suppliers/" + supplierId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.supplier.supplierId").value(supplierId))
                .andExpect(jsonPath("$.data.contacts[0].email").value("bob@supplier.test"));

        String poResponse = mockMvc.perform(post("/scm/api/in/v1/purchase-orders")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supplierId\":\"" + supplierId + "\",\"currency\":\"USD\",\"lines\":[{\"lineId\":\"L1\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":10,\"unitCost\":12.50}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.totalAmount").value(125.0))
                .andReturn().getResponse().getContentAsString();
        String poId = objectMapper.readTree(poResponse).at("/data/poId").asText();

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/approve")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PO_STATUS_INVALID"));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/submit")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/approve")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"warehouseId\":\"" + warehouseId + "\"," +
                                "\"receipts\":[{\"lineId\":\"L1\",\"quantity\":4}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_RECEIVED"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].sellerSku").value("SPK-BLK-US"))
                .andExpect(jsonPath("$.data[0].onHand").value(4))
                .andExpect(jsonPath("$.data[0].reserved").value(0));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory-transactions")
                        .header("X-Tenant-Id", tenantId)
                        .param("sellerSku", "SPK-BLK-US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].transactionType").value("RECEIVE"))
                .andExpect(jsonPath("$.data[0].quantity").value(4))
                .andExpect(jsonPath("$.data[0].referenceType").value("MANUAL_RECEIVE"));

        mockMvc.perform(get("/fms/api/in/v1/cost-events/by-source")
                        .header("X-Tenant-Id", tenantId)
                        .param("sourceType", "SCM_PURCHASE_ORDER")
                        .param("sourceId", poId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].costType").value("PRODUCT_COST"))
                .andExpect(jsonPath("$.data[0].sellerSku").value("SPK-BLK-US"))
                .andExpect(jsonPath("$.data[0].amount").value(50.0));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"warehouseId\":\"" + warehouseId + "\"," +
                                "\"receipts\":[{\"lineId\":\"L1\",\"quantity\":7}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PO_RECEIVE_EXCEEDS_ORDERED"));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/receive")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"warehouseId\":\"" + warehouseId + "\"," +
                                "\"receipts\":[{\"lineId\":\"L1\",\"quantity\":6}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECEIVED"));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].onHand").value(10))
                .andExpect(jsonPath("$.data[0].reserved").value(0));

        mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory-transactions")
                        .header("X-Tenant-Id", tenantId)
                        .param("sellerSku", "SPK-BLK-US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].quantity", Matchers.containsInAnyOrder(4, 6)));

        mockMvc.perform(get("/fms/api/in/v1/cost-events/by-source")
                        .header("X-Tenant-Id", tenantId)
                        .param("sourceType", "SCM_PURCHASE_ORDER")
                        .param("sourceId", poId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].amount", Matchers.containsInAnyOrder(50.0, 75.0)));

        mockMvc.perform(get("/scm/api/in/v1/purchase-orders")
                        .header("X-Tenant-Id", otherTenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(put("/scm/api/in/v1/suppliers/" + supplierId)
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status":"INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.supplier.status").value("INACTIVE"));

        mockMvc.perform(post("/scm/api/in/v1/purchase-orders")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supplierId\":\"" + supplierId + "\",\"currency\":\"USD\",\"lines\":[{\"lineId\":\"L2\",\"sellerSku\":\"SPK-BLK-US\",\"quantity\":1,\"unitCost\":12.50}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SUPPLIER_DISABLED"));
    }

    @Test
    void generatePurchasePlanFromOmsAndSuggestionThenCreatePurchaseOrder() throws Exception {
        String tenantId = uniqueTenant("scm-plan");
        String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"US-TX",
                                  "name":"Texas Plan Warehouse",
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
                                  "sellerSku":"SPK-PLAN-US",
                                  "quantity":4,
                                  "referenceType":"BOOTSTRAP",
                                  "referenceId":"seed-plan-1"
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk());

        String supplierResponse = mockMvc.perform(post("/scm/api/in/v1/suppliers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Plan Supplier",
                                  "contactName":"Luna",
                                  "countryCode":"CN"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String supplierId = objectMapper.readTree(supplierResponse).at("/data/supplier/supplierId").asText();

        String orderResponse = mockMvc.perform(post("/oms/api/in/v1/orders/import")
                        .header("X-Tenant-Id", tenantId)
                        .header("X-Trace-Id", "trace-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platform":"Amazon",
                                  "platformOrderNo":"AMZ-PLAN-100",
                                  "buyerName":"Plan Buyer",
                                  "countryCode":"US",
                                  "shippingAddress":"10 Main St, Austin, TX",
                                  "currency":"USD",
                                  "lines":[
                                    {
                                      "lineId":"PLAN-L1",
                                      "sellerSku":"SPK-PLAN-US",
                                      "title":"Plan Speaker",
                                      "quantity":10,
                                      "unitPrice":29.99
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andReturn().getResponse().getContentAsString();
        String orderId = objectMapper.readTree(orderResponse).at("/data/orderId").asText();

        mockMvc.perform(get("/oms/api/in/v1/orders/procurement-demand")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerSku").value("SPK-PLAN-US"))
                .andExpect(jsonPath("$.data[0].orderDemandQuantity").value(10))
                .andExpect(jsonPath("$.data[0].orderIds[0]").value(orderId));

        String suggestionResponse = mockMvc.perform(post("/scm/api/in/v1/replenishment-suggestions")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sellerSku":"SPK-PLAN-US",
                                  "warehouseId":"%s",
                                  "currentStock":5,
                                  "avgDailySales":2,
                                  "leadTimeDays":4
                                }
                                """.formatted(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.suggestedQuantity").value(7))
                .andReturn().getResponse().getContentAsString();
        String suggestionId = objectMapper.readTree(suggestionResponse).at("/data/suggestionId").asText();

        mockMvc.perform(patch("/scm/api/in/v1/replenishment-suggestions/" + suggestionId + "/accept")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        String openPoResponse = mockMvc.perform(post("/scm/api/in/v1/purchase-orders")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId":"%s",
                                  "currency":"USD",
                                  "purchaseType":"STANDARD",
                                  "lines":[
                                    {
                                      "lineId":"OPEN-L1",
                                      "sellerSku":"SPK-PLAN-US",
                                      "quantity":3,
                                      "unitCost":8.50
                                    }
                                  ]
                                }
                                """.formatted(supplierId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        String openPoId = objectMapper.readTree(openPoResponse).at("/data/poId").asText();

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + openPoId + "/submit")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + openPoId + "/approve")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        String planResponse = mockMvc.perform(post("/scm/api/in/v1/purchase-plans/generate")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("GENERATED"))
                .andExpect(jsonPath("$.data.lines.length()").value(1))
                .andExpect(jsonPath("$.data.lines[0].sellerSku").value("SPK-PLAN-US"))
                .andExpect(jsonPath("$.data.lines[0].orderDemandQuantity").value(10))
                .andExpect(jsonPath("$.data.lines[0].replenishmentDemandQuantity").value(7))
                .andExpect(jsonPath("$.data.lines[0].availableInventoryQuantity").value(4))
                .andExpect(jsonPath("$.data.lines[0].inPurchasingQuantity").value(3))
                .andExpect(jsonPath("$.data.lines[0].suggestedPurchaseQuantity").value(10))
                .andReturn().getResponse().getContentAsString();
        String planId = objectMapper.readTree(planResponse).at("/data/planId").asText();
        String planLineId = objectMapper.readTree(planResponse).at("/data/lines/0/lineId").asText();

        mockMvc.perform(get("/scm/api/in/v1/purchase-plans/" + planId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planId").value(planId))
                .andExpect(jsonPath("$.data.lines[0].lineId").value(planLineId))
                .andExpect(jsonPath("$.data.lines[0].orderSourceRefs[0]").value(orderId))
                .andExpect(jsonPath("$.data.lines[0].suggestionSourceRefs[0]").value(suggestionId));

        String generatedPoResponse = mockMvc.perform(post("/scm/api/in/v1/purchase-plans/" + planId + "/purchase-orders")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId":"%s",
                                  "currency":"USD",
                                  "purchaseType":"STANDARD",
                                  "paymentTerms":"30D",
                                  "shippingTerms":"FOB",
                                  "notes":"generated from purchase plan",
                                  "lines":[
                                    {
                                      "planLineId":"%s",
                                      "unitCost":9.50
                                    }
                                  ]
                                }
                                """.formatted(supplierId, planLineId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.lines[0].sellerSku").value("SPK-PLAN-US"))
                .andExpect(jsonPath("$.data.lines[0].quantity").value(10))
                .andExpect(jsonPath("$.data.totalAmount").value(95.0))
                .andReturn().getResponse().getContentAsString();
        String generatedPoId = objectMapper.readTree(generatedPoResponse).at("/data/poId").asText();

        mockMvc.perform(get("/scm/api/in/v1/purchase-plans/" + planId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ORDERED"))
                .andExpect(jsonPath("$.data.lines[0].lineStatus").value("ORDERED"))
                .andExpect(jsonPath("$.data.lines[0].linkedPoId").value(generatedPoId));

        mockMvc.perform(post("/scm/api/in/v1/purchase-plans/" + planId + "/purchase-orders")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId":"%s",
                                  "currency":"USD",
                                  "lines":[
                                    {
                                      "planLineId":"%s",
                                      "unitCost":9.50
                                    }
                                  ]
                                }
                                """.formatted(supplierId, planLineId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PURCHASE_PLAN_LINE_ALREADY_ORDERED"));
    }

    @Test
    void managePurchaseApprovalFlowAndPaymentRequestFromPurchaseOrder() throws Exception {
        String tenantId = uniqueTenant("scm-approval");
        String supplierResponse = mockMvc.perform(post("/scm/api/in/v1/suppliers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Approval Supplier",
                                  "contactName":"Nina",
                                  "countryCode":"CN"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String supplierId = objectMapper.readTree(supplierResponse).at("/data/supplier/supplierId").asText();

        String poResponse = mockMvc.perform(post("/scm/api/in/v1/purchase-orders")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId":"%s",
                                  "currency":"USD",
                                  "paymentTerms":"30D",
                                  "purchaseType":"STANDARD",
                                  "lines":[
                                    {
                                      "lineId":"APP-L1",
                                      "sellerSku":"APP-SKU-001",
                                      "quantity":12,
                                      "unitCost":12.50
                                    }
                                  ]
                                }
                                """.formatted(supplierId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.totalAmount").value(150.0))
                .andReturn().getResponse().getContentAsString();
        String poId = objectMapper.readTree(poResponse).at("/data/poId").asText();

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/submit")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        String approvalFlowResponse = mockMvc.perform(post("/scm/api/in/v1/purchase-orders/" + poId + "/approvals")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approvers":[
                                    {"approverId":"manager-1"},
                                    {"approverId":"director-1"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].approvalLevel").value(1))
                .andExpect(jsonPath("$.data[1].approvalLevel").value(2))
                .andReturn().getResponse().getContentAsString();
        String firstApprovalId = objectMapper.readTree(approvalFlowResponse).at("/data/0/approvalId").asText();
        String secondApprovalId = objectMapper.readTree(approvalFlowResponse).at("/data/1/approvalId").asText();

        mockMvc.perform(post("/scm/api/in/v1/purchase-orders/" + poId + "/payment-requests")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId":"scm-pay-100",
                                  "amount":150.00,
                                  "requestedBy":"buyer-1",
                                  "approvalFlow":[
                                    {"node":"finance-manager"},
                                    {"node":"cashier"}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PO_STATUS_INVALID"));

        mockMvc.perform(post("/scm/api/in/v1/purchase-orders/" + poId + "/approvals/" + secondApprovalId + "/approve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "comment":"skip first level"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PURCHASE_APPROVAL_LEVEL_INVALID"));

        mockMvc.perform(post("/scm/api/in/v1/purchase-orders/" + poId + "/approvals/" + firstApprovalId + "/approve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "comment":"manager approved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.approvalLevel").value(1));

        mockMvc.perform(get("/scm/api/in/v1/purchase-orders/" + poId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        mockMvc.perform(post("/scm/api/in/v1/purchase-orders/" + poId + "/approvals/" + secondApprovalId + "/approve")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "comment":"director approved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.approvalLevel").value(2));

        mockMvc.perform(get("/scm/api/in/v1/purchase-orders/" + poId + "/approvals")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].status", Matchers.contains("APPROVED", "APPROVED")));

        mockMvc.perform(get("/scm/api/in/v1/purchase-orders/" + poId)
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(post("/scm/api/in/v1/purchase-orders/" + poId + "/payment-requests")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId":"scm-pay-100",
                                  "amount":150.00,
                                  "requestedBy":"buyer-1",
                                  "approvalFlow":[
                                    {"node":"finance-manager"},
                                    {"node":"cashier"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").value("scm-pay-100"))
                .andExpect(jsonPath("$.data.poId").value(poId))
                .andExpect(jsonPath("$.data.supplierId").value(supplierId))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.requestType").value("PROCUREMENT"));

        mockMvc.perform(get("/scm/api/in/v1/purchase-orders/" + poId + "/payment-requests")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].requestId").value("scm-pay-100"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    void managePurchaseTrackingExceptionsAndQuantityClosure() throws Exception {
        String tenantId = uniqueTenant("scm-tracking");
        String supplierResponse = mockMvc.perform(post("/scm/api/in/v1/suppliers")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Tracking Supplier",
                                  "contactName":"Iris",
                                  "countryCode":"CN"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String supplierId = objectMapper.readTree(supplierResponse).at("/data/supplier/supplierId").asText();

        String poResponse = mockMvc.perform(post("/scm/api/in/v1/purchase-orders")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId":"%s",
                                  "currency":"USD",
                                  "purchaseType":"STANDARD",
                                  "lines":[
                                    {
                                      "lineId":"TRK-L1",
                                      "sellerSku":"TRACK-SKU-1",
                                      "quantity":10,
                                      "unitCost":10.00
                                    },
                                    {
                                      "lineId":"TRK-L2",
                                      "sellerSku":"TRACK-SKU-2",
                                      "quantity":5,
                                      "unitCost":8.00
                                    }
                                  ]
                                }
                                """.formatted(supplierId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        String poId = objectMapper.readTree(poResponse).at("/data/poId").asText();

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/submit")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-orders/" + poId + "/approve")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(post("/scm/api/in/v1/purchase-orders/" + poId + "/trackings/init")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].status").value("PENDING_RECEIPT"))
                .andExpect(jsonPath("$.data[1].status").value("PENDING_RECEIPT"));

        mockMvc.perform(post("/scm/api/in/v1/purchase-orders/" + poId + "/trackings/TRK-L1/receipts")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receivedQuantity":6,
                                  "actualUnitCost":10.80
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lineId").value("TRK-L1"))
                .andExpect(jsonPath("$.data.receivedQuantity").value(6))
                .andExpect(jsonPath("$.data.pendingQuantity").value(4))
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_RECEIVED"));

        mockMvc.perform(post("/scm/api/in/v1/purchase-orders/" + poId + "/trackings/TRK-L2/receipts")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receivedQuantity":6,
                                  "actualUnitCost":8.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lineId").value("TRK-L2"))
                .andExpect(jsonPath("$.data.receivedQuantity").value(6))
                .andExpect(jsonPath("$.data.status").value("FULLY_RECEIVED"));

        String underDeliveredResponse = mockMvc.perform(post("/scm/api/in/v1/purchase-orders/" + poId + "/exceptions")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineId":"TRK-L1",
                                  "sellerSku":"TRACK-SKU-1",
                                  "exceptionType":"UNDER_DELIVERED",
                                  "expectedValue":10,
                                  "actualValue":6,
                                  "description":"supplier short shipped"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exceptionType").value("UNDER_DELIVERED"))
                .andReturn().getResponse().getContentAsString();
        String underDeliveredExceptionId =
                objectMapper.readTree(underDeliveredResponse).at("/data/exceptionId").asText();

        mockMvc.perform(get("/scm/api/in/v1/purchase-orders/" + poId + "/exceptions")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath(
                        "$.data[*].exceptionType",
                        Matchers.containsInAnyOrder("PRICE_INCREASED", "OVER_DELIVERED", "UNDER_DELIVERED")));

        mockMvc.perform(patch("/scm/api/in/v1/purchase-exceptions/" + underDeliveredExceptionId + "/handle")
                        .header("X-Tenant-Id", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "handlerId":"buyer-1",
                                  "handlerNote":"supplier confirmed shortage and close line",
                                  "resolution":"RESOLVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        mockMvc.perform(get("/scm/api/in/v1/purchase-orders/" + poId + "/trackings")
                        .header("X-Tenant-Id", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[?(@.lineId=='TRK-L1')].pendingQuantity").value(Matchers.contains(0)))
                .andExpect(jsonPath("$.data[?(@.lineId=='TRK-L1')].status").value(Matchers.contains("CLOSED_WITH_EXCEPTION")))
                .andExpect(jsonPath("$.data[?(@.lineId=='TRK-L2')].receivedQuantity").value(Matchers.contains(6)));
    }

    /**
     * PG 集成测试会复用同一 Spring 上下文和数据库实例。
     * 为避免不同用例之间租户数据串扰，每个场景使用独立租户编码。
     */
    private String uniqueTenant(String scenario) {
        return scenario + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

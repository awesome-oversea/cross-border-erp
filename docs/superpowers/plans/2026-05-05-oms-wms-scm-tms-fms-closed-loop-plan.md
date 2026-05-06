# OMS-WMS-SCM-TMS-FMS Closed Loop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a persistent, testable OMS→WMS→SCM→TMS→FMS business loop with shipment execution, inventory ledger integrity, purchase receipt linkage, and order-level cost/profit traceability.

**Architecture:** Keep all work inside the existing domain modules and wire the existing cross-domain clients more tightly instead of introducing parallel implementations. Use integration tests in `erp-app` to drive the loop end-to-end, then add only the minimum schema, mapper, service, and controller changes needed for order-scoped cost events and the smallest usable FMS aggregation/profit engine persistence layer.

**Tech Stack:** Java 17, Spring Boot 3, Spring MVC, OpenFeign, MyBatis XML mappers, Flyway, JUnit 5, MockMvc

---

## File Structure

### Primary files to modify

- `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/OmsApiTests.java`
  - Add fulfillment-plan, shipment sync log, and invalid shipment-path integration coverage.
- `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/ScmApiTests.java`
  - Add purchase receipt → WMS inbound → inventory ledger assertions.
- `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/FmsApiTests.java`
  - Add order-level profit anti-cross-order test and engine persistence/API test.
- `D:/Project/erp/erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java`
  - Tighten fulfillment execution, shipment creation, inventory reservation/deduction, and platform sync result behavior.
- `D:/Project/erp/erp-domain-oms/src/main/java/com/aidotnet/erp/oms/client/TmsClient.java`
  - Expand shipment request shape to carry warehouse/shipping metadata already used by the service.
- `D:/Project/erp/erp-domain-wms/src/main/java/com/aidotnet/erp/wms/application/InventoryService.java`
  - Expose transaction listing and preserve reference metadata through reserve/release/deduct/receive.
- `D:/Project/erp/erp-domain-scm/src/main/java/com/aidotnet/erp/scm/application/PurchaseService.java`
  - Preserve PO receipt trace when calling WMS and FMS.
- `D:/Project/erp/erp-domain-scm/src/main/java/com/aidotnet/erp/scm/client/FmsClient.java`
  - Add order-/reference-grade cost event fields required by FMS.
- `D:/Project/erp/erp-domain-tms/src/main/java/com/aidotnet/erp/tms/application/ShipmentService.java`
  - Keep delivery/cancel transitions strict and record shipping-cost events with richer source dimensions.
- `D:/Project/erp/erp-domain-tms/src/main/java/com/aidotnet/erp/tms/client/FmsClient.java`
  - Add order-/reference-grade cost event fields required by FMS.
- `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/domain/CostEvent.java`
  - Extend the cost-event aggregate with order-scoped trace fields.
- `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/domain/ProfitStatement.java`
  - Keep persisted statement shape aligned with order-level aggregation output.
- `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/application/FinanceService.java`
  - Change profit calculation from SKU-wide grouping to order-scoped grouping and expose order-source queries.
- `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/api/FinanceController.java`
  - Accept and expose the new order-scoped fields and add order-level profit query entrypoints.
- `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/infrastructure/FinanceStore.java`
  - Persist and query cost events and profit statements using the new fields.
- `D:/Project/erp/erp-domain-fms/src/main/resources/mapper/FinanceMapper.xml`
  - Add the new cost-event/profit columns and order-scoped selects.
- `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/application/CostAggregationEngine.java`
  - Make target resolution order-aware and usable from persisted order-scoped cost events.
- `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/application/ProfitCalculationEngine.java`
  - Persist dimension-based profit results driven by saved allocation results.
- `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/infrastructure/FmsExtStore.java`
  - Finish save/list/find methods for aggregation rules, allocation results, profit results, and deviation alerts.
- `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/infrastructure/mapper/FmsExtMapper.java`
  - Keep the mapper interface aligned with implemented XML statements.
- `D:/Project/erp/erp-domain-fms/src/main/resources/mapper/FmsExtMapper.xml`
  - Add missing SQL for rule/result/alert persistence.
- `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/api/FinanceEngineController.java`
  - New minimal controller for rule creation, event aggregation, and profit-result execution/listing.
- `D:/Project/erp/erp-app/src/main/resources/db/migration/V35__fms_order_trace_and_engine_tables.sql`
  - Add order-scoped trace columns and engine tables.

### Existing files to inspect while implementing

- `D:/Project/erp/docs/superpowers/specs/2026-05-05-oms-wms-scm-tms-fms-closed-loop-design.md`
- `D:/Project/erp/erp-app/src/main/resources/db/migration/V14__fms_receivable_payment_cost_profit.sql`
- `D:/Project/erp/erp-app/src/main/resources/db/migration/V21__wms_inventory_transaction_ledger.sql`
- `D:/Project/erp/erp-app/src/main/resources/db/migration/V22__oms_fulfillment_plan.sql`
- `D:/Project/erp/erp-app/src/main/resources/db/migration/V23__oms_shipment_execution_and_platform_sync.sql`
- `D:/Project/erp/erp-app/src/main/resources/db/migration/V25__fms_platform_bill_import.sql`

---

### Task 1: Lock the OMS→WMS→TMS shipment loop with failing integration coverage

**Files:**
- Modify: `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/OmsApiTests.java`
- Modify: `D:/Project/erp/erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java`
- Modify: `D:/Project/erp/erp-domain-oms/src/main/java/com/aidotnet/erp/oms/client/TmsClient.java`
- Modify: `D:/Project/erp/erp-domain-oms/src/main/java/com/aidotnet/erp/oms/client/WmsClient.java`
- Modify: `D:/Project/erp/erp-domain-oms/src/main/java/com/aidotnet/erp/oms/api/OrderFulfillmentController.java`
- Test: `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/OmsApiTests.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void generateFulfillmentPlanShipOrderAndPersistPlatformSyncLogs() throws Exception {
    String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                    .header("X-Tenant-Id", "tenant-loop")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "code":"US-NJ",
                              "name":"New Jersey Warehouse",
                              "countryCode":"US"
                            }
                            """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();

    mockMvc.perform(post("/wms/api/in/v1/inventory/receive")
                    .header("X-Tenant-Id", "tenant-loop")
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

    String carrierResponse = mockMvc.perform(post("/tms/api/in/v1/carriers")
                    .header("X-Tenant-Id", "tenant-loop")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "code":"UPS",
                              "name":"UPS",
                              "countryCode":"US",
                              "apiEnabled":true
                            }
                            """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    String carrierId = objectMapper.readTree(carrierResponse).at("/data/carrierId").asText();

    String orderResponse = mockMvc.perform(post("/api/oms/orders/import")
                    .header("X-Tenant-Id", "tenant-loop")
                    .header("X-Trace-Id", "trace-loop")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "platform":"Amazon",
                              "platformOrderNo":"AMZ-LOOP-100",
                              "buyerName":"Alice",
                              "countryCode":"US",
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

    mockMvc.perform(patch("/api/oms/orders/" + orderId + "/paid")
                    .header("X-Tenant-Id", "tenant-loop"))
            .andExpect(status().isOk());

    mockMvc.perform(post("/oms/api/in/v1/orders/" + orderId + "/fulfillment-plans/generate")
                    .header("X-Tenant-Id", "tenant-loop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PLANNED"))
            .andExpect(jsonPath("$.data.packages.length()").value(1));

    mockMvc.perform(patch("/api/oms/orders/" + orderId + "/ship")
                    .header("X-Tenant-Id", "tenant-loop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SHIPPED"));

    mockMvc.perform(get("/oms/api/in/v1/orders/" + orderId + "/platform-shipments/logs")
                    .header("X-Tenant-Id", "tenant-loop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].status").value("SUCCESS"));

    mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory")
                    .header("X-Tenant-Id", "tenant-loop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].onHand").value(3))
            .andExpect(jsonPath("$.data[0].reserved").value(0));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `rtk mvn -pl erp-app -Dtest=OmsApiTests#generateFulfillmentPlanShipOrderAndPersistPlatformSyncLogs test`
Expected: FAIL because shipment creation/platform sync or warehouse reservation/deduction is incomplete or returns a non-`PLANNED`/non-`SUCCESS` path.

- [ ] **Step 3: Write minimal implementation**

Update the OMS/TMS/WMS loop to reserve stock when building executable packages, create a shipment from package metadata, mark package sync results, and leave one success log per shipped package.

```java
// D:/Project/erp/erp-domain-oms/src/main/java/com/aidotnet/erp/oms/client/TmsClient.java
record CreateShipmentRequest(
        String orderId,
        String warehouseId,
        String carrierId,
        String shippingMethodId,
        String trackingNo,
        String destinationCountry,
        BigDecimal weight,
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        Instant estimatedDelivery
) {}
```

```java
// D:/Project/erp/erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java
private void reservePackageInventory(String tenantId, OrderFulfillmentPackage pkg) {
    for (OrderFulfillmentPackageLine line : pkg.lines()) {
        wmsClient.reserve(new WmsClient.StockRequest(
                pkg.warehouseId(),
                line.sellerSku(),
                line.quantity(),
                "OMS_FULFILLMENT_PACKAGE",
                pkg.packageId(),
                "reserve-for-shipment"));
    }
}

private TmsClient.ShipmentResponse createShipment(SalesOrder order, OrderFulfillmentPackage pkg) {
    return unwrap(tmsClient.createShipment(new TmsClient.CreateShipmentRequest(
            order.orderId(),
            pkg.warehouseId(),
            pkg.carrierId(),
            pkg.serviceLevel(),
            buildTrackingNo(order, pkg),
            pkg.destinationCountry(),
            null,
            null,
            null,
            null,
            null)));
}
```

```java
// D:/Project/erp/erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java
private PlatformShipmentSyncLog successLog(SalesOrder order, OrderFulfillmentPackage pkg) {
    return new PlatformShipmentSyncLog(
            UUID.randomUUID().toString(),
            order.tenantId(),
            order.orderId(),
            pkg.packageId(),
            order.platform(),
            order.platformOrderNo(),
            pkg.trackingNo(),
            PlatformShipmentSyncStatus.SUCCESS,
            1,
            null,
            Instant.now());
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `rtk mvn -pl erp-app -Dtest=OmsApiTests#generateFulfillmentPlanShipOrderAndPersistPlatformSyncLogs test`
Expected: PASS with one shipment sync log, one fulfillment plan, and post-ship inventory reduced from 5 to 3.

- [ ] **Step 5: Commit**

```bash
rtk git add "erp-app/src/test/java/com/aidotnet/erp/app/OmsApiTests.java" "erp-domain-oms/src/main/java/com/aidotnet/erp/oms/application/OrderService.java" "erp-domain-oms/src/main/java/com/aidotnet/erp/oms/client/TmsClient.java" "erp-domain-oms/src/main/java/com/aidotnet/erp/oms/client/WmsClient.java" "erp-domain-oms/src/main/java/com/aidotnet/erp/oms/api/OrderFulfillmentController.java" && rtk git commit -m "$(cat <<'EOF'
feat: close shipment execution loop

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"`
```

### Task 2: Make SCM receipt update WMS ledger and FMS cost trace with failing tests first

**Files:**
- Modify: `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/ScmApiTests.java`
- Modify: `D:/Project/erp/erp-domain-scm/src/main/java/com/aidotnet/erp/scm/application/PurchaseService.java`
- Modify: `D:/Project/erp/erp-domain-scm/src/main/java/com/aidotnet/erp/scm/client/WmsClient.java`
- Modify: `D:/Project/erp/erp-domain-scm/src/main/java/com/aidotnet/erp/scm/client/FmsClient.java`
- Modify: `D:/Project/erp/erp-domain-wms/src/main/java/com/aidotnet/erp/wms/api/InventoryController.java`
- Test: `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/ScmApiTests.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void receivePurchaseOrderUpdatesInventoryTransactionsAndProductCostTrace() throws Exception {
    String warehouseResponse = mockMvc.perform(post("/wms/api/in/v1/warehouses")
                    .header("X-Tenant-Id", "tenant-proc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "code":"CN-SZ",
                              "name":"Shenzhen WH",
                              "countryCode":"CN"
                            }
                            """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    String warehouseId = objectMapper.readTree(warehouseResponse).at("/data/warehouseId").asText();

    String supplierResponse = mockMvc.perform(post("/api/scm/suppliers")
                    .header("X-Tenant-Id", "tenant-proc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "name":"Shenzhen Supplier",
                              "contactName":"Bob",
                              "countryCode":"CN"
                            }
                            """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    String supplierId = objectMapper.readTree(supplierResponse).at("/data/supplierId").asText();

    String poResponse = mockMvc.perform(post("/api/scm/purchase-orders")
                    .header("X-Tenant-Id", "tenant-proc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "supplierId":"%s",
                              "currency":"USD",
                              "lines":[
                                {
                                  "lineId":"L1",
                                  "sellerSku":"SKU-PO-1",
                                  "quantity":10,
                                  "unitCost":12.50
                                }
                              ]
                            }
                            """.formatted(supplierId)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    String poId = objectMapper.readTree(poResponse).at("/data/poId").asText();

    mockMvc.perform(patch("/api/scm/purchase-orders/" + poId + "/submit")
                    .header("X-Tenant-Id", "tenant-proc"))
            .andExpect(status().isOk());
    mockMvc.perform(patch("/api/scm/purchase-orders/" + poId + "/approve")
                    .header("X-Tenant-Id", "tenant-proc"))
            .andExpect(status().isOk());

    mockMvc.perform(patch("/api/scm/purchase-orders/" + poId + "/receive")
                    .header("X-Tenant-Id", "tenant-proc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "warehouseId":"%s",
                              "receipts":[
                                {
                                  "lineId":"L1",
                                  "quantity":4
                                }
                              ]
                            }
                            """.formatted(warehouseId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PARTIALLY_RECEIVED"));

    mockMvc.perform(get("/wms/api/in/v1/warehouses/" + warehouseId + "/inventory-transactions")
                    .header("X-Tenant-Id", "tenant-proc")
                    .param("sellerSku", "SKU-PO-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].referenceType").value("SCM_PURCHASE_ORDER"))
            .andExpect(jsonPath("$.data[0].referenceId").value(poId));

    mockMvc.perform(get("/fms/api/in/v1/cost-events/by-source")
                    .header("X-Tenant-Id", "tenant-proc")
                    .param("sourceType", "SCM_PURCHASE_ORDER")
                    .param("sourceId", poId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].costType").value("PRODUCT_COST"))
            .andExpect(jsonPath("$.data[0].sellerSku").value("SKU-PO-1"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `rtk mvn -pl erp-app -Dtest=ScmApiTests#receivePurchaseOrderUpdatesInventoryTransactionsAndProductCostTrace test`
Expected: FAIL because the current SCM receipt path does not preserve reference metadata strongly enough through WMS/FMS.

- [ ] **Step 3: Write minimal implementation**

Pass the PO reference cleanly into WMS and FMS from `PurchaseService.receive(...)`.

```java
// D:/Project/erp/erp-domain-scm/src/main/java/com/aidotnet/erp/scm/client/WmsClient.java
record StockRequest(
        String warehouseId,
        String sellerSku,
        int quantity,
        String referenceType,
        String referenceId,
        String remark
) {}
```

```java
// D:/Project/erp/erp-domain-scm/src/main/java/com/aidotnet/erp/scm/application/PurchaseService.java
private void receiveInventory(PurchaseOrder po, String warehouseId, PurchaseOrderLine line, int receivedQuantity) {
    wmsClient.receive(new com.aidotnet.erp.scm.client.WmsClient.StockRequest(
            warehouseId,
            line.sellerSku(),
            receivedQuantity,
            "SCM_PURCHASE_ORDER",
            po.poId(),
            "purchase-receive"));
}

private void recordProductCost(PurchaseOrder po, PurchaseOrderLine line, int receivedQuantity) {
    fmsClient.recordCostEvent(new FmsClient.RecordCostEventRequest(
            "PRODUCT_COST",
            "SCM_PURCHASE_ORDER",
            po.poId(),
            null,
            line.sellerSku(),
            null,
            po.currency(),
            line.unitCost().multiply(BigDecimal.valueOf(receivedQuantity)),
            Instant.now()));
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `rtk mvn -pl erp-app -Dtest=ScmApiTests#receivePurchaseOrderUpdatesInventoryTransactionsAndProductCostTrace test`
Expected: PASS with one WMS `SCM_PURCHASE_ORDER` transaction and one FMS `PRODUCT_COST` event linked to the PO.

- [ ] **Step 5: Commit**

```bash
rtk git add "erp-app/src/test/java/com/aidotnet/erp/app/ScmApiTests.java" "erp-domain-scm/src/main/java/com/aidotnet/erp/scm/application/PurchaseService.java" "erp-domain-scm/src/main/java/com/aidotnet/erp/scm/client/WmsClient.java" "erp-domain-scm/src/main/java/com/aidotnet/erp/scm/client/FmsClient.java" "erp-domain-wms/src/main/java/com/aidotnet/erp/wms/api/InventoryController.java" && rtk git commit -m "$(cat <<'EOF'
feat: trace purchase receipts through inventory and cost events

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"`
```

### Task 3: Move FMS profit calculation from SKU-wide grouping to order-scoped grouping

**Files:**
- Modify: `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/FmsApiTests.java`
- Modify: `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/domain/CostEvent.java`
- Modify: `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/domain/ProfitStatement.java`
- Modify: `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/application/FinanceService.java`
- Modify: `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/api/FinanceController.java`
- Modify: `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/infrastructure/FinanceStore.java`
- Modify: `D:/Project/erp/erp-domain-fms/src/main/resources/mapper/FinanceMapper.xml`
- Create: `D:/Project/erp/erp-app/src/main/resources/db/migration/V35__fms_order_trace_and_engine_tables.sql`
- Test: `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/FmsApiTests.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void calculateProfitByOrderWithoutCrossOrderCostPollution() throws Exception {
    mockMvc.perform(post("/fms/api/in/v1/cost-events")
                    .header("X-Tenant-Id", "tenant-profit-order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "costType":"PRODUCT_COST",
                              "sourceType":"OMS_ORDER",
                              "sourceId":"ORD-100",
                              "orderId":"ORD-100",
                              "sellerSku":"SKU-100",
                              "currency":"USD",
                              "amount":30.00
                            }
                            """))
            .andExpect(status().isOk());

    mockMvc.perform(post("/fms/api/in/v1/cost-events")
                    .header("X-Tenant-Id", "tenant-profit-order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "costType":"PRODUCT_COST",
                              "sourceType":"OMS_ORDER",
                              "sourceId":"ORD-200",
                              "orderId":"ORD-200",
                              "sellerSku":"SKU-100",
                              "currency":"USD",
                              "amount":70.00
                            }
                            """))
            .andExpect(status().isOk());

    mockMvc.perform(post("/fms/api/in/v1/profit-statements")
                    .header("X-Tenant-Id", "tenant-profit-order")
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
            .andExpect(jsonPath("$.data.totalCost").value(30.0))
            .andExpect(jsonPath("$.data.grossProfit").value(50.0));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `rtk mvn -pl erp-app -Dtest=FmsApiTests#calculateProfitByOrderWithoutCrossOrderCostPollution test`
Expected: FAIL because `FinanceService.calculateProfit(...)` currently sums all cost events by SKU instead of by order.

- [ ] **Step 3: Write minimal implementation**

Add order-scoped fields to cost events, persist them, and query profit costs by order first.

```java
// D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/domain/CostEvent.java
public record CostEvent(
        String costEventId,
        String tenantId,
        String costType,
        String sourceType,
        String sourceId,
        String orderId,
        String sellerSku,
        String marketplaceId,
        String currency,
        BigDecimal amount,
        Instant occurredAt,
        Instant createdAt
) {}
```

```java
// D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/application/FinanceService.java
public ProfitStatement calculateProfit(String tenantId, CalculateProfitCommand command) {
    List<CostEvent> costs = command.orderId() != null
            ? financeStore.listCostEventsByOrderId(tenantId, command.orderId())
            : financeStore.listCostEventsBySku(tenantId, command.sellerSku());
    if (command.orderId() != null) {
        costs = costs.stream()
                .filter(cost -> command.orderId().equals(cost.orderId()))
                .toList();
    }
    BigDecimal productCost = sumByType(costs, CostEvent.CostType.PRODUCT_COST);
    BigDecimal shippingCost = sumByType(costs, CostEvent.CostType.SHIPPING_COST);
    BigDecimal commission = sumByType(costs, CostEvent.CostType.COMMISSION);
    BigDecimal totalCost = productCost.add(shippingCost).add(commission)
            .add(sumByType(costs, CostEvent.CostType.FBA_FEE))
            .add(sumByType(costs, CostEvent.CostType.ADVERTISING))
            .add(sumByType(costs, CostEvent.CostType.OTHER))
            .add(sumByType(costs, CostEvent.CostType.RETURN_COST))
            .add(sumByType(costs, CostEvent.CostType.STORAGE_FEE));
    BigDecimal grossProfit = command.revenue().subtract(totalCost);
    BigDecimal grossMargin = command.revenue().compareTo(BigDecimal.ZERO) > 0
            ? grossProfit.divide(command.revenue(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
    return financeStore.saveProfitStatement(new ProfitStatement(
            UUID.randomUUID().toString(),
            tenantId,
            command.sellerSku(),
            command.marketplaceId(),
            command.orderId(),
            command.revenue(),
            productCost,
            shippingCost,
            sumByType(costs, CostEvent.CostType.FBA_FEE),
            commission,
            sumByType(costs, CostEvent.CostType.ADVERTISING),
            sumByType(costs, CostEvent.CostType.OTHER)
                    .add(sumByType(costs, CostEvent.CostType.RETURN_COST))
                    .add(sumByType(costs, CostEvent.CostType.STORAGE_FEE)),
            totalCost,
            grossProfit,
            grossMargin,
            command.currency(),
            Instant.now()));
}
```

```sql
-- D:/Project/erp/erp-app/src/main/resources/db/migration/V35__fms_order_trace_and_engine_tables.sql
ALTER TABLE fms_cost_event ADD COLUMN IF NOT EXISTS order_id VARCHAR(64);
CREATE INDEX IF NOT EXISTS idx_fms_cost_order ON fms_cost_event(tenant_id, order_id);
ALTER TABLE fms_profit_statement ADD COLUMN IF NOT EXISTS source_summary CLOB;
```

- [ ] **Step 4: Run test to verify it passes**

Run: `rtk mvn -pl erp-app -Dtest=FmsApiTests#calculateProfitByOrderWithoutCrossOrderCostPollution test`
Expected: PASS with `ORD-100` profit using only the `30.00` cost event.

- [ ] **Step 5: Commit**

```bash
rtk git add "erp-app/src/test/java/com/aidotnet/erp/app/FmsApiTests.java" "erp-domain-fms/src/main/java/com/aidotnet/erp/fms/domain/CostEvent.java" "erp-domain-fms/src/main/java/com/aidotnet/erp/fms/domain/ProfitStatement.java" "erp-domain-fms/src/main/java/com/aidotnet/erp/fms/application/FinanceService.java" "erp-domain-fms/src/main/java/com/aidotnet/erp/fms/api/FinanceController.java" "erp-domain-fms/src/main/java/com/aidotnet/erp/fms/infrastructure/FinanceStore.java" "erp-domain-fms/src/main/resources/mapper/FinanceMapper.xml" "erp-app/src/main/resources/db/migration/V35__fms_order_trace_and_engine_tables.sql" && rtk git commit -m "$(cat <<'EOF'
fix: calculate profit with order-scoped cost tracing

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"`
```

### Task 4: Finish the smallest usable FMS aggregation/profit engine persistence and API

**Files:**
- Modify: `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/FmsApiTests.java`
- Modify: `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/application/CostAggregationEngine.java`
- Modify: `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/application/ProfitCalculationEngine.java`
- Modify: `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/infrastructure/FmsExtStore.java`
- Modify: `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/infrastructure/mapper/FmsExtMapper.java`
- Modify: `D:/Project/erp/erp-domain-fms/src/main/resources/mapper/FmsExtMapper.xml`
- Create: `D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/api/FinanceEngineController.java`
- Modify: `D:/Project/erp/erp-app/src/main/resources/db/migration/V35__fms_order_trace_and_engine_tables.sql`
- Test: `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/FmsApiTests.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void createAggregationRuleAggregateOrderCostAndPersistProfitResult() throws Exception {
    mockMvc.perform(post("/fms/api/in/v1/cost-events")
                    .header("X-Tenant-Id", "tenant-engine")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "costType":"SHIPPING_COST",
                              "sourceType":"TMS_SHIPMENT",
                              "sourceId":"SHIP-100",
                              "orderId":"ORD-500",
                              "sellerSku":"SKU-500",
                              "currency":"USD",
                              "amount":12.00
                            }
                            """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

    String ruleResponse = mockMvc.perform(post("/fms/api/in/v1/engine/cost-rules")
                    .header("X-Tenant-Id", "tenant-engine")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "ruleName":"Order shipping cost rule",
                              "costSource":"SHIPPING_COST",
                              "costCategory":"LOGISTICS",
                              "allocationMethod":"DIRECT",
                              "allocationBasis":"SOURCE",
                              "targetDimension":"ORDER",
                              "priority":100
                            }
                            """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    String ruleId = objectMapper.readTree(ruleResponse).at("/data/ruleId").asText();

    String eventResponse = mockMvc.perform(get("/fms/api/in/v1/cost-events/by-source")
                    .header("X-Tenant-Id", "tenant-engine")
                    .param("sourceType", "TMS_SHIPMENT")
                    .param("sourceId", "SHIP-100"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    String costEventId = objectMapper.readTree(eventResponse).at("/data/0/costEventId").asText();

    mockMvc.perform(post("/fms/api/in/v1/engine/cost-events/" + costEventId + "/aggregate")
                    .header("X-Tenant-Id", "tenant-engine"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].ruleId").value(ruleId))
            .andExpect(jsonPath("$.data[0].targetDimension").value("ORDER"))
            .andExpect(jsonPath("$.data[0].targetId").value("ORD-500"));

    mockMvc.perform(post("/fms/api/in/v1/engine/profit-results")
                    .header("X-Tenant-Id", "tenant-engine")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "dimensionType":"ORDER",
                              "dimensionId":"ORD-500",
                              "sellerSku":"SKU-500",
                              "orderId":"ORD-500",
                              "marketplaceId":"AMAZON",
                              "revenue":50.00,
                              "currency":"USD"
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalCost").value(12.0))
            .andExpect(jsonPath("$.data.grossProfit").value(38.0));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `rtk mvn -pl erp-app -Dtest=FmsApiTests#createAggregationRuleAggregateOrderCostAndPersistProfitResult test`
Expected: FAIL because the FMS engine tables/XML/controller are not fully implemented.

- [ ] **Step 3: Write minimal implementation**

Add the missing engine tables, XML statements, and a small controller that exposes only the paths the test needs.

```sql
-- D:/Project/erp/erp-app/src/main/resources/db/migration/V35__fms_order_trace_and_engine_tables.sql
CREATE TABLE IF NOT EXISTS fms_cost_aggregation_rule (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    rule_id VARCHAR(64) NOT NULL UNIQUE,
    tenant_id VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    cost_source VARCHAR(64),
    cost_category VARCHAR(64),
    allocation_method VARCHAR(32) NOT NULL,
    allocation_basis VARCHAR(64),
    target_dimension VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    priority INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS fms_cost_allocation_result (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    result_id VARCHAR(64) NOT NULL UNIQUE,
    tenant_id VARCHAR(64) NOT NULL,
    rule_id VARCHAR(64) NOT NULL,
    cost_event_id VARCHAR(64) NOT NULL,
    target_dimension VARCHAR(32) NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    exchange_rate DECIMAL(18, 6) NOT NULL,
    amount_in_base_currency DECIMAL(18, 2) NOT NULL,
    dimensions CLOB,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS fms_profit_result (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    result_id VARCHAR(64) NOT NULL UNIQUE,
    tenant_id VARCHAR(64) NOT NULL,
    dimension_type VARCHAR(32) NOT NULL,
    dimension_id VARCHAR(64) NOT NULL,
    seller_sku VARCHAR(128),
    order_id VARCHAR(64),
    store_id VARCHAR(64),
    marketplace_id VARCHAR(64),
    revenue DECIMAL(18, 2) NOT NULL,
    total_cost DECIMAL(18, 2) NOT NULL,
    gross_profit DECIMAL(18, 2) NOT NULL,
    gross_margin DECIMAL(18, 4) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    cost_details CLOB,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS fms_profit_deviation_alert (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    alert_id VARCHAR(64) NOT NULL UNIQUE,
    tenant_id VARCHAR(64) NOT NULL,
    dimension_type VARCHAR(32) NOT NULL,
    dimension_id VARCHAR(64) NOT NULL,
    seller_sku VARCHAR(128),
    expected_margin DECIMAL(18, 4) NOT NULL,
    actual_margin DECIMAL(18, 4) NOT NULL,
    deviation DECIMAL(18, 4) NOT NULL,
    deviation_threshold DECIMAL(18, 4) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP
);
```

```java
// D:/Project/erp/erp-domain-fms/src/main/java/com/aidotnet/erp/fms/api/FinanceEngineController.java
@RestController
@RequestMapping("/fms/api/in/v1/engine")
public class FinanceEngineController {

    private final CostAggregationEngine costAggregationEngine;
    private final ProfitCalculationEngine profitCalculationEngine;

    public FinanceEngineController(CostAggregationEngine costAggregationEngine,
                                   ProfitCalculationEngine profitCalculationEngine) {
        this.costAggregationEngine = costAggregationEngine;
        this.profitCalculationEngine = profitCalculationEngine;
    }

    @PostMapping("/cost-rules")
    public Result<CostAggregationRule> createRule(@Valid @RequestBody CreateRuleRequest request) {
        return Result.ok(costAggregationEngine.createRule(currentTenant(), new CreateAggregationRuleCommand(
                request.ruleName(), request.costSource(), request.costCategory(), request.allocationMethod(),
                request.allocationBasis(), request.targetDimension(), request.priority())));
    }

    @PostMapping("/cost-events/{costEventId}/aggregate")
    public Result<List<CostAllocationResult>> aggregate(@PathVariable String costEventId) {
        return Result.ok(costAggregationEngine.aggregateCostEvent(currentTenant(), costEventId));
    }

    @PostMapping("/profit-results")
    public Result<ProfitResult> calculate(@Valid @RequestBody CalculateProfitRequest request) {
        return Result.ok(profitCalculationEngine.calculateProfit(currentTenant(), new CalculateProfitCommand(
                request.dimensionType(), request.dimensionId(), request.sellerSku(), request.orderId(),
                request.storeId(), request.marketplaceId(), request.revenue(), request.currency())));
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `rtk mvn -pl erp-app -Dtest=FmsApiTests#createAggregationRuleAggregateOrderCostAndPersistProfitResult test`
Expected: PASS with one persisted order-target allocation result and one persisted order profit result.

- [ ] **Step 5: Commit**

```bash
rtk git add "erp-app/src/test/java/com/aidotnet/erp/app/FmsApiTests.java" "erp-domain-fms/src/main/java/com/aidotnet/erp/fms/application/CostAggregationEngine.java" "erp-domain-fms/src/main/java/com/aidotnet/erp/fms/application/ProfitCalculationEngine.java" "erp-domain-fms/src/main/java/com/aidotnet/erp/fms/infrastructure/FmsExtStore.java" "erp-domain-fms/src/main/java/com/aidotnet/erp/fms/infrastructure/mapper/FmsExtMapper.java" "erp-domain-fms/src/main/resources/mapper/FmsExtMapper.xml" "erp-domain-fms/src/main/java/com/aidotnet/erp/fms/api/FinanceEngineController.java" "erp-app/src/main/resources/db/migration/V35__fms_order_trace_and_engine_tables.sql" && rtk git commit -m "$(cat <<'EOF'
feat: persist minimal aggregation and profit engine results

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"`
```

### Task 5: Run the batch verification mapped to the acceptance slice

**Files:**
- Modify: `D:/Project/erp/docs/acceptance/p0-baseline-self-check.md`
- Test: `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/OmsApiTests.java`
- Test: `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/ScmApiTests.java`
- Test: `D:/Project/erp/erp-app/src/test/java/com/aidotnet/erp/app/FmsApiTests.java`

- [ ] **Step 1: Write the acceptance evidence note**

Append a new section to `D:/Project/erp/docs/acceptance/p0-baseline-self-check.md` capturing this slice:

```markdown
## OMS-WMS-SCM-TMS-FMS 最小闭环补齐

- 订单履约：履约计划、运单创建、平台发货回传日志已验证。
- 采购收货：收货后库存流水与产品成本事件已验证。
- 财务利润：成本事件已支持订单维度归集，利润不再按同 SKU 跨单串算。
- 引擎最小骨架：成本归集规则、分摊结果、利润结果已可持久化并通过接口验证。
```

- [ ] **Step 2: Run the focused acceptance tests**

Run: `rtk mvn -pl erp-app -Dtest=OmsApiTests,ScmApiTests,FmsApiTests test`
Expected: PASS with all shipment, receipt, cost, and profit loop tests green.

- [ ] **Step 3: Run the full app-module test suite**

Run: `rtk mvn -pl erp-app test`
Expected: PASS with `BUILD SUCCESS` for the application integration suite.

- [ ] **Step 4: Run the root verification build**

Run: `rtk mvn test`
Expected: PASS with Flyway migrations applied successfully and no regression failures in other modules.

- [ ] **Step 5: Commit**

```bash
rtk git add "docs/acceptance/p0-baseline-self-check.md" && rtk git commit -m "$(cat <<'EOF'
docs: record closed-loop acceptance evidence

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"`
```

---

## Self-Review

### Spec coverage

- OMS order → fulfillment plan → shipment sync: covered by Task 1.
- WMS reserve/release/deduct/inbound and transaction trace: covered by Tasks 1 and 2.
- SCM approval/receipt → WMS/FMS linkage: covered by Task 2.
- TMS shipment/tracking/shipping cost entry: covered by Tasks 1 and 4.
- FMS cost events, platform bill/profit correctness, and minimal aggregation/profit engine persistence: covered by Tasks 3 and 4.
- Acceptance evidence and fresh verification commands: covered by Task 5.

### Placeholder scan

- No `TBD`, `TODO`, or deferred-code placeholders remain in this plan.
- Every task includes a test, a fail command, an implementation shape, a pass command, and a commit command.

### Type consistency

- `orderId` is the primary new trace field carried from SCM/TMS/OMS into FMS cost events.
- Engine APIs use `dimensionType`/`dimensionId`, matching `ProfitCalculationEngine.CalculateProfitCommand`.
- Shipment loop uses the existing `/oms/api/in/v1/orders/{orderId}/fulfillment-plans/*` and `/tms/api/in/v1/shipments/*` paths already present in the codebase.

package com.aidotnet.erp.oms.application;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.common.event.StandardDomainEvent;
import com.aidotnet.erp.oms.client.FmsClient;
import com.aidotnet.erp.oms.client.TmsClient;
import com.aidotnet.erp.oms.client.WmsClient;
import com.aidotnet.erp.oms.domain.BuyerBlacklistEntry;
import com.aidotnet.erp.oms.domain.OrderLine;
import com.aidotnet.erp.oms.domain.OrderFulfillmentPlan;
import com.aidotnet.erp.oms.domain.FulfillmentPackageStatus;
import com.aidotnet.erp.oms.domain.PlatformShipmentSyncStatus;
import com.aidotnet.erp.oms.domain.OrderFulfillmentPackage;
import com.aidotnet.erp.oms.domain.OrderFulfillmentPackageLine;
import com.aidotnet.erp.oms.domain.OrderRefund;
import com.aidotnet.erp.oms.domain.OrderRiskCheck;
import com.aidotnet.erp.oms.domain.OrderStatus;
import com.aidotnet.erp.oms.domain.PmsRiskAlert;
import com.aidotnet.erp.oms.domain.PmsRiskAlertReviewLog;
import com.aidotnet.erp.oms.domain.SalesOrder;
import com.aidotnet.erp.oms.infrastructure.FulfillmentPlanStore;
import com.aidotnet.erp.oms.infrastructure.OrderStore;
import com.aidotnet.erp.oms.infrastructure.PlatformShipmentSyncLogStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("OMS订单服务测试")
class OrderServiceTest {

    private OrderStore orderStore;
    private DomainEventPublisher eventPublisher;
    private WmsClient wmsClient;
    private FmsClient fmsClient;
    private TmsClient tmsClient;
    private FulfillmentPlanStore fulfillmentPlanStore;
    private PlatformShipmentSyncLogStore platformShipmentSyncLogStore;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderStore = mock(OrderStore.class);
        eventPublisher = mock(DomainEventPublisher.class);
        wmsClient = mock(WmsClient.class);
        fmsClient = mock(FmsClient.class);
        tmsClient = mock(TmsClient.class);
        fulfillmentPlanStore = mock(FulfillmentPlanStore.class);
        platformShipmentSyncLogStore = mock(PlatformShipmentSyncLogStore.class);
        orderService = new OrderService(orderStore, fulfillmentPlanStore, platformShipmentSyncLogStore, eventPublisher, wmsClient, fmsClient, tmsClient);
    }

    @Test
    @DisplayName("导入订单应返回CREATED状态")
    void importOrder() {
        OrderLine line = new OrderLine("L1", "P-001", "SKU-001", "Product A", 3, BigDecimal.valueOf(29.99), BigDecimal.valueOf(89.97), BigDecimal.ZERO, BigDecimal.valueOf(20));
        OrderService.ImportOrderCommand cmd = new OrderService.ImportOrderCommand(
                "AMAZON", "PO-001", "John", "US", "123 Main Street", "USD", List.of(line));
        when(orderStore.findByPlatformOrderNo("T1", "AMAZON", "PO-001")).thenReturn(Optional.empty());
        when(orderStore.listRecentOrdersByBuyer(any(), any(), any(), any())).thenReturn(List.of());
        when(orderStore.listBuyerBlacklist(any())).thenReturn(List.<BuyerBlacklistEntry>of());
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wmsClient.checkAvailability(any())).thenReturn(Result.ok(
                new WmsClient.InventoryAvailabilityResponse("SKU-001", 100, 10, 90)));
        when(fmsClient.createReceivable(any())).thenReturn(Result.ok(
                new FmsClient.ReceivableResponse("R1", "OMS_ORDER", "ORD-1", "CREATED")));

        SalesOrder result = orderService.importOrder("T1", cmd);
        assertNotNull(result);
        assertEquals(OrderStatus.CREATED, result.status());
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("重复导入订单应抛出异常")
    void importDuplicateOrder() {
        OrderLine line = new OrderLine("L1", "P-001", "SKU-001", "Product A", 3, BigDecimal.valueOf(29.99), BigDecimal.valueOf(89.97), BigDecimal.ZERO, BigDecimal.valueOf(20));
        OrderService.ImportOrderCommand cmd = new OrderService.ImportOrderCommand(
                "AMAZON", "PO-001", "John", "US", "123 Main Street", "USD", List.of(line));
        SalesOrder existing = new SalesOrder(
                "ORD-1", "T1", null, null, "AMAZON", null, "PO-001", "John", null, "US",
                "123 Main Street", "USD", BigDecimal.valueOf(89.97), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.CREATED, "UNPAID", "UNFULFILLED", null, null, List.of(line), List.of(), Instant.now(), Instant.now(), Instant.now());
        when(orderStore.findByPlatformOrderNo("T1", "AMAZON", "PO-001")).thenReturn(Optional.of(existing));

        assertThrows(com.aidotnet.erp.common.exception.BizException.class,
                () -> orderService.importOrder("T1", cmd));
    }

    @Test
    @DisplayName("确认付款应流转为PAID状态")
    void markPaid() {
        SalesOrder order = new SalesOrder(
                "ORD-1", "T1", null, null, "AMAZON", null, "PO-001", "John", null, "US",
                "123 Main Street", "USD", BigDecimal.valueOf(89.97), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.CREATED, "UNPAID", "UNFULFILLED", null, null, List.of(), List.of(), Instant.now(), Instant.now(), Instant.now());
        when(orderStore.find("T1", "ORD-1")).thenReturn(Optional.of(order));
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fmsClient.createReceivable(any())).thenReturn(Result.ok(
                new FmsClient.ReceivableResponse("R1", "OMS_ORDER", "ORD-1", "CREATED")));

        SalesOrder result = orderService.markPaid("T1", "ORD-1");
        assertEquals(OrderStatus.PAID, result.status());
    }

    @Test
    @DisplayName("黑名单买家订单应进入待审")
    void importOrderShouldRequireReviewWhenBuyerBlacklisted() {
        OrderLine line = new OrderLine("L1", "P-001", "SKU-001", "Product A", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50), BigDecimal.ZERO, BigDecimal.valueOf(30));
        OrderService.ImportOrderCommand cmd = new OrderService.ImportOrderCommand(
                "AMAZON", "PO-BL-001", "John", "US", "123 Main Street", "USD", List.of(line), "API", "STORE-1", "US", "{\"po\":\"PO-BL-001\"}");
        when(orderStore.findByPlatformOrderNo("T1", "AMAZON", "PO-BL-001")).thenReturn(Optional.empty());
        when(orderStore.listRecentOrdersByBuyer(any(), any(), any(), any())).thenReturn(List.of());
        when(orderStore.listBuyerBlacklist("T1")).thenReturn(List.of(
                new BuyerBlacklistEntry("B1", "T1", "John", "risk", Instant.now())));
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wmsClient.checkAvailability(any())).thenReturn(Result.ok(
                new WmsClient.InventoryAvailabilityResponse("SKU-001", 100, 0, 100)));

        SalesOrder result = orderService.importOrder("T1", cmd);

        assertEquals(OrderStatus.REVIEW_REQUIRED, result.status());
    }

    @Test
    @DisplayName("低利润订单应进入待审")
    void importOrderShouldRequireReviewWhenProfitTooLow() {
        OrderLine line = new OrderLine("L1", "P-001", "SKU-001", "Product A", 1, BigDecimal.valueOf(100), BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(98));
        OrderService.ImportOrderCommand cmd = new OrderService.ImportOrderCommand(
                "AMAZON", "PO-LP-001", "John", "US", "123 Main Street", "USD", List.of(line), "API", "STORE-1", "US", "{\"po\":\"PO-LP-001\"}");
        when(orderStore.findByPlatformOrderNo("T1", "AMAZON", "PO-LP-001")).thenReturn(Optional.empty());
        when(orderStore.listRecentOrdersByBuyer(any(), any(), any(), any())).thenReturn(List.of());
        when(orderStore.listBuyerBlacklist(any())).thenReturn(List.<BuyerBlacklistEntry>of());
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wmsClient.checkAvailability(any())).thenReturn(Result.ok(
                new WmsClient.InventoryAvailabilityResponse("SKU-001", 100, 0, 100)));

        SalesOrder result = orderService.importOrder("T1", cmd);

        assertEquals(OrderStatus.REVIEW_REQUIRED, result.status());
    }

    @Test
    @DisplayName("异常地址订单应进入待审")
    void importOrderShouldRequireReviewWhenAddressAbnormal() {
        OrderLine line = new OrderLine("L1", "P-001", "SKU-001", "Product A", 1, BigDecimal.valueOf(50), BigDecimal.valueOf(50), BigDecimal.ZERO, BigDecimal.valueOf(20));
        OrderService.ImportOrderCommand cmd = new OrderService.ImportOrderCommand(
                "AMAZON", "PO-ADDR-001", "John", "US", "PO BOX 1", "USD", List.of(line), "API", "STORE-1", "US", "{\"po\":\"PO-ADDR-001\"}");
        when(orderStore.findByPlatformOrderNo("T1", "AMAZON", "PO-ADDR-001")).thenReturn(Optional.empty());
        when(orderStore.listRecentOrdersByBuyer(any(), any(), any(), any())).thenReturn(List.of());
        when(orderStore.listBuyerBlacklist(any())).thenReturn(List.<BuyerBlacklistEntry>of());
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wmsClient.checkAvailability(any())).thenReturn(Result.ok(
                new WmsClient.InventoryAvailabilityResponse("SKU-001", 100, 0, 100)));

        SalesOrder result = orderService.importOrder("T1", cmd);

        assertEquals(OrderStatus.REVIEW_REQUIRED, result.status());
    }

    @Test
    @DisplayName("库存不足订单应保留为已创建并记录补货风险")
    void importOrderShouldStayCreatedWhenInventoryIsShort() {
        OrderLine line = new OrderLine("L1", "P-001", "SKU-001", "Product A", 10, BigDecimal.valueOf(50), BigDecimal.valueOf(500), BigDecimal.ZERO, BigDecimal.valueOf(20));
        OrderService.ImportOrderCommand cmd = new OrderService.ImportOrderCommand(
                "AMAZON", "PO-INV-001", "John", "US", "123 Main Street", "USD", List.of(line), "API", "STORE-1", "US", "{\"po\":\"PO-INV-001\"}");
        when(orderStore.findByPlatformOrderNo("T1", "AMAZON", "PO-INV-001")).thenReturn(Optional.empty());
        when(orderStore.listRecentOrdersByBuyer(any(), any(), any(), any())).thenReturn(List.of());
        when(orderStore.listBuyerBlacklist(any())).thenReturn(List.<BuyerBlacklistEntry>of());
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderStore.saveRiskCheck(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wmsClient.checkAvailability("SKU-001")).thenReturn(Result.ok(
                new WmsClient.InventoryAvailabilityResponse("SKU-001", 4, 0, 4)));

        SalesOrder result = orderService.importOrder("T1", cmd);

        assertEquals(OrderStatus.CREATED, result.status());
        ArgumentCaptor<OrderRiskCheck> riskCaptor = ArgumentCaptor.forClass(OrderRiskCheck.class);
        verify(orderStore).saveRiskCheck(riskCaptor.capture());
        assertEquals(OrderRiskCheck.RiskLevel.HIGH, riskCaptor.getValue().riskLevel());
        assertEquals("INVENTORY_SHORTAGE", riskCaptor.getValue().riskType());
    }

    @Test
    @DisplayName("履约计划应按仓库拆包并生成物流推荐")
    void generateFulfillmentPlan() {
        OrderLine line1 = new OrderLine("L1", "P-001", "SKU-001", "Product A", 2, BigDecimal.valueOf(20), BigDecimal.valueOf(40), BigDecimal.ZERO, BigDecimal.valueOf(12));
        OrderLine line2 = new OrderLine("L2", "P-002", "SKU-002", "Product B", 1, BigDecimal.valueOf(15), BigDecimal.valueOf(15), BigDecimal.ZERO, BigDecimal.valueOf(8));
        SalesOrder order = new SalesOrder(
                "ORD-2", "T1", null, null, "AMAZON", null, "PO-002", "Jane", null, "US",
                "8 Main Street", "USD", BigDecimal.valueOf(55), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.PAID, "PAID", "UNFULFILLED", null, null, List.of(line1, line2), List.of(), Instant.now(), Instant.now(), Instant.now());
        when(orderStore.find("T1", "ORD-2")).thenReturn(Optional.of(order));
        when(fulfillmentPlanStore.findByOrderId("T1", "ORD-2")).thenReturn(Optional.empty());
        when(fulfillmentPlanStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wmsClient.listWarehouses()).thenReturn(Result.ok(List.of(
                new WmsClient.WarehouseResponse("W1", "T1", "US-WH", "US Warehouse", "US"),
                new WmsClient.WarehouseResponse("W2", "T1", "CN-WH", "CN Warehouse", "CN")
        )));
        when(wmsClient.listBalances("W1")).thenReturn(Result.ok(List.of(
                new WmsClient.InventoryBalanceResponse("T1", "W1", "SKU-001", 2, 0)
        )));
        when(wmsClient.listBalances("W2")).thenReturn(Result.ok(List.of(
                new WmsClient.InventoryBalanceResponse("T1", "W2", "SKU-002", 1, 0)
        )));
        when(tmsClient.recommendCarriers(any())).thenReturn(Result.ok(List.of(
                new TmsClient.CarrierRecommendationResponse("C1", "UPS_EXPRESS", "UPS Express", "EXPRESS",
                        BigDecimal.valueOf(25), 2, 95)
        )));

        OrderFulfillmentPlan plan = orderService.generateFulfillmentPlan("T1", "ORD-2");

        assertEquals(2, plan.packages().size());
        assertTrue(plan.splitShipment());
        assertEquals(OrderStatus.PAID, order.status());
    }

    @Test
    @DisplayName("已付款订单发货时应执行库存扣减并生成追踪号")
    void shipPaidOrder() {
        OrderLine line = new OrderLine("L1", "P-001", "SKU-001", "Product A", 2, BigDecimal.valueOf(20), BigDecimal.valueOf(40), BigDecimal.ZERO, BigDecimal.valueOf(12));
        SalesOrder order = new SalesOrder(
                "ORD-3", "T1", null, null, "AMAZON", null, "PO-003", "Jane", null, "US",
                "18 Main Street", "USD", BigDecimal.valueOf(40), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.PAID, "PAID", "UNFULFILLED", null, null, List.of(line), List.of(), Instant.now(), Instant.now(), Instant.now());
        OrderFulfillmentPackage fulfillmentPackage = new OrderFulfillmentPackage(
                "PKG-1",
                "PLAN-1",
                "W1",
                "US-WH",
                "C1",
                "UPS_EXPRESS",
                "UPS Express",
                "US",
                "EXPRESS",
                FulfillmentPackageStatus.READY,
                2,
                BigDecimal.valueOf(40),
                BigDecimal.valueOf(18),
                2,
                null,
                null,
                null,
                null,
                PlatformShipmentSyncStatus.NOT_SYNCED,
                0,
                null,
                null,
                List.of(new OrderFulfillmentPackageLine("PL-1", "L1", "SKU-001", "Product A", 2,
                        BigDecimal.valueOf(20), BigDecimal.valueOf(40))));
        OrderFulfillmentPlan plan = new OrderFulfillmentPlan("PLAN-1", "T1", "ORD-3",
                com.aidotnet.erp.oms.domain.FulfillmentPlanStatus.PLANNED, false, false,
                BigDecimal.valueOf(18), Instant.now(), Instant.now(), List.of(fulfillmentPackage));
        when(orderStore.find("T1", "ORD-3")).thenReturn(Optional.of(order));
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fulfillmentPlanStore.findByOrderId("T1", "ORD-3")).thenReturn(Optional.of(plan));
        when(fulfillmentPlanStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wmsClient.checkAvailability(any())).thenReturn(Result.ok(
                new WmsClient.InventoryAvailabilityResponse("SKU-001", 100, 0, 100)));
        when(wmsClient.reserve(any())).thenReturn(Result.ok(
                new WmsClient.InventoryBalanceResponse("T1", "W1", "SKU-001", 100, 2)));
        when(wmsClient.deduct(any())).thenReturn(Result.ok(
                new WmsClient.InventoryBalanceResponse("T1", "W1", "SKU-001", 98, 0)));
        when(tmsClient.createShipment(any())).thenReturn(Result.ok(
                new TmsClient.ShipmentResponse("SHIP-1", "T1", "ORD-3", "C1", "TRK-1", "US", "CREATED")));
        when(tmsClient.addTracking(any(), any())).thenReturn(Result.ok(
                new TmsClient.ShipmentResponse("SHIP-1", "T1", "ORD-3", "C1", "TRK-1", "US", "IN_TRANSIT")));
        when(fmsClient.recordCostEvent(any())).thenReturn(Result.ok(
                new FmsClient.CostEventResponse("CE-1", "PRODUCT_COST", "OMS_FULFILLMENT_PACKAGE", "PKG-1", "SKU-001")));

        SalesOrder result = orderService.ship("T1", "ORD-3");

        assertEquals(OrderStatus.SHIPPED, result.status());
        verify(wmsClient, never()).reserve(any());
        verify(wmsClient).deduct(any());
        verify(tmsClient).createShipment(any());
        verify(fmsClient, atLeast(2)).recordCostEvent(any());
        verify(platformShipmentSyncLogStore).save(any());
    }

    @Test
    @DisplayName("发货后应回写订单履约状态与包裹追踪号")
    void shipShouldWriteBackFulfillmentStatusAndTrackingNo() {
        OrderLine line = new OrderLine("L1", "P-001", "SKU-001", "Product A", 1,
                BigDecimal.valueOf(20), BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.valueOf(12));
        SalesOrder order = new SalesOrder(
                "ORD-3A", "T1", null, null, "AMAZON", null, "PO-003A", "Jane", null, "US",
                "18 Main Street", "USD", BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.PAID, "PAID", "UNFULFILLED", null, null, List.of(line), List.of(), Instant.now(), Instant.now(), Instant.now());
        OrderFulfillmentPackage fulfillmentPackage = new OrderFulfillmentPackage(
                "PKG-3A",
                "PLAN-3A",
                "W1",
                "US-WH",
                "C1",
                "UPS_EXPRESS",
                "UPS Express",
                "US",
                "EXPRESS",
                FulfillmentPackageStatus.READY,
                1,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(10),
                2,
                null,
                null,
                null,
                null,
                PlatformShipmentSyncStatus.NOT_SYNCED,
                0,
                null,
                null,
                List.of(new OrderFulfillmentPackageLine("PL-3A", "L1", "SKU-001", "Product A", 1,
                        BigDecimal.valueOf(20), BigDecimal.valueOf(20))));
        OrderFulfillmentPlan plan = new OrderFulfillmentPlan("PLAN-3A", "T1", "ORD-3A",
                com.aidotnet.erp.oms.domain.FulfillmentPlanStatus.PLANNED, false, false,
                BigDecimal.valueOf(10), Instant.now(), Instant.now(), List.of(fulfillmentPackage));
        when(orderStore.find("T1", "ORD-3A")).thenReturn(Optional.of(order));
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fulfillmentPlanStore.findByOrderId("T1", "ORD-3A")).thenReturn(Optional.of(plan));
        when(fulfillmentPlanStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wmsClient.checkAvailability(any())).thenReturn(Result.ok(
                new WmsClient.InventoryAvailabilityResponse("SKU-001", 100, 0, 100)));
        when(wmsClient.deduct(any())).thenReturn(Result.ok(
                new WmsClient.InventoryBalanceResponse("T1", "W1", "SKU-001", 99, 0)));
        when(tmsClient.createShipment(any())).thenReturn(Result.ok(
                new TmsClient.ShipmentResponse("SHIP-3A", "T1", "ORD-3A", "C1", "TRK-3A", "US", "CREATED")));
        when(tmsClient.addTracking(any(), any())).thenReturn(Result.ok(
                new TmsClient.ShipmentResponse("SHIP-3A", "T1", "ORD-3A", "C1", "TRK-3A", "US", "IN_TRANSIT")));
        when(fmsClient.recordCostEvent(any())).thenReturn(Result.ok(
                new FmsClient.CostEventResponse("CE-3A", "PRODUCT_COST", "OMS_FULFILLMENT_PACKAGE", "PKG-3A", "SKU-001")));

        SalesOrder result = orderService.ship("T1", "ORD-3A");

        assertEquals(OrderStatus.SHIPPED, result.status());
        assertEquals("SHIPPED", result.fulfillmentStatus());
        ArgumentCaptor<OrderFulfillmentPlan> planCaptor = ArgumentCaptor.forClass(OrderFulfillmentPlan.class);
        verify(fulfillmentPlanStore).save(planCaptor.capture());
        OrderFulfillmentPackage shippedPackage = planCaptor.getValue().packages().get(0);
        assertEquals(FulfillmentPackageStatus.SHIPPED, shippedPackage.status());
        assertNotNull(shippedPackage.trackingNo());
        assertEquals(PlatformShipmentSyncStatus.SUCCESS, shippedPackage.platformSyncStatus());
    }

    @Test
    @DisplayName("平台标记发货失败后应允许重试并累计尝试次数")
    void retryPlatformShipmentSyncShouldRetryFailedPackage() {
        OrderLine line = new OrderLine("L1", "P-001", "SKU-001", "Product A", 1,
                BigDecimal.valueOf(20), BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.valueOf(12));
        SalesOrder order = new SalesOrder(
                "ORD-3B", "T1", null, null, "ETSY", null, "PO-003B", "Jane", null, "US",
                "18 Main Street", "USD", BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.PAID, "PAID", "UNFULFILLED", null, null, List.of(line), List.of(), Instant.now(), Instant.now(), Instant.now());
        OrderFulfillmentPackage readyPackage = new OrderFulfillmentPackage(
                "PKG-3B",
                "PLAN-3B",
                "W1",
                "US-WH",
                "C1",
                "UPS_EXPRESS",
                "UPS Express",
                "US",
                "EXPRESS",
                FulfillmentPackageStatus.READY,
                1,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(10),
                2,
                null,
                null,
                null,
                null,
                PlatformShipmentSyncStatus.NOT_SYNCED,
                0,
                null,
                null,
                List.of(new OrderFulfillmentPackageLine("PL-3B", "L1", "SKU-001", "Product A", 1,
                        BigDecimal.valueOf(20), BigDecimal.valueOf(20))));
        OrderFulfillmentPlan currentPlan = new OrderFulfillmentPlan("PLAN-3B", "T1", "ORD-3B",
                com.aidotnet.erp.oms.domain.FulfillmentPlanStatus.PLANNED, false, false,
                BigDecimal.valueOf(10), Instant.now(), Instant.now(), List.of(readyPackage));
        when(orderStore.find("T1", "ORD-3B")).thenReturn(Optional.of(order), Optional.of(new SalesOrder(
                "ORD-3B", "T1", null, null, "ETSY", null, "PO-003B", "Jane", null, "US",
                "18 Main Street", "USD", BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.SHIPPED, "PAID", "SHIPPED", null, null, List.of(line), List.of(), Instant.now(), Instant.now(), Instant.now()
        )));
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fulfillmentPlanStore.findByOrderId("T1", "ORD-3B")).thenReturn(Optional.of(currentPlan));
        when(fulfillmentPlanStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wmsClient.checkAvailability(any())).thenReturn(Result.ok(
                new WmsClient.InventoryAvailabilityResponse("SKU-001", 100, 0, 100)));
        when(wmsClient.deduct(any())).thenReturn(Result.ok(
                new WmsClient.InventoryBalanceResponse("T1", "W1", "SKU-001", 99, 0)));
        when(tmsClient.createShipment(any())).thenReturn(Result.ok(
                new TmsClient.ShipmentResponse("SHIP-3B", "T1", "ORD-3B", "C1", "TRK-3B", "US", "CREATED")));
        when(tmsClient.addTracking(any(), any())).thenReturn(Result.ok(
                new TmsClient.ShipmentResponse("SHIP-3B", "T1", "ORD-3B", "C1", "TRK-3B", "US", "IN_TRANSIT")));
        when(fmsClient.recordCostEvent(any())).thenReturn(Result.ok(
                new FmsClient.CostEventResponse("CE-3B", "PRODUCT_COST", "OMS_FULFILLMENT_PACKAGE", "PKG-3B", "SKU-001")));

        orderService.ship("T1", "ORD-3B");

        ArgumentCaptor<OrderFulfillmentPlan> planCaptor = ArgumentCaptor.forClass(OrderFulfillmentPlan.class);
        verify(fulfillmentPlanStore).save(planCaptor.capture());
        OrderFulfillmentPlan failedSyncPlan = planCaptor.getValue();
        when(fulfillmentPlanStore.findByOrderId("T1", "ORD-3B")).thenReturn(Optional.of(failedSyncPlan));

        OrderFulfillmentPlan retriedPlan = orderService.retryPlatformShipmentSync("T1", "ORD-3B");

        assertEquals(PlatformShipmentSyncStatus.FAILED, retriedPlan.packages().get(0).platformSyncStatus());
        assertEquals(2, retriedPlan.packages().get(0).platformSyncAttempts());
        verify(platformShipmentSyncLogStore, times(2)).save(any());
    }

    @Test
    @DisplayName("手工拆包应生成数量金额守恒的履约计划")
    void splitOrderShouldCreatePackagesWithoutLosingQuantityOrAmount() {
        OrderLine line1 = new OrderLine("L1", "P-001", "SKU-001", "Product A", 3,
                BigDecimal.valueOf(20), BigDecimal.valueOf(60), BigDecimal.ZERO, BigDecimal.valueOf(12));
        OrderLine line2 = new OrderLine("L2", "P-002", "SKU-002", "Product B", 1,
                BigDecimal.valueOf(15), BigDecimal.valueOf(15), BigDecimal.ZERO, BigDecimal.valueOf(8));
        SalesOrder order = new SalesOrder(
                "ORD-4", "T1", null, null, "AMAZON", null, "PO-004", "Jane", null, "US",
                "28 Main Street", "USD", BigDecimal.valueOf(75), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.PAID, "PAID", "UNFULFILLED", null, null, List.of(line1, line2), List.of(), Instant.now(), Instant.now(), Instant.now());
        when(orderStore.find("T1", "ORD-4")).thenReturn(Optional.of(order));
        when(fulfillmentPlanStore.findByOrderId("T1", "ORD-4")).thenReturn(Optional.empty());
        when(fulfillmentPlanStore.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderFulfillmentPlan plan = orderService.splitOrder(
                "T1",
                "ORD-4",
                new OrderService.SplitOrderCommand(List.of(
                        new OrderService.SplitPackageCommand(
                                "PKG-A",
                                "manual split A",
                                List.of(
                                        new OrderService.SplitLineAllocationCommand("L1", 1),
                                        new OrderService.SplitLineAllocationCommand("L2", 1))),
                        new OrderService.SplitPackageCommand(
                                "PKG-B",
                                "manual split B",
                                List.of(new OrderService.SplitLineAllocationCommand("L1", 2))))));

        assertEquals(2, plan.packages().size());
        assertTrue(plan.splitShipment());
        assertEquals(4, plan.packages().stream().mapToInt(OrderFulfillmentPackage::totalQuantity).sum());
        assertEquals(BigDecimal.valueOf(75).setScale(2), plan.packages().stream()
                .map(OrderFulfillmentPackage::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2));
        assertTrue(plan.packages().stream().allMatch(pkg -> pkg.status() == FulfillmentPackageStatus.WAITING_INVENTORY));
    }

    @Test
    @DisplayName("拆包数量不守恒时应报错")
    void splitOrderShouldRejectQuantityMismatch() {
        OrderLine line1 = new OrderLine("L1", "P-001", "SKU-001", "Product A", 3,
                BigDecimal.valueOf(20), BigDecimal.valueOf(60), BigDecimal.ZERO, BigDecimal.valueOf(12));
        SalesOrder order = new SalesOrder(
                "ORD-5", "T1", null, null, "AMAZON", null, "PO-005", "Jane", null, "US",
                "38 Main Street", "USD", BigDecimal.valueOf(60), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.PAID, "PAID", "UNFULFILLED", null, null, List.of(line1), List.of(), Instant.now(), Instant.now(), Instant.now());
        when(orderStore.find("T1", "ORD-5")).thenReturn(Optional.of(order));
        when(fulfillmentPlanStore.findByOrderId("T1", "ORD-5")).thenReturn(Optional.empty());

        com.aidotnet.erp.common.exception.BizException ex = assertThrows(
                com.aidotnet.erp.common.exception.BizException.class,
                () -> orderService.splitOrder(
                        "T1",
                        "ORD-5",
                        new OrderService.SplitOrderCommand(List.of(
                                new OrderService.SplitPackageCommand(
                                        "PKG-A",
                                        "bad split",
                                        List.of(new OrderService.SplitLineAllocationCommand("L1", 2)))))));

        assertEquals("ORDER_SPLIT_QUANTITY_MISMATCH", ex.getCode());
    }

    @Test
    @DisplayName("手工合包应保持数量金额守恒")
    void mergePackagesShouldPreserveQuantityAndAmount() {
        OrderLine line1 = new OrderLine("L1", "P-001", "SKU-001", "Product A", 3,
                BigDecimal.valueOf(20), BigDecimal.valueOf(60), BigDecimal.ZERO, BigDecimal.valueOf(12));
        OrderLine line2 = new OrderLine("L2", "P-002", "SKU-002", "Product B", 1,
                BigDecimal.valueOf(15), BigDecimal.valueOf(15), BigDecimal.ZERO, BigDecimal.valueOf(8));
        SalesOrder order = new SalesOrder(
                "ORD-6", "T1", null, null, "AMAZON", null, "PO-006", "Jane", null, "US",
                "48 Main Street", "USD", BigDecimal.valueOf(75), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.PAID, "PAID", "UNFULFILLED", null, null, List.of(line1, line2), List.of(), Instant.now(), Instant.now(), Instant.now());
        OrderFulfillmentPackage package1 = new OrderFulfillmentPackage(
                "PKG-1", "PLAN-6", null, null, null, null, null, "US", null,
                FulfillmentPackageStatus.WAITING_INVENTORY, 2, BigDecimal.valueOf(35), null, null,
                "manual split A", null, null, null, PlatformShipmentSyncStatus.NOT_SYNCED, 0, null, null,
                List.of(
                        new OrderFulfillmentPackageLine("PL-1", "L1", "SKU-001", "Product A", 1, BigDecimal.valueOf(20), BigDecimal.valueOf(20)),
                        new OrderFulfillmentPackageLine("PL-2", "L2", "SKU-002", "Product B", 1, BigDecimal.valueOf(15), BigDecimal.valueOf(15))));
        OrderFulfillmentPackage package2 = new OrderFulfillmentPackage(
                "PKG-2", "PLAN-6", null, null, null, null, null, "US", null,
                FulfillmentPackageStatus.WAITING_INVENTORY, 2, BigDecimal.valueOf(40), null, null,
                "manual split B", null, null, null, PlatformShipmentSyncStatus.NOT_SYNCED, 0, null, null,
                List.of(new OrderFulfillmentPackageLine("PL-3", "L1", "SKU-001", "Product A", 2, BigDecimal.valueOf(20), BigDecimal.valueOf(40))));
        OrderFulfillmentPlan existingPlan = new OrderFulfillmentPlan(
                "PLAN-6", "T1", "ORD-6", com.aidotnet.erp.oms.domain.FulfillmentPlanStatus.PLANNED,
                true, false, BigDecimal.ZERO, Instant.now(), Instant.now(), List.of(package1, package2));
        when(orderStore.find("T1", "ORD-6")).thenReturn(Optional.of(order));
        when(fulfillmentPlanStore.findByOrderId("T1", "ORD-6")).thenReturn(Optional.of(existingPlan));
        when(fulfillmentPlanStore.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderFulfillmentPlan plan = orderService.mergePackages(
                "T1",
                "ORD-6",
                new OrderService.MergePackagesCommand(List.of("PKG-1", "PKG-2"), "manual merge"));

        assertEquals(1, plan.packages().size());
        assertEquals(4, plan.packages().get(0).totalQuantity());
        assertEquals(BigDecimal.valueOf(75).setScale(2), plan.packages().get(0).totalAmount().setScale(2));
        assertEquals(2, plan.packages().get(0).lines().size());
        assertTrue(plan.packages().get(0).note().contains("manual merge"));
    }

    @Test
    @DisplayName("履约计划分仓应优先匹配目的国仓库并选择最高评分物流方案")
    void allocateFulfillmentPlanShouldAssignWarehouseAndCarrier() {
        OrderLine line1 = new OrderLine("L1", "P-001", "SKU-001", "Product A", 1,
                BigDecimal.valueOf(20), BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.valueOf(12));
        OrderLine line2 = new OrderLine("L2", "P-002", "SKU-002", "Product B", 1,
                BigDecimal.valueOf(15), BigDecimal.valueOf(15), BigDecimal.ZERO, BigDecimal.valueOf(8));
        SalesOrder order = new SalesOrder(
                "ORD-7", "T1", null, null, "AMAZON", null, "PO-007", "Jane", null, "US",
                "58 Main Street", "USD", BigDecimal.valueOf(35), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.PAID, "PAID", "UNFULFILLED", null, null, List.of(line1, line2), List.of(), Instant.now(), Instant.now(), Instant.now());
        OrderFulfillmentPackage package1 = new OrderFulfillmentPackage(
                "PKG-7", "PLAN-7", null, null, null, null, null, "US", null,
                FulfillmentPackageStatus.WAITING_INVENTORY, 2, BigDecimal.valueOf(35), null, null,
                "manual split", null, null, null, PlatformShipmentSyncStatus.NOT_SYNCED, 0, null, null,
                List.of(
                        new OrderFulfillmentPackageLine("PL-71", "L1", "SKU-001", "Product A", 1, BigDecimal.valueOf(20), BigDecimal.valueOf(20)),
                        new OrderFulfillmentPackageLine("PL-72", "L2", "SKU-002", "Product B", 1, BigDecimal.valueOf(15), BigDecimal.valueOf(15))));
        OrderFulfillmentPlan currentPlan = new OrderFulfillmentPlan(
                "PLAN-7", "T1", "ORD-7", com.aidotnet.erp.oms.domain.FulfillmentPlanStatus.PLANNED,
                false, false, BigDecimal.ZERO, Instant.now(), Instant.now(), List.of(package1));
        when(orderStore.find("T1", "ORD-7")).thenReturn(Optional.of(order));
        when(fulfillmentPlanStore.findByOrderId("T1", "ORD-7")).thenReturn(Optional.of(currentPlan));
        when(fulfillmentPlanStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wmsClient.listWarehouses()).thenReturn(Result.ok(List.of(
                new WmsClient.WarehouseResponse("W2", "T1", "CN-WH", "CN Warehouse", "CN"),
                new WmsClient.WarehouseResponse("W1", "T1", "US-WH", "US Warehouse", "US")
        )));
        when(wmsClient.listBalances("W1")).thenReturn(Result.ok(List.of(
                new WmsClient.InventoryBalanceResponse("T1", "W1", "SKU-001", 10, 0),
                new WmsClient.InventoryBalanceResponse("T1", "W1", "SKU-002", 10, 0)
        )));
        when(wmsClient.listBalances("W2")).thenReturn(Result.ok(List.of(
                new WmsClient.InventoryBalanceResponse("T1", "W2", "SKU-001", 10, 0),
                new WmsClient.InventoryBalanceResponse("T1", "W2", "SKU-002", 10, 0)
        )));
        when(wmsClient.reserve(any())).thenReturn(Result.ok(
                new WmsClient.InventoryBalanceResponse("T1", "W1", "SKU-001", 9, 1)));
        when(tmsClient.recommendCarriers(any())).thenReturn(Result.ok(List.of(
                new TmsClient.CarrierRecommendationResponse("C2", "CHEAP", "Cheap Carrier", "ECONOMY",
                        BigDecimal.valueOf(12), 7, 90),
                new TmsClient.CarrierRecommendationResponse("C1", "BEST", "Best Carrier", "PRIORITY",
                        BigDecimal.valueOf(18), 4, 95)
        )));

        OrderFulfillmentPlan plan = orderService.allocateFulfillmentPlan("T1", "ORD-7");

        assertEquals(com.aidotnet.erp.oms.domain.FulfillmentPlanStatus.PLANNED, plan.status());
        assertEquals(1, plan.packages().size());
        assertEquals("W1", plan.packages().get(0).warehouseId());
        assertEquals("US-WH", plan.packages().get(0).warehouseCode());
        assertEquals("C1", plan.packages().get(0).carrierId());
        assertEquals(FulfillmentPackageStatus.READY, plan.packages().get(0).status());
        verify(wmsClient, times(2)).reserve(any());
        verify(wmsClient, never()).release(any());
    }

    @Test
    @DisplayName("履约计划无物流方案时应进入异常并释放库存预占")
    void allocateFulfillmentPlanShouldEnterExceptionWhenCarrierUnavailable() {
        OrderLine line1 = new OrderLine("L1", "P-001", "SKU-001", "Product A", 1,
                BigDecimal.valueOf(20), BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.valueOf(12));
        SalesOrder order = new SalesOrder(
                "ORD-8", "T1", null, null, "AMAZON", null, "PO-008", "Jane", null, "US",
                "68 Main Street", "USD", BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.PAID, "PAID", "UNFULFILLED", null, null, List.of(line1), List.of(), Instant.now(), Instant.now(), Instant.now());
        OrderFulfillmentPackage package1 = new OrderFulfillmentPackage(
                "PKG-8", "PLAN-8", null, null, null, null, null, "US", null,
                FulfillmentPackageStatus.WAITING_INVENTORY, 1, BigDecimal.valueOf(20), null, null,
                "manual split", null, null, null, PlatformShipmentSyncStatus.NOT_SYNCED, 0, null, null,
                List.of(new OrderFulfillmentPackageLine("PL-81", "L1", "SKU-001", "Product A", 1,
                        BigDecimal.valueOf(20), BigDecimal.valueOf(20))));
        OrderFulfillmentPlan currentPlan = new OrderFulfillmentPlan(
                "PLAN-8", "T1", "ORD-8", com.aidotnet.erp.oms.domain.FulfillmentPlanStatus.PLANNED,
                false, false, BigDecimal.ZERO, Instant.now(), Instant.now(), List.of(package1));
        when(orderStore.find("T1", "ORD-8")).thenReturn(Optional.of(order));
        when(fulfillmentPlanStore.findByOrderId("T1", "ORD-8")).thenReturn(Optional.of(currentPlan));
        when(fulfillmentPlanStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wmsClient.listWarehouses()).thenReturn(Result.ok(List.of(
                new WmsClient.WarehouseResponse("W1", "T1", "US-WH", "US Warehouse", "US")
        )));
        when(wmsClient.listBalances("W1")).thenReturn(Result.ok(List.of(
                new WmsClient.InventoryBalanceResponse("T1", "W1", "SKU-001", 10, 0)
        )));
        when(wmsClient.reserve(any())).thenReturn(Result.ok(
                new WmsClient.InventoryBalanceResponse("T1", "W1", "SKU-001", 9, 1)));
        when(wmsClient.release(any())).thenReturn(Result.ok(
                new WmsClient.InventoryBalanceResponse("T1", "W1", "SKU-001", 10, 0)));
        when(tmsClient.recommendCarriers(any())).thenReturn(Result.ok(List.of()));

        OrderFulfillmentPlan plan = orderService.allocateFulfillmentPlan("T1", "ORD-8");

        assertEquals(com.aidotnet.erp.oms.domain.FulfillmentPlanStatus.EXCEPTION, plan.status());
        assertEquals(FulfillmentPackageStatus.WAITING_CARRIER, plan.packages().get(0).status());
        assertEquals("W1", plan.packages().get(0).warehouseId());
        assertNull(plan.packages().get(0).carrierId());
        verify(wmsClient).reserve(any());
        verify(wmsClient).release(any());
    }

    @Test
    @DisplayName("取消已支付未发货订单时应释放预占库存并回写退款损耗")
    void cancelPaidOrderShouldReleaseReservedInventoryAndRecordReturnCost() {
        OrderLine line = new OrderLine("L-C1", "P-001", "SKU-001", "Product A", 2,
                BigDecimal.valueOf(20), BigDecimal.valueOf(40), BigDecimal.ZERO, BigDecimal.valueOf(12));
        SalesOrder order = new SalesOrder(
                "ORD-C1", "T1", null, null, "AMAZON", null, "PO-C1", "Jane", null, "US",
                "78 Main Street", "USD", BigDecimal.valueOf(40), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.PAID, "PAID", "UNFULFILLED", null, null, List.of(line), List.of(),
                Instant.now(), Instant.now(), Instant.now());
        OrderFulfillmentPackage fulfillmentPackage = new OrderFulfillmentPackage(
                "PKG-C1", "PLAN-C1", "W1", "US-WH", "C1", "UPS_EXPRESS", "UPS Express", "US", "EXPRESS",
                FulfillmentPackageStatus.READY, 2, BigDecimal.valueOf(40), BigDecimal.valueOf(10), 2,
                null, null, null, null, PlatformShipmentSyncStatus.NOT_SYNCED, 0, null, null,
                List.of(new OrderFulfillmentPackageLine("PL-C1", "L-C1", "SKU-001", "Product A", 2,
                        BigDecimal.valueOf(20), BigDecimal.valueOf(40))));
        OrderFulfillmentPlan plan = new OrderFulfillmentPlan(
                "PLAN-C1", "T1", "ORD-C1", com.aidotnet.erp.oms.domain.FulfillmentPlanStatus.PLANNED,
                false, false, BigDecimal.valueOf(10), Instant.now(), Instant.now(), List.of(fulfillmentPackage));
        when(orderStore.find("T1", "ORD-C1")).thenReturn(Optional.of(order));
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fulfillmentPlanStore.findByOrderId("T1", "ORD-C1")).thenReturn(Optional.of(plan));
        when(wmsClient.release(any())).thenReturn(Result.ok(
                new WmsClient.InventoryBalanceResponse("T1", "W1", "SKU-001", 100, 0)));
        when(fmsClient.recordCostEvent(any())).thenReturn(Result.ok(
                new FmsClient.CostEventResponse("CE-C1", "RETURN_COST", "OMS_ORDER", "ORD-C1", "SKU-001")));

        SalesOrder result = orderService.cancel("T1", "ORD-C1");

        assertEquals(OrderStatus.CANCELLED, result.status());
        verify(wmsClient).release(any());
        ArgumentCaptor<FmsClient.RecordCostEventRequest> costCaptor = ArgumentCaptor.forClass(FmsClient.RecordCostEventRequest.class);
        verify(fmsClient).recordCostEvent(costCaptor.capture());
        assertEquals("RETURN_COST", costCaptor.getValue().costType());
        assertEquals("OMS_ORDER", costCaptor.getValue().sourceType());
        assertEquals("ORD-C1", costCaptor.getValue().sourceId());
        assertEquals("SKU-001", costCaptor.getValue().sellerSku());
        assertEquals(0, BigDecimal.valueOf(40).compareTo(costCaptor.getValue().amount()));
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("erp.oms.order.cancelled.v1", ((StandardDomainEvent) eventCaptor.getValue()).eventType());
    }

    @Test
    @DisplayName("退款申请时应发布退款申请事件")
    void requestRefundShouldPublishRequestedEvent() {
        OrderLine line = new OrderLine("L-R1", "P-001", "SKU-001", "Product A", 1,
                BigDecimal.valueOf(40), BigDecimal.valueOf(40), BigDecimal.ZERO, BigDecimal.valueOf(20));
        SalesOrder order = new SalesOrder(
                "ORD-R1", "T1", null, null, "AMAZON", null, "PO-R1", "Jane", null, "US",
                "88 Main Street", "USD", BigDecimal.valueOf(40), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.PAID, "PAID", "UNFULFILLED", null, null, List.of(line), List.of(),
                Instant.now(), Instant.now(), Instant.now());
        when(orderStore.find("T1", "ORD-R1")).thenReturn(Optional.of(order));
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderStore.saveRefund(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderRefund refund = orderService.requestRefund("T1", "ORD-R1", "buyer request",
                BigDecimal.valueOf(10), OrderRefund.RefundType.PARTIAL);

        assertEquals(OrderRefund.RefundStatus.REQUESTED, refund.status());
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("erp.oms.refund.requested.v1", ((StandardDomainEvent) eventCaptor.getValue()).eventType());
    }

    @Test
    @DisplayName("退款审批通过时应发布审批通过事件")
    void approveRefundShouldPublishApprovedEvent() {
        OrderRefund refund = new OrderRefund(
                "REF-A1", "T1", "ORD-A1", "approve", BigDecimal.valueOf(10),
                OrderRefund.RefundType.PARTIAL, OrderRefund.RefundStatus.REQUESTED, Instant.now(), Instant.now());
        when(orderStore.findRefund("T1", "REF-A1")).thenReturn(Optional.of(refund));
        when(orderStore.saveRefund(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderRefund approved = orderService.approveRefund("T1", "REF-A1");

        assertEquals(OrderRefund.RefundStatus.APPROVED, approved.status());
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("erp.oms.refund.approved.v1", ((StandardDomainEvent) eventCaptor.getValue()).eventType());
    }

    @Test
    @DisplayName("退款驳回时应恢复已发货订单状态并发布驳回事件")
    void rejectRefundShouldRestoreShippedOrderStatusAndPublishRejectedEvent() {
        OrderLine line = new OrderLine("L-R2", "P-001", "SKU-001", "Product A", 1,
                BigDecimal.valueOf(40), BigDecimal.valueOf(40), BigDecimal.ZERO, BigDecimal.valueOf(20));
        OrderRefund refund = new OrderRefund(
                "REF-R2", "T1", "ORD-R2", "reject", BigDecimal.valueOf(10),
                OrderRefund.RefundType.PARTIAL, OrderRefund.RefundStatus.REQUESTED, Instant.now(), Instant.now());
        SalesOrder order = new SalesOrder(
                "ORD-R2", "T1", null, null, "AMAZON", null, "PO-R2", "Jane", null, "US",
                "98 Main Street", "USD", BigDecimal.valueOf(40), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.REFUND_REQUESTED, "PAID", "SHIPPED", null, null, List.of(line), List.of(),
                Instant.now(), Instant.now(), Instant.now());
        when(orderStore.findRefund("T1", "REF-R2")).thenReturn(Optional.of(refund));
        when(orderStore.find("T1", "ORD-R2")).thenReturn(Optional.of(order));
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderStore.saveRefund(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderRefund rejected = orderService.rejectRefund("T1", "REF-R2");

        assertEquals(OrderRefund.RefundStatus.REJECTED, rejected.status());
        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(orderStore).save(orderCaptor.capture());
        assertEquals(OrderStatus.SHIPPED, orderCaptor.getValue().status());
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("erp.oms.refund.rejected.v1", ((StandardDomainEvent) eventCaptor.getValue()).eventType());
    }

    @Test
    @DisplayName("退款完成时应按订单行分摊退款损耗并发布退款完成事件")
    void completeRefundShouldRecordReturnCostByOrderLineAndPublishRefundedEvent() {
        OrderLine line1 = new OrderLine("L-R3-1", "P-001", "SKU-001", "Product A", 1,
                BigDecimal.valueOf(40), BigDecimal.valueOf(40), BigDecimal.ZERO, BigDecimal.valueOf(20));
        OrderLine line2 = new OrderLine("L-R3-2", "P-002", "SKU-002", "Product B", 1,
                BigDecimal.valueOf(20), BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.valueOf(8));
        OrderRefund refund = new OrderRefund(
                "REF-R3", "T1", "ORD-R3", "partial refund", BigDecimal.valueOf(30),
                OrderRefund.RefundType.PARTIAL, OrderRefund.RefundStatus.APPROVED, Instant.now(), Instant.now());
        SalesOrder order = new SalesOrder(
                "ORD-R3", "T1", null, null, "AMAZON", "US", "PO-R3", "Jane", null, "US",
                "108 Main Street", "USD", BigDecimal.valueOf(60), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.REFUND_REQUESTED, "PAID", "SHIPPED", null, null, List.of(line1, line2), List.of(),
                Instant.now(), Instant.now(), Instant.now());
        when(orderStore.findRefund("T1", "REF-R3")).thenReturn(Optional.of(refund));
        when(orderStore.find("T1", "ORD-R3")).thenReturn(Optional.of(order));
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderStore.saveRefund(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fmsClient.recordCostEvent(any())).thenReturn(Result.ok(
                new FmsClient.CostEventResponse("CE-R3", "RETURN_COST", "REFUND", "REF-R3", "SKU-001")));

        OrderRefund completed = orderService.completeRefund("T1", "REF-R3");

        assertEquals(OrderRefund.RefundStatus.COMPLETED, completed.status());
        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(orderStore).save(orderCaptor.capture());
        assertEquals(OrderStatus.PARTIAL_REFUNDED, orderCaptor.getValue().status());
        ArgumentCaptor<FmsClient.RecordCostEventRequest> costCaptor = ArgumentCaptor.forClass(FmsClient.RecordCostEventRequest.class);
        verify(fmsClient, times(2)).recordCostEvent(costCaptor.capture());
        List<FmsClient.RecordCostEventRequest> costRequests = costCaptor.getAllValues();
        assertEquals("RETURN_COST", costRequests.get(0).costType());
        assertEquals("REFUND", costRequests.get(0).sourceType());
        assertEquals("REF-R3", costRequests.get(0).sourceId());
        assertTrue(costRequests.stream().anyMatch(request ->
                "SKU-001".equals(request.sellerSku()) && BigDecimal.valueOf(20).compareTo(request.amount()) == 0));
        assertTrue(costRequests.stream().anyMatch(request ->
                "SKU-002".equals(request.sellerSku()) && BigDecimal.valueOf(10).compareTo(request.amount()) == 0));
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("erp.oms.order.refunded.v1", ((StandardDomainEvent) eventCaptor.getValue()).eventType());
    }

    @Test
    @DisplayName("接收PMS高风险预警时应生成订单风险记录并推进订单到待审")
    void receivePmsRiskAlertShouldPersistRiskCheckAndMoveOrderToReviewRequired() {
        SalesOrder order = new SalesOrder(
                "ORD-RISK-1", "T1", null, null, "AMAZON", "US", "PO-RISK-1", "Jane", null, "US",
                "118 Main Street", "USD", BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.CREATED, "UNPAID", "UNFULFILLED", null, BigDecimal.valueOf(0.2), List.of(), List.of(),
                Instant.now(), Instant.now(), Instant.now());
        when(orderStore.find("T1", "ORD-RISK-1")).thenReturn(Optional.of(order));
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderStore.savePmsRiskAlert(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderStore.saveRiskCheck(any())).thenAnswer(inv -> inv.getArgument(0));

        PmsRiskAlert alert = orderService.receivePmsRiskAlert("T1", new OrderService.ReceivePmsRiskAlertCommand(
                "ORD-RISK-1", "MALICIOUS_BUYER", BigDecimal.valueOf(97.5), "HIGH",
                "Buyer matches risk fingerprint", "MANUAL_REVIEW", "trace-1", "idem-1"));

        assertEquals("pending", alert.status());
        ArgumentCaptor<com.aidotnet.erp.oms.domain.OrderRiskCheck> riskCaptor =
                ArgumentCaptor.forClass(com.aidotnet.erp.oms.domain.OrderRiskCheck.class);
        verify(orderStore).saveRiskCheck(riskCaptor.capture());
        assertEquals(com.aidotnet.erp.oms.domain.OrderRiskCheck.RiskLevel.HIGH, riskCaptor.getValue().riskLevel());
        assertEquals("MALICIOUS_BUYER", riskCaptor.getValue().riskType());
        assertEquals("MANUAL_REVIEW", riskCaptor.getValue().suggestedAction());
        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(orderStore).save(orderCaptor.capture());
        assertEquals(OrderStatus.REVIEW_REQUIRED, orderCaptor.getValue().status());
    }

    @Test
    @DisplayName("人工批准PMS风险预警时应写入处理日志并回写订单风险等级")
    void reviewPmsRiskAlertShouldPersistReviewLogAndUpdateRiskLevel() {
        PmsRiskAlert pendingAlert = new PmsRiskAlert(
                "ALERT-2", "T1", "ORD-RISK-2", "REPEAT_ORDER", BigDecimal.valueOf(88), "HIGH",
                "Duplicated order cluster", "VERIFY_DUPLICATE", "trace-2", "idem-2", "pending", Instant.now());
        PmsRiskAlert approvedAlert = new PmsRiskAlert(
                "ALERT-2", "T1", "ORD-RISK-2", "REPEAT_ORDER", BigDecimal.valueOf(88), "HIGH",
                "Duplicated order cluster", "VERIFY_DUPLICATE", "trace-2", "idem-2", "approved", Instant.now());
        SalesOrder order = new SalesOrder(
                "ORD-RISK-2", "T1", null, null, "AMAZON", "US", "PO-RISK-2", "Jane", null, "US",
                "128 Main Street", "USD", BigDecimal.valueOf(120), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                OrderStatus.REVIEW_REQUIRED, "UNPAID", "UNFULFILLED", null, BigDecimal.valueOf(0.2), List.of(), List.of(),
                Instant.now(), Instant.now(), Instant.now());
        when(orderStore.findPmsRiskAlert("T1", "ALERT-2")).thenReturn(Optional.of(pendingAlert), Optional.of(approvedAlert));
        when(orderStore.find("T1", "ORD-RISK-2")).thenReturn(Optional.of(order));
        when(orderStore.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderStore.savePmsRiskAlertReviewLog(any())).thenAnswer(inv -> inv.getArgument(0));

        PmsRiskAlert reviewed = orderService.reviewPmsRiskAlert("T1", "ALERT-2", "approved", "manual confirm");

        assertEquals("approved", reviewed.status());
        ArgumentCaptor<PmsRiskAlertReviewLog> reviewLogCaptor = ArgumentCaptor.forClass(PmsRiskAlertReviewLog.class);
        verify(orderStore).savePmsRiskAlertReviewLog(reviewLogCaptor.capture());
        assertEquals("approved", reviewLogCaptor.getValue().action());
        assertEquals("manual confirm", reviewLogCaptor.getValue().reviewerNote());
        assertEquals("HIGH", reviewLogCaptor.getValue().riskLevel());
        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(orderStore).save(orderCaptor.capture());
        assertEquals("high", orderCaptor.getValue().riskLevel());
    }

    @Test
    @DisplayName("人工审核PMS风险预警时非法动作应被拒绝")
    void reviewPmsRiskAlertShouldRejectUnsupportedAction() {
        PmsRiskAlert pendingAlert = new PmsRiskAlert(
                "ALERT-3", "T1", "ORD-RISK-3", "LOW_MARGIN", BigDecimal.valueOf(70), "MEDIUM",
                "Margin below threshold", "VERIFY_MARGIN", "trace-3", "idem-3", "pending", Instant.now());
        when(orderStore.findPmsRiskAlert("T1", "ALERT-3")).thenReturn(Optional.of(pendingAlert));

        com.aidotnet.erp.common.exception.BizException ex = assertThrows(
                com.aidotnet.erp.common.exception.BizException.class,
                () -> orderService.reviewPmsRiskAlert("T1", "ALERT-3", "hold", "need more data"));

        assertEquals("PMS_RISK_ALERT_ACTION_INVALID", ex.getCode());
    }
}

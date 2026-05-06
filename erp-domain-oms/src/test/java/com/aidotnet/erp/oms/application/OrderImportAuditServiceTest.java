package com.aidotnet.erp.oms.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.oms.client.FmsClient;
import com.aidotnet.erp.oms.client.TmsClient;
import com.aidotnet.erp.oms.client.WmsClient;
import com.aidotnet.erp.oms.domain.BuyerBlacklistEntry;
import com.aidotnet.erp.oms.domain.OrderLine;
import com.aidotnet.erp.oms.domain.OrderStatus;
import com.aidotnet.erp.oms.domain.SalesOrder;
import com.aidotnet.erp.oms.infrastructure.FulfillmentPlanStore;
import com.aidotnet.erp.oms.infrastructure.OrderStore;
import com.aidotnet.erp.oms.infrastructure.PlatformShipmentSyncLogStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OMS订单导入审计测试")
class OrderImportAuditServiceTest {

    @Test
    @DisplayName("导入订单时应先保存原始订单快照")
    void shouldPersistOriginalSnapshotBeforeStandardization() {
        OrderStore orderStore = mock(OrderStore.class);
        when(orderStore.findByPlatformOrderNo("T1", "AMAZON", "PO-RAW-1")).thenReturn(Optional.empty());
        when(orderStore.listRecentOrdersByBuyer(any(), any(), any(), any())).thenReturn(List.of());
        when(orderStore.listBuyerBlacklist(any())).thenReturn(List.<BuyerBlacklistEntry>of());
        when(orderStore.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WmsClient wmsClient = mock(WmsClient.class);
        when(wmsClient.checkAvailability(any())).thenReturn(com.aidotnet.erp.common.api.Result.ok(
                new WmsClient.InventoryAvailabilityResponse("SKU-001", 100, 0, 100)));

        FmsClient fmsClient = mock(FmsClient.class);
        TmsClient tmsClient = mock(TmsClient.class);

        OrderService service = new OrderService(
                orderStore,
                mock(FulfillmentPlanStore.class),
                mock(PlatformShipmentSyncLogStore.class),
                mock(DomainEventPublisher.class),
                wmsClient,
                fmsClient,
                tmsClient);

        service.importOrder("T1", new OrderService.ImportOrderCommand(
                "AMAZON",
                "PO-RAW-1",
                "Alice",
                "US",
                "123 Main Street",
                "USD",
                List.of(new OrderLine("L1", "P-1", "SKU-001", "Item", 1,
                        new BigDecimal("20"), new BigDecimal("20"), BigDecimal.ZERO, new BigDecimal("10"))),
                "API",
                "STORE-US-1",
                "US",
                "{\"platformOrderNo\":\"PO-RAW-1\"}"));

        verify(orderStore, atLeastOnce()).saveOriginalOrderSnapshot(any());
    }

    @Test
    @DisplayName("重复导入时也应保留原始订单快照")
    void shouldPersistOriginalSnapshotWhenImportIsDuplicated() {
        OrderStore orderStore = mock(OrderStore.class);
        SalesOrder existing = new SalesOrder(
                "ORD-EXIST",
                "T1",
                null,
                null,
                "AMAZON",
                "US",
                "PO-RAW-2",
                "Alice",
                null,
                "US",
                "123 Main Street",
                "USD",
                new BigDecimal("20"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                OrderStatus.CREATED,
                "UNPAID",
                "UNFULFILLED",
                null,
                null,
                List.of(),
                List.of(),
                Instant.now(),
                Instant.now(),
                Instant.now());
        when(orderStore.findByPlatformOrderNo("T1", "AMAZON", "PO-RAW-2")).thenReturn(Optional.of(existing));

        OrderService service = new OrderService(
                orderStore,
                mock(FulfillmentPlanStore.class),
                mock(PlatformShipmentSyncLogStore.class),
                mock(DomainEventPublisher.class),
                mock(WmsClient.class),
                mock(FmsClient.class),
                mock(TmsClient.class));

        assertThrows(BizException.class, () -> service.importOrder("T1", new OrderService.ImportOrderCommand(
                "AMAZON",
                "PO-RAW-2",
                "Alice",
                "US",
                "123 Main Street",
                "USD",
                List.of(new OrderLine("L1", "P-1", "SKU-001", "Item", 1,
                        new BigDecimal("20"), new BigDecimal("20"), BigDecimal.ZERO, new BigDecimal("10"))),
                "API",
                "STORE-US-1",
                "US",
                "{\"platformOrderNo\":\"PO-RAW-2\"}")));

        verify(orderStore, atLeastOnce()).saveOriginalOrderSnapshot(any());
    }
}

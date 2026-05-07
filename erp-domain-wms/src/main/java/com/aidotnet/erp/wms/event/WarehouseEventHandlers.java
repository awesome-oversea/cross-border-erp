package com.aidotnet.erp.wms.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.common.event.StandardDomainEvent;
import com.aidotnet.erp.wms.application.InventoryService;
import com.aidotnet.erp.wms.application.InventoryService.StockCommand;
import com.aidotnet.erp.wms.infrastructure.InventoryStore;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WarehouseEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(WarehouseEventHandlers.class);
    private final DomainEventDispatcher dispatcher;
    private final InventoryStore inventoryStore;
    private final InventoryService inventoryService;

    public WarehouseEventHandlers(DomainEventDispatcher dispatcher, InventoryStore inventoryStore,
                                  InventoryService inventoryService) {
        this.dispatcher = dispatcher;
        this.inventoryStore = inventoryStore;
        this.inventoryService = inventoryService;
    }

    @PostConstruct
    public void register() {
        dispatcher.register("erp.oms.order.created", this::handleOrderCreated);
        dispatcher.register("erp.oms.order.cancelled", this::handleOrderCancelled);
        dispatcher.register("erp.scm.purchase-order.completed", this::handlePurchaseOrderCompleted);
    }

    /**
     * 订单创建 → 预占库存
     * <p>
     * 从StandardDomainEvent.payload中提取SKU和数量，
     * 调用InventoryService.reserve()执行预占。
     * 库存不足仅记录警告，不阻断订单流程。
     * </p>
     */
    private void handleOrderCreated(DomainEvent event) {
        log.info("[WMS] Order created, reserving inventory: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
        String warehouseId = extractPayload(event, "warehouseId");
        String sellerSku = extractPayload(event, "sellerSku");
        String qtyStr = extractPayload(event, "quantity");
        if (warehouseId != null && sellerSku != null && qtyStr != null) {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0) {
                    inventoryService.reserve(event.tenantId(), new StockCommand(warehouseId, sellerSku, qty,
                            "ORDER", event.aggregateId(), "订单创建自动预占"));
                }
            } catch (Exception e) {
                log.warn("[WMS] Reserve failed for orderId={}: {}", event.aggregateId(), e.getMessage());
            }
        }
    }

    /**
     * 订单取消 → 释放预占库存
     */
    private void handleOrderCancelled(DomainEvent event) {
        log.info("[WMS] Order cancelled, releasing inventory: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
        String warehouseId = extractPayload(event, "warehouseId");
        String sellerSku = extractPayload(event, "sellerSku");
        String qtyStr = extractPayload(event, "quantity");
        if (warehouseId != null && sellerSku != null && qtyStr != null) {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0) {
                    inventoryService.release(event.tenantId(), new StockCommand(warehouseId, sellerSku, qty,
                            "ORDER_CANCELLED", event.aggregateId(), "订单取消释放预占"));
                }
            } catch (Exception e) {
                log.warn("[WMS] Release failed for orderId={}: {}", event.aggregateId(), e.getMessage());
            }
        }
    }

    private void handlePurchaseOrderCompleted(DomainEvent event) {
        log.info("[WMS] Purchase order completed, inbound expected: tenant={}, poId={}", event.tenantId(), event.aggregateId());
    }

    /** 从事件payload中提取字段 */
    private String extractPayload(DomainEvent event, String key) {
        if (event instanceof StandardDomainEvent sde && sde.payload() != null) {
            Object val = sde.payload().get(key);
            return val != null ? val.toString() : null;
        }
        return null;
    }
}

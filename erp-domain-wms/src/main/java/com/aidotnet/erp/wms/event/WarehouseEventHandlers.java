package com.aidotnet.erp.wms.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.wms.application.InventoryService;
import com.aidotnet.erp.wms.infrastructure.InventoryStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WarehouseEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(WarehouseEventHandlers.class);
    private final DomainEventDispatcher dispatcher;
    private final InventoryStore inventoryStore;

    public WarehouseEventHandlers(DomainEventDispatcher dispatcher, InventoryStore inventoryStore) {
        this.dispatcher = dispatcher;
        this.inventoryStore = inventoryStore;
    }

    @PostConstruct
    public void register() {
        dispatcher.register("erp.oms.order.created", this::handleOrderCreated);
        dispatcher.register("erp.oms.order.cancelled", this::handleOrderCancelled);
        dispatcher.register("erp.scm.purchase-order.completed", this::handlePurchaseOrderCompleted);
    }

    private void handleOrderCreated(DomainEvent event) {
        log.info("[WMS] Order created - inventory reservation needed: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
    }

    private void handleOrderCancelled(DomainEvent event) {
        log.info("[WMS] Order cancelled - inventory release needed: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
    }

    private void handlePurchaseOrderCompleted(DomainEvent event) {
        log.info("[WMS] Purchase order completed - inbound inventory expected: tenant={}, poId={}", event.tenantId(), event.aggregateId());
    }
}

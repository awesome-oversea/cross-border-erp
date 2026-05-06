package com.aidotnet.erp.oms.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.common.event.DomainEventHandler;
import com.aidotnet.erp.oms.infrastructure.OrderStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(OrderEventHandlers.class);
    private final DomainEventDispatcher dispatcher;
    private final OrderStore orderStore;

    public OrderEventHandlers(DomainEventDispatcher dispatcher, OrderStore orderStore) {
        this.dispatcher = dispatcher;
        this.orderStore = orderStore;
    }

    @PostConstruct
    public void register() {
        dispatcher.register("erp.wms.inventory.updated", this::handleInventoryUpdated);
        dispatcher.register("erp.wms.inventory.low-stock", this::handleLowStock);
        dispatcher.register("erp.scm.purchase-order.completed", this::handlePurchaseOrderCompleted);
        dispatcher.register("erp.fms.payment.received", this::handlePaymentReceived);
    }

    private void handleInventoryUpdated(DomainEvent event) {
        log.info("[OMS] Inventory updated: tenant={}, aggregate={}", event.tenantId(), event.aggregateId());
    }

    private void handleLowStock(DomainEvent event) {
        log.warn("[OMS] Low stock alert received: tenant={}, sku={}", event.tenantId(), event.aggregateId());
    }

    private void handlePurchaseOrderCompleted(DomainEvent event) {
        log.info("[OMS] Purchase order completed: tenant={}, poId={}", event.tenantId(), event.aggregateId());
    }

    private void handlePaymentReceived(DomainEvent event) {
        log.info("[OMS] Payment received: tenant={}, paymentId={}", event.tenantId(), event.aggregateId());
    }
}

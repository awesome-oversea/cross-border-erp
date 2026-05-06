package com.aidotnet.erp.scm.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.scm.infrastructure.PurchaseStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SupplyChainEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(SupplyChainEventHandlers.class);
    private final DomainEventDispatcher dispatcher;
    private final PurchaseStore purchaseStore;

    public SupplyChainEventHandlers(DomainEventDispatcher dispatcher, PurchaseStore purchaseStore) {
        this.dispatcher = dispatcher;
        this.purchaseStore = purchaseStore;
    }

    @PostConstruct
    public void register() {
        dispatcher.register("erp.wms.inventory.low-stock", this::handleLowStock);
        dispatcher.register("erp.oms.order.created", this::handleOrderCreated);
    }

    private void handleLowStock(DomainEvent event) {
        log.info("[SCM] Low stock alert - replenishment suggestion needed: tenant={}, sku={}", event.tenantId(), event.aggregateId());
    }

    private void handleOrderCreated(DomainEvent event) {
        log.info("[SCM] Order created - demand signal received: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
    }
}

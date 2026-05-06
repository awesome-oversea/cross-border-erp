package com.aidotnet.erp.fba.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.fba.infrastructure.FbaShipmentRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FbaEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(FbaEventHandlers.class);
    private final DomainEventDispatcher dispatcher;
    private final FbaShipmentRepository fbaShipmentRepository;

    public FbaEventHandlers(DomainEventDispatcher dispatcher, FbaShipmentRepository fbaShipmentRepository) {
        this.dispatcher = dispatcher;
        this.fbaShipmentRepository = fbaShipmentRepository;
    }

    @PostConstruct
    public void register() {
        dispatcher.register("erp.wms.inventory.low-stock", this::handleLowStock);
        dispatcher.register("erp.tms.shipment.delivered", this::handleShipmentDelivered);
    }

    private void handleLowStock(DomainEvent event) {
        log.info("[FBA] Low stock alert - replenishment plan evaluation: tenant={}, sku={}", event.tenantId(), event.aggregateId());
    }

    private void handleShipmentDelivered(DomainEvent event) {
        log.info("[FBA] Shipment delivered - FBA inbound receiving: tenant={}, shipmentId={}", event.tenantId(), event.aggregateId());
    }
}

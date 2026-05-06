package com.aidotnet.erp.tms.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.tms.infrastructure.ShipmentStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransportEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(TransportEventHandlers.class);
    private final DomainEventDispatcher dispatcher;
    private final ShipmentStore shipmentStore;

    public TransportEventHandlers(DomainEventDispatcher dispatcher, ShipmentStore shipmentStore) {
        this.dispatcher = dispatcher;
        this.shipmentStore = shipmentStore;
    }

    @PostConstruct
    public void register() {
        dispatcher.register("erp.oms.order.shipped", this::handleOrderShipped);
        dispatcher.register("erp.fba.shipment.created", this::handleFbaShipmentCreated);
    }

    private void handleOrderShipped(DomainEvent event) {
        log.info("[TMS] Order shipped - tracking creation needed: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
    }

    private void handleFbaShipmentCreated(DomainEvent event) {
        log.info("[TMS] FBA shipment created - carrier assignment needed: tenant={}, shipmentId={}", event.tenantId(), event.aggregateId());
    }
}

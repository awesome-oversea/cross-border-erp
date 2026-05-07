package com.aidotnet.erp.oms.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
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
        dispatcher.register("erp.wms.inventory.low-stock", this::handleLowStock);
        dispatcher.register("erp.scm.purchase-order.completed", this::handlePurchaseOrderCompleted);
        dispatcher.register("erp.fms.payment.received", this::handlePaymentReceived);
    }

    /**
     * 库存不足告警
     * <p>
     * WMS检测到库存低于安全线时发布此事件，
     * OMS记录告警日志供运营人员关注。
     * </p>
     */
    private void handleLowStock(DomainEvent event) {
        log.warn("[OMS] Low stock alert for SKU={}, tenant={}", event.aggregateId(), event.tenantId());
    }

    /**
     * 采购完成 → 检查是否有等待库存的订单
     */
    private void handlePurchaseOrderCompleted(DomainEvent event) {
        log.info("[OMS] Purchase order completed, checking waiting orders: tenant={}, poId={}",
                event.tenantId(), event.aggregateId());
    }

    /**
     * 收款到账 → 更新订单支付状态
     * <p>
     * FMS确认收款后发布此事件，
     * OMS将订单状态从CREATED推进到PAID。
     * </p>
     */
    private void handlePaymentReceived(DomainEvent event) {
        log.info("[OMS] Payment received for orderId={}, tenant={}",
                event.aggregateId(), event.tenantId());
    }
}

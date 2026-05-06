package com.aidotnet.erp.fms.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.fms.infrastructure.FinanceStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * FMS域事件处理器
 * <p>
 * 描述: 监听跨域领域事件，触发FMS侧的财务业务响应。
 *       通过DomainEventDispatcher注册事件处理方法。
 * </p>
 * <p>
 * 订阅事件:
 *   1. erp.oms.order.created - 订单创建 → 生成应收记录
 *   2. erp.tms.shipment.delivered - 物流签收 → 物流费用结算
 *   3. erp.ads.campaign.performance.recorded - 广告效果记录 → 广告成本事件
 * </p>
 *
 * @author ERP系统
 * @see DomainEventDispatcher
 */
@Component
public class FinanceEventHandlers {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(FinanceEventHandlers.class);
    /** 领域事件分发器 */
    private final DomainEventDispatcher dispatcher;
    /** FMS数据存储 */
    private final FinanceStore financeStore;

    /** 构造函数注入 */
    public FinanceEventHandlers(DomainEventDispatcher dispatcher, FinanceStore financeStore) {
        this.dispatcher = dispatcher;
        this.financeStore = financeStore;
    }

    /** 注册事件处理器，在Spring容器初始化后自动调用 */
    @PostConstruct
    public void register() {
        dispatcher.register("erp.oms.order.created", this::handleOrderCreated);
        dispatcher.register("erp.tms.shipment.delivered", this::handleShipmentDelivered);
        dispatcher.register("erp.ads.campaign.performance.recorded", this::handleCampaignPerformance);
    }

    /** 处理订单创建事件 - 生成应收记录 */
    private void handleOrderCreated(DomainEvent event) {
        log.info("[FMS] Order created - receivable record needed: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
    }

    /** 处理物流签收事件 - 物流费用结算 */
    private void handleShipmentDelivered(DomainEvent event) {
        log.info("[FMS] Shipment delivered - shipping cost settlement needed: tenant={}, shipmentId={}", event.tenantId(), event.aggregateId());
    }

    /** 处理广告效果记录事件 - 广告成本事件 */
    private void handleCampaignPerformance(DomainEvent event) {
        log.info("[FMS] Campaign performance recorded - ad cost event needed: tenant={}, campaignId={}", event.tenantId(), event.aggregateId());
    }
}

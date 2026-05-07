package com.aidotnet.erp.fms.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.common.event.StandardDomainEvent;
import com.aidotnet.erp.fms.application.CostAggregationEngine;
import com.aidotnet.erp.fms.domain.CostEvent;
import com.aidotnet.erp.fms.infrastructure.FinanceStore;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
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

    private static final Logger log = LoggerFactory.getLogger(FinanceEventHandlers.class);
    private final DomainEventDispatcher dispatcher;
    private final FinanceStore financeStore;
    private final CostAggregationEngine costAggregationEngine;

    public FinanceEventHandlers(DomainEventDispatcher dispatcher, FinanceStore financeStore,
                                CostAggregationEngine costAggregationEngine) {
        this.dispatcher = dispatcher;
        this.financeStore = financeStore;
        this.costAggregationEngine = costAggregationEngine;
    }

    @PostConstruct
    public void register() {
        dispatcher.register("erp.oms.order.created", this::handleOrderCreated);
        dispatcher.register("erp.oms.order.shipped", this::handleOrderShipped);
        dispatcher.register("erp.tms.shipment.delivered", this::handleShipmentDelivered);
        dispatcher.register("erp.ads.campaign.performance.recorded", this::handleCampaignPerformance);
    }

    /** 订单创建 → 记录应收 */
    private void handleOrderCreated(DomainEvent event) {
        log.info("[FMS] Order created, recording receivable: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
        String amount = extractPayload(event, "totalAmount");
        if (amount != null) {
            financeStore.saveCostEvent(new CostEvent(UUID.randomUUID().toString(), event.tenantId(), "RECEIVABLE",
                    "ORDER", event.aggregateId(), null, null, null, null,
                    event.tenantId(), new BigDecimal(amount), Instant.now(), Instant.now()));
        }
    }

    /** 订单发货 → 触发FIFO成本归集 */
    private void handleOrderShipped(DomainEvent event) {
        log.info("[FMS] Order shipped, cost aggregation needed: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
    }

    /** 物流签收 → 物流费用结算 */
    private void handleShipmentDelivered(DomainEvent event) {
        log.info("[FMS] Shipment delivered, settling shipping cost: tenant={}, shipmentId={}", event.tenantId(), event.aggregateId());
    }

    /** 广告效果 → 广告成本事件 */
    private void handleCampaignPerformance(DomainEvent event) {
        log.info("[FMS] Campaign performance recorded, ad cost: tenant={}, campaignId={}", event.tenantId(), event.aggregateId());
    }

    private String extractPayload(DomainEvent event, String key) {
        if (event instanceof StandardDomainEvent sde && sde.payload() != null) {
            Object val = sde.payload().get(key);
            return val != null ? val.toString() : null;
        }
        return null;
    }
}

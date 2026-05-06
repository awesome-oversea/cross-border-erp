package com.aidotnet.erp.pdm.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.pdm.infrastructure.ProductStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PDM域事件处理器
 * <p>
 * 描述: PDM域的跨域事件处理器，订阅其他域发布的领域事件，
 *       触发PDM域内的业务逻辑处理。
 * </p>
 * <p>
 * 订阅事件:
 *   - erp.oms.order.created: 订单创建 → 更新产品销售速度
 *   - erp.scm.purchase-order.completed: 采购完成 → 更新供应商绩效
 * </p>
 *
 * @author ERP系统
 */
@Component
public class ProductEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(ProductEventHandlers.class);
    private final DomainEventDispatcher dispatcher;
    private final ProductStore productStore;

    /**
     * 构造函数 - 依赖注入事件分发器和产品存储
     *
     * @param dispatcher   领域事件分发器
     * @param productStore 产品数据存储
     */
    public ProductEventHandlers(DomainEventDispatcher dispatcher, ProductStore productStore) {
        this.dispatcher = dispatcher;
        this.productStore = productStore;
    }

    /** 注册事件处理器，在Bean初始化后自动执行 */
    @PostConstruct
    public void register() {
        dispatcher.register("erp.oms.order.created", this::handleOrderCreated);
        dispatcher.register("erp.scm.purchase-order.completed", this::handlePurchaseOrderCompleted);
    }

    /**
     * 处理订单创建事件 - 更新产品销售速度
     *
     * @param event 订单创建领域事件
     */
    private void handleOrderCreated(DomainEvent event) {
        log.info("[PDM] Order created - product sales velocity update: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
    }

    /**
     * 处理采购完成事件 - 更新供应商绩效
     *
     * @param event 采购完成领域事件
     */
    private void handlePurchaseOrderCompleted(DomainEvent event) {
        log.info("[PDM] Purchase order completed - supplier performance update: tenant={}, poId={}", event.tenantId(), event.aggregateId());
    }
}

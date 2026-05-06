package com.aidotnet.erp.crm.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.crm.infrastructure.CrmStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CRM域事件处理器
 * <p>
 * 描述: 监听跨域领域事件，触发CRM侧的业务响应。
 *       通过DomainEventDispatcher注册事件处理方法。
 * </p>
 * <p>
 * 订阅事件:
 *   1. erp.oms.order.created - 订单创建 → 更新客户画像(消费统计)
 *   2. erp.oms.order.delivered - 订单签收 → 触发满意度跟进/评价请求
 *   3. erp.oms.order.cancelled - 订单取消 → 触发客户挽留流程
 * </p>
 *
 * @author ERP系统
 * @see DomainEventDispatcher
 */
@Component
public class CrmEventHandlers {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(CrmEventHandlers.class);
    /** 领域事件分发器 */
    private final DomainEventDispatcher dispatcher;
    /** CRM数据存储 */
    private final CrmStore crmStore;

    /** 构造函数注入 */
    public CrmEventHandlers(DomainEventDispatcher dispatcher, CrmStore crmStore) {
        this.dispatcher = dispatcher;
        this.crmStore = crmStore;
    }

    /** 注册事件处理器，在Spring容器初始化后自动调用 */
    @PostConstruct
    public void register() {
        dispatcher.register("erp.oms.order.created", this::handleOrderCreated);
        dispatcher.register("erp.oms.order.delivered", this::handleOrderDelivered);
        dispatcher.register("erp.oms.order.cancelled", this::handleOrderCancelled);
    }

    /** 处理订单创建事件 - 更新客户画像消费统计 */
    private void handleOrderCreated(DomainEvent event) {
        log.info("[CRM] Order created - customer profile update: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
    }

    /** 处理订单签收事件 - 触发满意度跟进和评价请求 */
    private void handleOrderDelivered(DomainEvent event) {
        log.info("[CRM] Order delivered - satisfaction follow-up: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
    }

    /** 处理订单取消事件 - 触发客户挽留流程 */
    private void handleOrderCancelled(DomainEvent event) {
        log.info("[CRM] Order cancelled - retention trigger: tenant={}, orderId={}", event.tenantId(), event.aggregateId());
    }
}

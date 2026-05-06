package com.aidotnet.erp.oms.domain;

import com.aidotnet.erp.common.event.DomainEvent;
import java.time.Instant;

/**
 * 订单创建事件
 * <p>
 * 描述: 订单创建时发布的领域事件，通知其他域新订单已入库。
 *       事件类型: oms.order.created
 * </p>
 * <p>
 * 订阅方:
 *   - WMS域: 触发库存预占
 *   - SCM域: 触发采购需求评估
 *   - PDM域: 更新产品销售速度
 *   - FMS域: 记录应收账款
 * </p>
 *
 * @author ERP系统
 */
public record OrderCreatedEvent(String eventId, String tenantId, String traceId, String aggregateId,
                                Instant occurredAt) implements DomainEvent {

    @Override
    public String eventType() {
        return "oms.order.created";
    }
}

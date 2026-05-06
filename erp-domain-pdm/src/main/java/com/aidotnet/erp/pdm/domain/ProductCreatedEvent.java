package com.aidotnet.erp.pdm.domain;

import com.aidotnet.erp.common.event.DomainEvent;
import java.time.Instant;

/**
 * 产品创建领域事件
 * <p>
 * 描述: SPU创建成功后发布的领域事件，通知其他域产品已创建。
 *       事件类型: pdm.product.created
 * </p>
 * <p>
 * 订阅方:
 *   - SOM域: 创建Listing关联
 *   - WMS域: 初始化库存记录
 *   - BI域: 更新产品统计
 * </p>
 *
 * @param eventId     事件唯一标识
 * @param tenantId    租户ID
 * @param traceId     链路追踪ID
 * @param aggregateId 聚合根ID(SPU ID)
 * @param occurredAt  事件发生时间
 * @author ERP系统
 */
public record ProductCreatedEvent(String eventId, String tenantId, String traceId, String aggregateId,
                                  Instant occurredAt) implements DomainEvent {

    @Override
    public String eventType() {
        return "pdm.product.created";
    }
}

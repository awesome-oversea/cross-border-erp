package com.aidotnet.erp.pdm.domain;

import com.aidotnet.erp.common.event.DomainEvent;
import java.time.Instant;

/**
 * 产品知识产权预警领域事件
 * <p>
 * 描述: 产品知识产权状态变更时发布的预警事件，通知相关域处理。
 *       事件类型: pdm.product.ip_alert
 * </p>
 * <p>
 * 触发场景:
 *   - 知识产权被标记为侵权(INFRINGING)
 *   - 知识产权即将到期
 * </p>
 * <p>
 * 订阅方:
 *   - SOM域: 下架侵权产品Listing
 *   - SYS域: 发送预警通知
 * </p>
 *
 * @param eventId     事件唯一标识
 * @param tenantId    租户ID
 * @param traceId     链路追踪ID
 * @param aggregateId 聚合根ID(SPU ID)
 * @param ipStatus    IP状态变更值
 * @param occurredAt  事件发生时间
 * @author ERP系统
 */
public record ProductIpAlertEvent(String eventId, String tenantId, String traceId, String aggregateId,
                                  String ipStatus, Instant occurredAt) implements DomainEvent {

    @Override
    public String eventType() {
        return "pdm.product.ip_alert";
    }
}

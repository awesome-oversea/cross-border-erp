package com.aidotnet.erp.som.domain;

import com.aidotnet.erp.common.event.DomainEvent;
import java.time.Instant;

/**
 * Listing上架事件
 * <p>
 * 描述: Listing上架时发布的领域事件，通知其他域Listing已上线。
 *       事件类型: som.listing.published
 * </p>
 * <p>
 * 订阅方:
 *   - WMS域: 触发库存预留
 *   - ADS域: 触发广告投放
 *   - BI域: 更新销售统计维度
 * </p>
 *
 * @author ERP系统
 */
public record ListingPublishedEvent(String eventId, String tenantId, String traceId,
                                    String aggregateId, String platform, String marketplace,
                                    Instant occurredAt) implements DomainEvent {

    @Override
    public String eventType() {
        return "som.listing.published";
    }
}

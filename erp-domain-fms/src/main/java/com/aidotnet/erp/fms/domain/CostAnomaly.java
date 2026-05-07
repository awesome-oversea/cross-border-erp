package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 成本异常/归集建议
 * <p>
 * 描述: 承接 PMS 输出的 AI 成本异常分析结果，在 FMS 内形成待审批业务对象。
 * 财务审批通过后，异常建议会落地为正式成本事件，并可生成归集结果，进入利润核算闭环。
 * </p>
 */
public record CostAnomaly(
        String anomalyId,
        String tenantId,
        String erpReferenceId,
        String idempotencyKey,
        String anomalyType,
        String sourceType,
        String sourceId,
        String dimensionType,
        String dimensionId,
        String sellerSku,
        String storeId,
        String channelCode,
        String marketplaceId,
        String costType,
        BigDecimal suggestedAmount,
        String currency,
        boolean autoAggregate,
        String reason,
        Map<String, Object> evidence,
        String status,
        String submittedBy,
        String submittedActorType,
        String approvedBy,
        Instant approvedAt,
        String effectiveCostEventId,
        List<String> allocationResultIds,
        String traceId,
        String purpose,
        String rawScope,
        Instant createdAt,
        Instant updatedAt
) {
    public enum Status {
        PENDING_APPROVAL,
        APPLIED,
        REJECTED
    }
}

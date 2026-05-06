package com.aidotnet.erp.oms.domain;

import java.time.Instant;

/**
 * 买家黑名单领域模型
 * <p>
 * 描述: 买家黑名单记录，用于拦截高风险买家的订单。
 * </p>
 *
 * @param entryId   记录唯一标识
 * @param tenantId  租户ID
 * @param buyerName 买家名称
 * @param reason    拉黑原因
 * @param createdAt 创建时间
 * @author ERP系统
 */
public record BuyerBlacklistEntry(
        String entryId,
        String tenantId,
        String buyerName,
        String reason,
        Instant createdAt
) {}

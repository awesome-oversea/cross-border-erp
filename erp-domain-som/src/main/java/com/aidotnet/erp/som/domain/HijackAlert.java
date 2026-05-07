package com.aidotnet.erp.som.domain;

import java.time.Instant;

/**
 * 跟卖监控与提醒领域模型
 * <p>
 * 描述: 监控Amazon等平台Listing的跟卖情况，自动检测跟卖者并通知运营人员。
 * </p>
 * <p>
 * 业务规则:
 *   1. 检测到新的跟卖者时生成HijackAlert记录
 *   2. 严重程度根据跟卖者数量和价格差异判断
 *   3. 运营确认处理后关闭告警
 * </p>
 *
 * @author ERP系统
 */
public record HijackAlert(
        String alertId,
        String tenantId,
        String listingId,
        String platform,
        String marketplace,
        /** 跟卖者名称 */
        String hijackerName,
        /** 跟卖价格 */
        java.math.BigDecimal hijackerPrice,
        /** 我方售价 */
        java.math.BigDecimal ourPrice,
        /** 严重程度: LOW/MEDIUM/HIGH/CRITICAL */
        String severity,
        /** 告警状态: OPEN/ACKNOWLEDGED/RESOLVED */
        String status,
        /** 处理人 */
        String handledBy,
        /** 处理备注 */
        String handleNote,
        Instant detectedAt,
        Instant handledAt,
        Instant createdAt,
        Instant updatedAt
) {}

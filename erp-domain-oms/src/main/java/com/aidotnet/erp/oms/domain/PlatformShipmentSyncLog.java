package com.aidotnet.erp.oms.domain;

import java.time.Instant;

/**
 * 平台发货同步日志领域模型
 * <p>
 * 描述: 记录向平台同步发货信息的日志，包含同步状态和错误信息。
 *       支持重试机制，记录尝试次数。
 * </p>
 *
 * @author ERP系统
 */
public record PlatformShipmentSyncLog(
        String logId,
        String tenantId,
        String orderId,
        String packageId,
        String platform,
        String platformOrderNo,
        String trackingNo,
        PlatformShipmentSyncStatus status,
        int attemptNo,
        String errorMessage,
        Instant syncedAt
) {}

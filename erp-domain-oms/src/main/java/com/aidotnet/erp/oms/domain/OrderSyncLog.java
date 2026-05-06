package com.aidotnet.erp.oms.domain;

import java.time.Instant;

/**
 * 订单同步日志领域模型
 * <p>
 * 描述: 平台订单拉取同步日志，记录每次同步的执行状态和结果。
 * </p>
 *
 * @author ERP系统
 */
public record OrderSyncLog(
        String syncId,
        String tenantId,
        String platform,
        String syncType,
        String status,
        int syncedCount,
        int failedCount,
        String errorMessage,
        Instant startedAt,
        Instant completedAt
) {
    /** 同步类型 */
    public enum SyncType {
        /** 自动同步 */
        AUTO,
        /** 手动同步 */
        MANUAL
    }

    /** 同步状态 */
    public enum SyncStatus {
        /** 执行中 */
        RUNNING,
        /** 已完成 */
        COMPLETED,
        /** 失败 */
        FAILED
    }
}

package com.aidotnet.erp.bi.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 自定义报表。
 * <p>
 * 业务语义:
 * 1. 报表编码在租户内唯一，用于稳定引用和权限收敛。
 * 2. 维度、指标、筛选和排序配置由 BI 域主控，运行时生成快照而不是直接暴露底层查询细节。
 * 3. lastRunSnapshotId/lastRunAt 记录最近一次执行结果，便于导出、审计和复盘。
 * </p>
 */
public record CustomReport(
        String reportId,
        String tenantId,
        String reportCode,
        String reportName,
        String subjectArea,
        List<String> dimensions,
        List<String> metrics,
        Map<String, Object> filters,
        List<String> sorts,
        String visibility,
        String permissionCode,
        String dataLevel,
        String description,
        boolean enabled,
        String lastRunSnapshotId,
        Instant lastRunAt,
        Instant createdAt,
        Instant updatedAt) {}

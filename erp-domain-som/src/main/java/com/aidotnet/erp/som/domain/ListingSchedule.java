package com.aidotnet.erp.som.domain;

import java.time.Instant;

/**
 * Listing定时上下架计划
 * <p>
 * 描述: 为Listing设置定时上架/下架/调价的自动化任务。
 *       运营人员可预先设置时间点，系统在指定时间自动执行。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一Listing在同一时间点只能有一个待执行计划
 *   2. 已执行的计划不可修改
 *   3. 过期未执行的计划自动标记为MISSED
 * </p>
 *
 * @author ERP系统
 */
public record ListingSchedule(
        String scheduleId,
        String tenantId,
        String listingId,
        /** 操作类型: PUBLISH/UNPUBLISH/ARCHIVE/ADJUST_PRICE */
        String actionType,
        /** 计划执行时间 */
        Instant scheduledAt,
        /** 调价目标价(ADJUST_PRICE时使用) */
        java.math.BigDecimal targetPrice,
        /** 状态: PENDING/EXECUTED/MISSED/CANCELLED */
        String status,
        Instant executedAt,
        String executedBy,
        Instant createdAt,
        Instant updatedAt
) {}

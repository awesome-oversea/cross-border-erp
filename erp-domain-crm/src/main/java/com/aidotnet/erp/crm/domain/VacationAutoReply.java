package com.aidotnet.erp.crm.domain;

import java.time.Instant;

/**
 * 假期自动回复领域模型
 * <p>
 * 描述: 假期自动回复配置，在指定时间段内自动回复客户消息。
 * </p>
 *
 * @author ERP系统
 */
public record VacationAutoReply(
        String vacationId,
        String tenantId,
        Instant startDate,
        Instant endDate,
        String replyContent,
        String language,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}

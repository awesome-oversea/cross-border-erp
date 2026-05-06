package com.aidotnet.erp.crm.domain;

import java.time.Instant;

/**
 * 邮件营销活动领域模型
 * <p>
 * 描述: 邮件营销活动，按客户分群定向发送。
 * </p>
 *
 * @author ERP系统
 */
public record EmailCampaign(
        String campaignId,
        String tenantId,
        String name,
        String subject,
        String content,
        String targetSegment,
        String status,
        Instant sentAt,
        Instant createdAt,
        Instant updatedAt
) {}

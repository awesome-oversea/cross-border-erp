package com.aidotnet.erp.ads.domain;

import java.time.Instant;

/**
 * 否定关键词领域模型
 * <p>
 * 描述: 广告管理中的否定关键词，用于排除无效点击和避免浪费广告预算。
 *       支持词组否定和精准否定两种匹配类型。
 * </p>
 * <p>
 * 业务规则:
 *   1. 添加否定关键词后，广告不会在包含该词的搜索中展示
 *   2. 搜索词分析中 HIGH_SPEND_LOW_RETURN 的词自动建议设为否定
 *   3. 否定关键词适用于整个广告活动或特定广告组
 * </p>
 *
 * @author ERP系统
 */
public record NegativeKeyword(
        String negativeKeywordId,
        String tenantId,
        String campaignId,
        String adGroupId,
        String keywordText,
        /** 匹配类型: NEGATIVE_PHRASE(词组否定)/NEGATIVE_EXACT(精准否定) */
        String matchType,
        /** 来源: MANUAL(手动添加)/AUTO_HARVEST(自动提炼)/PMS_SUGGESTION(PMS建议) */
        String source,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}

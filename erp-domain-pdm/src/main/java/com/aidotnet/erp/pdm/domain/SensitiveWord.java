package com.aidotnet.erp.pdm.domain;

import java.time.Instant;

/**
 * 敏感词领域模型
 * <p>
 * 描述: 产品合规敏感词实体，用于产品上架前的文本合规检查。
 *       支持多语言、多类别的敏感词管理，可启用/禁用。
 * </p>
 * <p>
 * 敏感词类别:
 *   - IP_RISK: 知识产权风险词，如仿牌关键词
 *   - TRADEMARK: 商标保护词，如注册商标名
 *   - REGULATORY: 法规限制词，如违禁品描述
 *   - PLATFORM_POLICY: 平台政策词，如平台禁止用语
 *   - CULTURAL: 文化敏感词，如宗教、政治敏感词
 * </p>
 *
 * @param wordId    敏感词唯一标识
 * @param tenantId  租户ID
 * @param word      敏感词内容
 * @param category  类别: IP_RISK/TRADEMARK/REGULATORY/PLATFORM_POLICY/CULTURAL
 * @param language  语言代码，如 zh、en、ja，null表示全语言
 * @param enabled   是否启用，禁用后不参与检查
 * @param createdAt 创建时间
 * @author ERP系统
 */
public record SensitiveWord(
        String wordId,
        String tenantId,
        String word,
        String category,
        String language,
        boolean enabled,
        Instant createdAt
) {
    /** 敏感词类别枚举 */
    public enum Category {
        /** 知识产权风险 */
        IP_RISK,
        /** 商标保护 */
        TRADEMARK,
        /** 法规限制 */
        REGULATORY,
        /** 平台政策 */
        PLATFORM_POLICY,
        /** 文化敏感 */
        CULTURAL
    }
}

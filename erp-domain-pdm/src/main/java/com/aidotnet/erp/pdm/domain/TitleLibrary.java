package com.aidotnet.erp.pdm.domain;

import java.time.Instant;

/**
 * 标题库领域模型
 * <p>
 * 描述: 产品上架前配置的标题库，运营可维护多个标题模板，
 *       刊登时系统自动随机调用，减少关联风险。
 * </p>
 * <p>
 * 业务规则:
 *   1. 标题按语言分组，支持多语种站点
 *   2. 同一SPU下的多个标题，刊登时随机匹配
 *   3. 已禁用标题不会被自动调用
 * </p>
 *
 * @author ERP系统
 */
public record TitleLibrary(
        String titleId,
        String tenantId,
        String spuId,
        String title,
        String language,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}

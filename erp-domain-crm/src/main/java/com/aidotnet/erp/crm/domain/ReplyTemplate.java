package com.aidotnet.erp.crm.domain;

import java.time.Instant;

/**
 * 回复模板领域模型
 * <p>
 * 描述: 客服回复模板，按类别和语言分类，支持多语言。
 * </p>
 *
 * @author ERP系统
 */
public record ReplyTemplate(String templateId, String tenantId, String name, String category, String content,
                            String language, boolean enabled, Instant createdAt, Instant updatedAt) {}

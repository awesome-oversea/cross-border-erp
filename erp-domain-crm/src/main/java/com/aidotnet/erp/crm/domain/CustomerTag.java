package com.aidotnet.erp.crm.domain;

import java.time.Instant;

/**
 * 客户标签领域模型
 * <p>
 * 描述: 客户标签，用于客户分群和精准营销。
 * </p>
 *
 * @author ERP系统
 */
public record CustomerTag(String tagId, String tenantId, String customerId, String tagName, String tagValue,
                          Instant taggedAt) {}

package com.aidotnet.erp.som.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * PMS智能Listing建议领域模型
 * <p>
 * 描述: PMS(Product Management System)智能服务生成的Listing优化建议，
 *       包括标题、描述、五点描述和价格建议。支持应用/拒绝操作。
 * </p>
 * <p>
 * 状态流转: pending(待处理) → applied(已应用)/rejected(已拒绝)
 * </p>
 *
 * @param suggestionId         建议唯一标识
 * @param tenantId             租户ID
 * @param listingId            关联Listing ID
 * @param suggestionType       建议类型
 * @param titleSuggestion      标题建议
 * @param descriptionSuggestion 描述建议
 * @param bulletPointsSuggestion 五点描述建议
 * @param priceSuggestion      价格建议
 * @param reason               建议原因
 * @param confidence           置信度
 * @param traceId              链路追踪ID
 * @param idempotencyKey       幂等键
 * @param status               状态: pending/applied/rejected
 * @param createdAt            创建时间
 * @author ERP系统
 */
public record PmsListingSuggestion(String suggestionId, String tenantId, String listingId,
                                   String suggestionType, String titleSuggestion,
                                   String descriptionSuggestion, String bulletPointsSuggestion,
                                   BigDecimal priceSuggestion, String reason,
                                   String confidence, String traceId, String idempotencyKey,
                                   String status, Instant createdAt) {}

package com.aidotnet.erp.som.domain;

import java.time.Instant;

/**
 * Listing优化记录领域模型
 * <p>
 * 描述: 记录Listing的优化操作历史，包括优化类型、前后值和结果，
 *       用于追踪优化效果和回溯变更。
 * </p>
 *
 * @param optimizationId 优化记录唯一标识
 * @param tenantId       租户ID
 * @param listingId      关联Listing ID
 * @param type           优化类型: TITLE/DESCRIPTION/PRICE/KEYWORDS/IMAGES/BULLET_POINTS
 * @param beforeValue    优化前值
 * @param afterValue     优化后值
 * @param operator       操作人
 * @param result         优化结果描述
 * @param createdAt      创建时间
 * @author ERP系统
 */
public record ListingOptimization(String optimizationId, String tenantId, String listingId,
                                  OptimizationType type, String beforeValue, String afterValue,
                                  String operator, String result, Instant createdAt) {}

package com.aidotnet.erp.scm.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 供应商综合评分领域模型
 * <p>
 * 描述: 供应商历史评估汇总评分，包含四维评分和评估次数。
 * </p>
 *
 * @author ERP系统
 */
public record SupplierScore(String scoreId, String tenantId, String supplierId, BigDecimal qualityScore,
                            BigDecimal deliveryScore, BigDecimal priceScore, BigDecimal serviceScore,
                            BigDecimal overallScore, int evaluationCount, Instant scoredAt) {}

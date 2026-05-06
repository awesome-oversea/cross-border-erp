package com.aidotnet.erp.scm.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 供应商评估领域模型
 * <p>
 * 描述: 供应商单次评估记录，包含质量、交期、价格、服务四个维度评分。
 * </p>
 *
 * @author ERP系统
 */
public record SupplierEvaluation(
        String evaluationId,
        String tenantId,
        String supplierId,
        BigDecimal qualityScore,
        BigDecimal deliveryScore,
        BigDecimal priceScore,
        BigDecimal serviceScore,
        BigDecimal overallScore,
        String comment,
        Instant evaluatedAt
) {}

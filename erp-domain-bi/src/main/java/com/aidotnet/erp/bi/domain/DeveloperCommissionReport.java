package com.aidotnet.erp.bi.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 开发人员提成报表。
 * <p>
 * 业务语义:
 * 1. 提成报表沉淀开发人员在指定周期内的开发产出、出单表现、利润贡献和 KPI 得分。
 * 2. orderRate 由 skuCount 与 orderCount 推导，避免前端重复计算口径。
 * 3. commissionCoefficient 和 commissionAmount 由 BI 域统一计算，确保提成口径一致。
 * </p>
 */
public record DeveloperCommissionReport(
        String reportId,
        String tenantId,
        String userId,
        String userName,
        String teamCode,
        String period,
        int skuCount,
        int orderCount,
        BigDecimal orderRate,
        BigDecimal salesProfit,
        BigDecimal kpiScore,
        BigDecimal baseCommissionRate,
        BigDecimal commissionCoefficient,
        BigDecimal commissionAmount,
        String currency,
        Instant createdAt) {}

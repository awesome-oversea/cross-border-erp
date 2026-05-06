package com.aidotnet.erp.pdm.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 选品建议领域模型
 * <p>
 * 描述: AI选品建议实体，承载AI分析产生的选品推荐和人工选品建议。
 *       支持市场分析、利润预估、风险评估等结构化信息。
 * </p>
 * <p>
 * 业务规则:
 *   1. 选品建议创建后为DRAFT状态，需提交审核
 *   2. 审核通过后可创建产品开发流程
 *   3. AI建议(aiSuggested=true)需人工审核确认
 *   4. 状态流转: DRAFT → SUBMITTED → APPROVED/REJECTED
 * </p>
 *
 * @param proposalId      建议唯一标识
 * @param tenantId        租户ID
 * @param productName     产品名称
 * @param title           建议标题
 * @param categoryId      目标类目ID
 * @param source          来源: AI_SUGGESTION(AI建议)、MANUAL(人工)、MARKET_RESEARCH(市场调研)
 * @param sourceReference 来源参考，如竞品链接、市场报告ID
 * @param estimatedCost   预估成本
 * @param marketAnalysis  市场分析，JSON格式
 * @param profitEstimation 利润预估，JSON格式
 * @param riskAssessment  风险评估，JSON格式
 * @param aiSuggested     是否AI建议
 * @param status          状态: DRAFT/SUBMITTED/APPROVED/REJECTED
 * @param submittedBy     提交人
 * @param reviewedBy      审核人
 * @param reviewComment   审核意见
 * @param createdAt       创建时间
 * @param updatedAt       更新时间
 * @author ERP系统
 */
public record SelectionProposal(
        String proposalId,
        String tenantId,
        String productName,
        String title,
        String categoryId,
        String source,
        String sourceReference,
        BigDecimal estimatedCost,
        String marketAnalysis,
        String profitEstimation,
        String riskAssessment,
        boolean aiSuggested,
        ProposalStatus status,
        String submittedBy,
        String reviewedBy,
        String reviewComment,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * 判断建议是否可以审核
     *
     * @return true-可审核，false-不可审核
     */
    public boolean canApprove() {
        return status == ProposalStatus.SUBMITTED;
    }

    /**
     * 判断建议是否可以提交审核
     *
     * @return true-可提交，false-不可提交
     */
    public boolean canSubmit() {
        return status == ProposalStatus.DRAFT;
    }
}

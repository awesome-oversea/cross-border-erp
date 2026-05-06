package com.aidotnet.erp.pdm.domain;

import java.time.Instant;

/**
 * PMS选品建议领域模型
 * <p>
 * 描述: PMS(AI选品系统)推送的选品建议实体，承载AI分析产生的
 *       市场分析、利润预估和风险评估结果。通过事件驱动机制
 *       从PMS系统接收，可转化为内部选品建议(SelectionProposal)。
 * </p>
 * <p>
 * 业务规则:
 *   1. 通过idempotencyKey保证消息幂等性
 *   2. confidence为AI置信度评分(0-100)
 *   3. 接收后可自动创建选品建议流程
 * </p>
 *
 * @param suggestionId    建议唯一标识
 * @param tenantId        租户ID
 * @param productName     产品名称
 * @param categoryId      目标类目ID
 * @param sourceMarketplace 来源市场，如 Amazon US、Shopee TH
 * @param sourceUrl       来源URL
 * @param marketAnalysis  市场分析，JSON格式
 * @param profitEstimation 利润预估，JSON格式
 * @param riskAssessment  风险评估，JSON格式
 * @param confidence      AI置信度(0-100)
 * @param traceId         链路追踪ID
 * @param idempotencyKey  幂等键，防止重复处理
 * @param createdAt       创建时间
 * @author ERP系统
 */
public record PmsSelectionSuggestion(
        String suggestionId,
        String tenantId,
        String productName,
        String categoryId,
        String sourceMarketplace,
        String sourceUrl,
        String marketAnalysis,
        String profitEstimation,
        String riskAssessment,
        String confidence,
        String traceId,
        String idempotencyKey,
        Instant createdAt
) {}

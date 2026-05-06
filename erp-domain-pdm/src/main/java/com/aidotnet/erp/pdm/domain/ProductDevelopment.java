package com.aidotnet.erp.pdm.domain;

import java.time.Instant;

/**
 * 产品开发领域模型
 * <p>
 * 描述: 产品开发流程实体，跟踪从选品建议到产品上架的全过程。
 *       支持多阶段开发流程，分配开发人员、编辑和设计人员。
 * </p>
 * <p>
 * 业务规则:
 *   1. 开发流程关联选品建议(proposalId)和产品SPU(spuId)
 *   2. 开发阶段(stage): RESEARCH(调研) → SAMPLING(打样) → TESTING(测试) → LISTING(上架)
 *   3. 优先级(priority): 1-10，数值越大优先级越高
 *   4. 状态(status): active(进行中)、completed(已完成)、cancelled(已取消)
 * </p>
 *
 * @param devId      开发记录唯一标识
 * @param tenantId   租户ID
 * @param spuId      关联SPU ID
 * @param proposalId 关联选品建议ID
 * @param stage      开发阶段: RESEARCH/SAMPLING/TESTING/LISTING
 * @param stageNote  阶段备注
 * @param developer  开发人员
 * @param editor     编辑人员
 * @param designer   设计人员
 * @param priority   优先级(1-10)
 * @param status     状态: active/completed/cancelled
 * @param createdAt  创建时间
 * @param updatedAt  更新时间
 * @author ERP系统
 */
public record ProductDevelopment(
        String devId,
        String tenantId,
        String spuId,
        String proposalId,
        DevStage stage,
        String stageNote,
        String developer,
        String editor,
        String designer,
        int priority,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}

package com.aidotnet.erp.pdm.domain;

/**
 * 选品建议状态枚举
 * <p>
 * 描述: 定义选品建议的生命周期状态
 * </p>
 * <p>
 * 状态流转: DRAFT → SUBMITTED → APPROVED/REJECTED → CONVERTED
 * </p>
 */
public enum ProposalStatus {
    /** 草稿 - 选品建议已创建但未提交 */
    DRAFT,
    /** 已提交 - 选品建议已提交等待审核 */
    SUBMITTED,
    /** 已批准 - 审核通过，可创建产品开发流程 */
    APPROVED,
    /** 已拒绝 - 审核未通过 */
    REJECTED,
    /** 已转化 - 已转化为产品开发流程 */
    CONVERTED
}

package com.aidotnet.erp.fba.domain;

/** FBA入库计划状态 */
public enum FbaInboundPlanStatus {
    /** 草稿 */
    DRAFT,
    /** 已提交 */
    SUBMITTED,
    /** 已拆分 */
    SPLIT,
    /** 已取消 */
    CANCELLED,
    /** 已完成 */
    COMPLETED
}

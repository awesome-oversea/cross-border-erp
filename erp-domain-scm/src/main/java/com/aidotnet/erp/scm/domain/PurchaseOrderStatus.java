package com.aidotnet.erp.scm.domain;

/** 采购订单状态 */
public enum PurchaseOrderStatus {
    /** 草稿 */
    DRAFT,
    /** 已提交 - 等待审批 */
    SUBMITTED,
    /** 已审批 - 可收货 */
    APPROVED,
    /** 部分收货 */
    PARTIALLY_RECEIVED,
    /** 已收货 */
    RECEIVED,
    /** 已取消 */
    CANCELLED
}

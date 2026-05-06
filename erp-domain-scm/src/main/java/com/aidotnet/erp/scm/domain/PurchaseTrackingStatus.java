package com.aidotnet.erp.scm.domain;

/** 采购跟踪状态 */
public enum PurchaseTrackingStatus {
    /** 待收货 */
    PENDING_RECEIPT,
    /** 部分收货 */
    PARTIALLY_RECEIVED,
    /** 全部收货 */
    FULLY_RECEIVED,
    /** 有异常关闭 */
    CLOSED_WITH_EXCEPTION
}

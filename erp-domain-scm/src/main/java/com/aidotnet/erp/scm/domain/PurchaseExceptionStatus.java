package com.aidotnet.erp.scm.domain;

/** 采购异常状态 */
public enum PurchaseExceptionStatus {
    /** 待处理 */
    PENDING,
    /** 处理中 */
    PROCESSING,
    /** 已解决 */
    RESOLVED,
    /** 已拒绝 */
    REJECTED
}

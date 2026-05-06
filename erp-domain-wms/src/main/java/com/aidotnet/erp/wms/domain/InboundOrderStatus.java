package com.aidotnet.erp.wms.domain;

/** 入库单状态 */
public enum InboundOrderStatus {
    /** 待入库 */
    PENDING,
    /** 入库中 */
    RECEIVING,
    /** 已完成 */
    COMPLETED,
    /** 已取消 */
    CANCELLED
}

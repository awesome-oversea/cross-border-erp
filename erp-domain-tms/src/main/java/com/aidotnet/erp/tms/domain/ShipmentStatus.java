package com.aidotnet.erp.tms.domain;

/** 发货单状态 */
public enum ShipmentStatus {
    /** 待发货 */
    PENDING,
    /** 已创建 */
    CREATED,
    /** 在途 */
    IN_TRANSIT,
    /** 已签收 */
    DELIVERED,
    /** 已取消 */
    CANCELLED
}

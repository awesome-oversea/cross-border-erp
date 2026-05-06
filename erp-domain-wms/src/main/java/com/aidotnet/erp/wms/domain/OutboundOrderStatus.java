package com.aidotnet.erp.wms.domain;

/** 出库单状态 */
public enum OutboundOrderStatus {
    /** 待出库 */
    PENDING,
    /** 拣货中 */
    PICKING,
    /** 已发货 */
    SHIPPED,
    /** 已取消 */
    CANCELLED
}

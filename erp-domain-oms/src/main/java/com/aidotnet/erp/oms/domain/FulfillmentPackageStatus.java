package com.aidotnet.erp.oms.domain;

/** 履约包裹状态 */
public enum FulfillmentPackageStatus {
    /** 待发货 - 库存已分配 */
    READY,
    /** 等待库存 - 库存不足 */
    WAITING_INVENTORY,
    /** 等待承运商 - 等待物流取件 */
    WAITING_CARRIER,
    /** 已发货 */
    SHIPPED
}

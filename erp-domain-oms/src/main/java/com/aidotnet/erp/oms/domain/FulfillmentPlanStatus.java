package com.aidotnet.erp.oms.domain;

/** 履约计划状态 */
public enum FulfillmentPlanStatus {
    /** 已计划 - 初始状态 */
    PLANNED,
    /** 部分分配 - 部分包裹已分配库存 */
    PARTIALLY_ALLOCATED,
    /** 异常 - 履约异常(如库存不足) */
    EXCEPTION,
    /** 部分发货 - 部分包裹已发货 */
    PARTIALLY_SHIPPED,
    /** 已发货 - 所有包裹已发货 */
    SHIPPED
}

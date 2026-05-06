package com.aidotnet.erp.wms.domain;

/** 移动类型 */
public enum MovementType {
    /** 调拨 */
    TRANSFER,
    /** 收货上架 */
    RECEIVING,
    /** 拣货下架 */
    PICKING,
    /** 退货入库 */
    RETURN,
    /** 调整移位 */
    ADJUSTMENT
}

package com.aidotnet.erp.som.domain;

/** 店铺状态枚举 */
public enum StoreStatus {
    /** 已创建 - 店铺信息已录入，未连接平台 */
    CREATED,
    /** 已连接 - 已通过平台API授权连接 */
    CONNECTED,
    /** 已停用 - 店铺暂停使用 */
    DISABLED
}

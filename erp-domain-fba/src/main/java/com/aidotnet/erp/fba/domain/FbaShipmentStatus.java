package com.aidotnet.erp.fba.domain;

/** FBA发货单状态 */
public enum FbaShipmentStatus {
    /** 草稿 */
    DRAFT,
    /** 已提交 */
    SUBMITTED,
    /** 已打包 */
    PACKED,
    /** 已发货 */
    SHIPPED,
    /** 已接收 */
    RECEIVED,
    /** 已关闭 */
    CLOSED,
    /** 已取消 */
    CANCELLED
}

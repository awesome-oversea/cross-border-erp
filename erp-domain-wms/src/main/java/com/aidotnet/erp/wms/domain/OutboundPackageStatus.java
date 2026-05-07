package com.aidotnet.erp.wms.domain;

/**
 * 出库包裹状态
 */
public enum OutboundPackageStatus {
    /** 已完成打包，待称重 */
    PACKED,
    /** 已称重，待发货 */
    WEIGHED,
    /** 已发货 */
    SHIPPED
}

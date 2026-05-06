package com.aidotnet.erp.scm.domain;

/** 采购异常类型 */
public enum PurchaseExceptionType {
    /** 超交 - 实际收货超过订购量 */
    OVER_DELIVERED,
    /** 短交 - 实际收货少于订购量 */
    UNDER_DELIVERED,
    /** 涨价 - 实际单价高于采购单价 */
    PRICE_INCREASED,
    /** 降价 - 实际单价低于采购单价 */
    PRICE_DECREASED,
    /** 损坏 - 收到损坏商品 */
    DAMAGED,
    /** 错发 - 收到错误商品 */
    WRONG_ITEM,
    /** 缺失 - 缺少商品 */
    MISSING_ITEM,
    /** 延迟 - 交货延迟 */
    DELAYED
}

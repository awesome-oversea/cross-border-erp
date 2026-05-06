package com.aidotnet.erp.oms.domain;

/**
 * 订单状态枚举
 * <p>
 * 状态流转:
 *   正常流程: PENDING → CREATED → CONFIRMED → PAID → SHIPPED → DELIVERED
 *   审核分支: → REVIEW_REQUIRED → REVIEW_REJECTED
 *   取消分支: PENDING/CONFIRMED → CANCELLED
 *   退货分支: DELIVERED → RETURN_REQUESTED → RETURNED
 *   退款分支: → REFUND_REQUESTED → PARTIAL_REFUNDED/REFUNDED
 * </p>
 */
public enum OrderStatus {
    /** 待处理 - 刚从平台拉取 */
    PENDING,
    /** 已创建 - 订单信息已入库 */
    CREATED,
    /** 已确认 - 风控通过，等待付款 */
    CONFIRMED,
    /** 待审核 - 触发风控规则 */
    REVIEW_REQUIRED,
    /** 审核拒绝 */
    REVIEW_REJECTED,
    /** 已付款 - 付款确认 */
    PAID,
    /** 已取消 */
    CANCELLED,
    /** 已发货 */
    SHIPPED,
    /** 已签收 */
    DELIVERED,
    /** 退货申请中 */
    RETURN_REQUESTED,
    /** 已退货 */
    RETURNED,
    /** 退款申请中 */
    REFUND_REQUESTED,
    /** 部分退款 */
    PARTIAL_REFUNDED,
    /** 已退款 */
    REFUNDED
}

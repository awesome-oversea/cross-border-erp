package com.aidotnet.erp.oms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 销售订单领域模型
 * <p>
 * 描述: OMS域核心实体，表示一个跨境电商销售订单。包含订单基本信息、
 *       订单行、促销信息和风险等级。是订单履约的起点。
 * </p>
 * <p>
 * 状态流转: PENDING → CREATED → CONFIRMED → PAID → SHIPPED → DELIVERED
 *           也可进入: REVIEW_REQUIRED → REVIEW_REJECTED
 *           退货分支: DELIVERED → RETURN_REQUESTED → RETURNED → REFUND_REQUESTED → REFUNDED
 *           取消分支: PENDING/CONFIRMED → CANCELLED
 * </p>
 *
 * @param orderId         订单唯一标识
 * @param tenantId        租户ID
 * @param listingId       关联Listing ID
 * @param storeId         关联店铺ID
 * @param platform        销售平台
 * @param marketplace     市场站点
 * @param platformOrderNo 平台订单号
 * @param buyerName       买家姓名
 * @param customerId      客户ID
 * @param countryCode     目的国家代码
 * @param shippingAddress 收货地址
 * @param currency        币种
 * @param totalAmount     订单总金额
 * @param taxAmount       税额
 * @param shippingAmount  运费
 * @param discountAmount  折扣金额
 * @param status          订单状态
 * @param paymentStatus   支付状态
 * @param fulfillmentStatus 履约状态
 * @param riskLevel       风险等级: safe/risky
 * @param profitMargin    利润率
 * @param lines           订单行列表
 * @param promotions      促销信息列表
 * @param orderDate       下单时间
 * @param createdAt       创建时间
 * @param updatedAt       更新时间
 * @author ERP系统
 */
public record SalesOrder(
        String orderId,
        String tenantId,
        String listingId,
        String storeId,
        String platform,
        String marketplace,
        String platformOrderNo,
        String buyerName,
        String customerId,
        String countryCode,
        String shippingAddress,
        String currency,
        BigDecimal totalAmount,
        BigDecimal taxAmount,
        BigDecimal shippingAmount,
        BigDecimal discountAmount,
        OrderStatus status,
        String paymentStatus,
        String fulfillmentStatus,
        String riskLevel,
        BigDecimal profitMargin,
        List<OrderLine> lines,
        List<Promotion> promotions,
        Instant orderDate,
        Instant createdAt,
        Instant updatedAt
) {
    /** 是否可退款(已发货或已签收) */
    public boolean canRefund() {
        return status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED;
    }

    /** 是否可取消(待处理或已确认) */
    public boolean canCancel() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    /** 是否高风险(风险等级为risky或利润率低于5%) */
    public boolean isRisky() {
        return "risky".equals(riskLevel) || (profitMargin != null && profitMargin.compareTo(BigDecimal.valueOf(0.05)) < 0);
    }
}

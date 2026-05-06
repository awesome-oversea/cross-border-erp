package com.aidotnet.erp.oms.domain;

import java.math.BigDecimal;

/**
 * 订单行领域模型
 * <p>
 * 描述: 销售订单中的单个商品行，记录SKU、数量、单价和成本信息。
 * </p>
 *
 * @param lineId           订单行唯一标识
 * @param productId        产品ID
 * @param sellerSku        卖家SKU编码
 * @param title            商品标题
 * @param quantity         购买数量
 * @param unitPrice        单价
 * @param totalPrice       行总价
 * @param taxAmount        税额
 * @param estimatedUnitCost 预估单位成本
 * @author ERP系统
 */
public record OrderLine(
        String lineId,
        String productId,
        String sellerSku,
        String title,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        BigDecimal taxAmount,
        BigDecimal estimatedUnitCost
) {
    /** 计算行总价，totalPrice为空时按单价×数量计算 */
    public BigDecimal lineTotal() {
        return totalPrice != null ? totalPrice : unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

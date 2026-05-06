package com.aidotnet.erp.oms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 促销信息领域模型
 * <p>
 * 描述: 订单关联的促销信息，记录优惠券、折扣、捆绑销售等促销类型。
 * </p>
 *
 * @author ERP系统
 */
public record Promotion(
        String promoId,
        String tenantId,
        String orderId,
        String promoType,
        String promoCode,
        BigDecimal discount,
        String description,
        Instant appliedAt
) {
    /** 促销类型 */
    public enum PromoType {
        /** 优惠券 */
        COUPON,
        /** 折扣 */
        DISCOUNT,
        /** 捆绑销售 */
        BUNDLE,
        /** 限时促销 */
        FLASH_SALE,
        /** 平台促销 */
        PLATFORM_PROMO
    }
}

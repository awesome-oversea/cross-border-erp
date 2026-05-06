package com.aidotnet.erp.oms.domain;

import java.math.BigDecimal;

/**
 * 履约包裹行领域模型
 * <p>
 * 描述: 履约包裹中的商品行，记录包裹内每个SKU的数量和金额。
 * </p>
 *
 * @author ERP系统
 */
public record OrderFulfillmentPackageLine(
        String packageLineId,
        String orderLineId,
        String sellerSku,
        String title,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineAmount
) {}

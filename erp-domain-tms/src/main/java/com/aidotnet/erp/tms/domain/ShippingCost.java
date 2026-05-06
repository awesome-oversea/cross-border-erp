package com.aidotnet.erp.tms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 运费成本领域模型
 * <p>
 * 描述: 发货单的运费成本明细，包含运费、燃油附加费、其他费用和总成本。
 * </p>
 *
 * @author ERP系统
 */
public record ShippingCost(
        String costId,
        String tenantId,
        String shipmentId,
        String carrierId,
        BigDecimal freightCost,
        BigDecimal fuelSurcharge,
        BigDecimal otherFees,
        BigDecimal totalCost,
        String currency,
        Instant createdAt
) {}

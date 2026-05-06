package com.aidotnet.erp.oms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 订单履约计划领域模型
 * <p>
 * 描述: 订单履约的核心编排实体，将订单拆分为多个包裹并分配仓库和物流渠道。
 *       支持拆单发货和部分发货。
 * </p>
 * <p>
 * 状态流转: PLANNED → PARTIALLY_ALLOCATED → SHIPPED
 *           异常: → EXCEPTION
 *           部分发货: → PARTIALLY_SHIPPED → SHIPPED
 * </p>
 *
 * @param planId               履约计划唯一标识
 * @param tenantId             租户ID
 * @param orderId              关联订单ID
 * @param status               状态
 * @param splitShipment        是否拆单发货
 * @param partialShipment      是否部分发货
 * @param estimatedShippingCost 预估运费
 * @param createdAt            创建时间
 * @param updatedAt            更新时间
 * @param packages             履约包裹列表
 * @author ERP系统
 */
public record OrderFulfillmentPlan(
        String planId,
        String tenantId,
        String orderId,
        FulfillmentPlanStatus status,
        boolean splitShipment,
        boolean partialShipment,
        BigDecimal estimatedShippingCost,
        Instant createdAt,
        Instant updatedAt,
        List<OrderFulfillmentPackage> packages
) {}

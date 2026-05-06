package com.aidotnet.erp.fba.domain;

import java.time.Instant;

/**
 * FBA入库计划领域模型
 * <p>
 * 描述: FBA入库计划，记录计划发往FBA仓库的SKU、数量和关联店铺站点。
 * </p>
 *
 * @author ERP系统
 */
public record FbaInboundPlan(
        String planId,
        String tenantId,
        String warehouseId,
        String planName,
        String sellerSku,
        int plannedQuantity,
        String storeId,
        String siteCode,
        String sourcePlanId,
        FbaInboundPlanStatus status,
        Instant createdAt,
        Instant updatedAt
) {}

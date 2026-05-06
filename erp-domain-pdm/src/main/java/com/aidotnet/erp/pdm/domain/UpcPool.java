package com.aidotnet.erp.pdm.domain;

import java.time.Instant;

/**
 * UPC码池领域模型
 * <p>
 * 描述: UPC(Universal Product Code)码池管理实体，用于跨境电商产品
 *       的条码分配和回收。UPC码是产品上架平台的必要标识。
 * </p>
 * <p>
 * 业务规则:
 *   1. UPC码状态: AVAILABLE(可用) → ASSIGNED(已分配) → AVAILABLE(回收)
 *   2. UPC码分配给SKU后不可重复分配
 *   3. 已分配的UPC码可释放回池中
 *   4. DISABLED状态的UPC码不可使用(如过期或作废)
 * </p>
 *
 * @param poolId        UPC码池记录唯一标识
 * @param tenantId      租户ID
 * @param upcCode       UPC条码，12位数字
 * @param status        状态: AVAILABLE(可用)/ASSIGNED(已分配)/DISABLED(已禁用)
 * @param assignedSkuId 已分配的SKU ID，null表示未分配
 * @param assignedAt    分配时间
 * @param createdAt     创建时间
 * @author ERP系统
 */
public record UpcPool(
        String poolId,
        String tenantId,
        String upcCode,
        UpcStatus status,
        String assignedSkuId,
        Instant assignedAt,
        Instant createdAt
) {
    /** UPC码状态枚举 */
    public enum UpcStatus {
        /** 可用 - UPC码可分配给SKU */
        AVAILABLE,
        /** 已分配 - UPC码已绑定到SKU */
        ASSIGNED,
        /** 已禁用 - UPC码不可使用(过期/作废) */
        DISABLED
    }
}

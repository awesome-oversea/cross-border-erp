package com.aidotnet.erp.scm.domain;

import java.time.Instant;

/**
 * 供应商领域模型
 * <p>
 * 描述: SCM域供应商实体，记录供应商基本信息、信用评级、交期和起订量。
 * </p>
 *
 * @author ERP系统
 */
public record Supplier(
        String supplierId,
        String tenantId,
        String name,
        String companyName,
        String contactName,
        String countryCode,
        String creditRating,
        int leadTimeDays,
        int moq,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    /** 供应商状态 */
    public enum SupplierStatus {
        /** 活跃 */
        ACTIVE,
        /** 停用 */
        INACTIVE,
        /** 黑名单 */
        BLACKLISTED,
        /** 待审核 */
        PENDING_REVIEW
    }

    /** 是否活跃供应商 */
    public boolean isActive() {
        return SupplierStatus.ACTIVE.name().equalsIgnoreCase(status);
    }

    /** 是否可下单(活跃且非黑名单) */
    public boolean canPlaceOrder() {
        return isActive() && !SupplierStatus.BLACKLISTED.name().equalsIgnoreCase(status);
    }
}

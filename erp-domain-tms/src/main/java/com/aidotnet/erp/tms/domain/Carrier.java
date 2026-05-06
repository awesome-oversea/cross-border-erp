package com.aidotnet.erp.tms.domain;

import java.time.Instant;

/**
 * 承运商领域模型
 * <p>
 * 描述: 物流承运商，支持快递、标准、经济和货代四种类型。
 * </p>
 *
 * @author ERP系统
 */
public record Carrier(
        String carrierId,
        String tenantId,
        String code,
        String name,
        String countryCode,
        String type,
        String status,
        String contactPerson,
        String phone,
        boolean apiEnabled,
        Instant createdAt,
        Instant updatedAt
) {
    /** 承运商类型 */
    public enum CarrierType {
        /** 快递 */
        EXPRESS,
        /** 标准 */
        STANDARD,
        /** 经济 */
        ECONOMY,
        /** 货代 */
        FREIGHT_FORWARDER
    }

    /** 承运商状态 */
    public enum CarrierStatus {
        /** 活跃 */
        ACTIVE,
        /** 停用 */
        INACTIVE,
        /** 暂停 */
        SUSPENDED
    }

    /** 是否活跃承运商 */
    public boolean isActive() {
        return CarrierStatus.ACTIVE.name().equalsIgnoreCase(status);
    }
}

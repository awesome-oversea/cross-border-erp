package com.aidotnet.erp.wms.domain;

import java.time.Instant;

/**
 * 仓库领域模型
 * <p>
 * 描述: WMS域核心实体，表示物理仓库。支持自营仓、三方仓、FBA仓和海外仓四种类型。
 * </p>
 *
 * @author ERP系统
 */
public record Warehouse(
        String warehouseId,
        String tenantId,
        String code,
        String name,
        String type,
        String countryCode,
        String address,
        String status,
        String contactPerson,
        String phone,
        Instant createdAt,
        Instant updatedAt
) {
    /** 仓库类型 */
    public enum WarehouseType {
        /** 自营仓 */
        SELF_OWNED,
        /** 三方仓 */
        THIRD_PARTY,
        /** FBA仓 */
        FBA,
        /** 海外仓 */
        OVERSEAS
    }

    /** 仓库状态 */
    public enum WarehouseStatus {
        /** 活跃 */
        ACTIVE,
        /** 停用 */
        INACTIVE,
        /** 维护中 */
        MAINTENANCE
    }

    /** 是否活跃仓库 */
    public boolean isActive() {
        return WarehouseStatus.ACTIVE.name().equalsIgnoreCase(status);
    }
}

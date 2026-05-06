package com.aidotnet.erp.common.security;

import java.util.Map;
import java.util.Set;

public record DataScope(
        String tenantId,
        Set<String> orgIds,
        Set<String> departmentIds,
        Set<String> storeIds,
        Set<String> marketplaceIds,
        Set<String> channelIds,
        Set<String> warehouseIds,
        Set<String> supplierIds,
        Set<String> categoryIds,
        DataLevel dataLevel
) {
    public enum DataLevel {
        DETAIL, SUMMARY, MASKED
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "tenantId", tenantId != null ? tenantId : "",
                "orgIds", orgIds != null ? orgIds : Set.of(),
                "departmentIds", departmentIds != null ? departmentIds : Set.of(),
                "storeIds", storeIds != null ? storeIds : Set.of(),
                "marketplaceIds", marketplaceIds != null ? marketplaceIds : Set.of(),
                "channelIds", channelIds != null ? channelIds : Set.of(),
                "warehouseIds", warehouseIds != null ? warehouseIds : Set.of(),
                "supplierIds", supplierIds != null ? supplierIds : Set.of(),
                "categoryIds", categoryIds != null ? categoryIds : Set.of(),
                "dataLevel", dataLevel != null ? dataLevel.name() : DataLevel.DETAIL.name()
        );
    }
}

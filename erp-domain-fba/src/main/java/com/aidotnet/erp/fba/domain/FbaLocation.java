package com.aidotnet.erp.fba.domain;

import java.time.Instant;

/**
 * FBA仓库位置领域模型
 * <p>
 * 描述: 亚马逊FBA仓库位置信息，记录地址和状态。
 * </p>
 *
 * @author ERP系统
 */
public record FbaLocation(
        String locationId,
        String tenantId,
        String name,
        String countryCode,
        String address,
        String locationType,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}

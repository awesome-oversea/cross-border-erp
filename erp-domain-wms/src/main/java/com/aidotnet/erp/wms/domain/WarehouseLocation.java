package com.aidotnet.erp.wms.domain;

import java.time.Instant;

/**
 * 仓库库位领域模型
 * <p>
 * 描述: 仓库内的物理库位，按区域-通道-货架-货位四级编码。
 * </p>
 *
 * @author ERP系统
 */
public record WarehouseLocation(
        String locationId,
        String tenantId,
        String warehouseId,
        String locationCode,
        String zone,
        String aisle,
        String shelf,
        String bin,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}

package com.aidotnet.erp.fba.domain;

import java.time.Instant;

/**
 * 箱标领域模型
 * <p>
 * 描述: FBA发货箱标，记录每箱SKU数量、箱数和标签打印URL。
 * </p>
 *
 * @author ERP系统
 */
public record CartonLabel(
        String labelId,
        String tenantId,
        String fbaShipmentId,
        String cartonId,
        String sellerSku,
        int quantityPerCarton,
        int numberOfCartons,
        String labelUrl,
        Instant createdAt
) {}

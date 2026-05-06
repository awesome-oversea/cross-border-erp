package com.aidotnet.erp.tms.domain;

import java.time.Instant;

/**
 * 物流渠道领域模型
 * <p>
 * 描述: 承运商下的物流渠道，定义运输类型和计费方式。
 * </p>
 *
 * @author ERP系统
 */
public record ShippingMethod(
        String methodId,
        String tenantId,
        String carrierId,
        String name,
        String type,
        String rateType,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    /** 运输类型 */
    public enum Type { /** 快递 */ EXPRESS, /** 标准 */ STANDARD, /** 经济 */ ECONOMY, /** 特殊 */ SPECIAL }
    /** 计费类型 */
    public enum RateType { /** 固定费率 */ FLAT, /** 按重量 */ WEIGHT_BASED, /** 按体积 */ VOLUME_BASED, /** 按区域 */ ZONE_BASED }
}

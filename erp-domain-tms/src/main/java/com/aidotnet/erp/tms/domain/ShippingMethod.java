package com.aidotnet.erp.tms.domain;

import java.time.Instant;

/**
 * 鐗╂祦娓犻亾棰嗗煙妯″瀷
 * <p>
 * 鎻忚堪: 鎵胯繍鍟嗕笅鐨勭墿娴佹笭閬擄紝瀹氫箟杩愯緭绫诲瀷鍜岃璐规柟寮忋€? * </p>
 *
 * @author ERP绯荤粺
 */
public record ShippingMethod(
        String methodId,
        String tenantId,
        String carrierId,
        String methodCode,
        String methodName,
        String transportMode,
        String rateType,
        boolean enabled,
        Integer estimatedDaysMin,
        Integer estimatedDaysMax,
        Instant createdAt,
        Instant updatedAt
) {
    /** 杩愯緭鏂瑰紡 */
    public enum TransportMode { AIR, SEA, LAND, RAIL, EXPRESS, LOCAL_DELIVERY }
    /** 璁¤垂绫诲瀷 */
    public enum RateType { /** 鍥哄畾璐圭巼 */ FLAT, /** 鎸夐噸閲?*/ WEIGHT_BASED, /** 鎸変綋绉?*/ VOLUME_BASED, /** 鎸夊尯鍩?*/ ZONE_BASED }
}

package com.aidotnet.erp.pdm.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 平台产品限价领域模型
 * <p>
 * 描述: 设置产品在各平台/站点的最低/最高销售限价，防止多店铺同站点内部价格战。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一产品+平台+站点的限价记录唯一
 *   2. 刊登或调价时自动校验是否超出限价范围
 *   3. 超出限价时系统拦截并提示运营人员
 * </p>
 *
 * @author ERP系统
 */
public record PlatformPriceLimit(
        String limitId,
        String tenantId,
        String spuId,
        String platform,
        String marketplace,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String currency,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}

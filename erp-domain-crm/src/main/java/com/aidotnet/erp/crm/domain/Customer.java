package com.aidotnet.erp.crm.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 客户领域模型
 * <p>
 * 描述: CRM域核心实体，表示跨境电商客户。包含基本信息、消费统计和标签。
 *       VIP判定: 累计消费≥1000。
 * </p>
 *
 * @author ERP系统
 */
public record Customer(
        String customerId,
        String tenantId,
        String name,
        String email,
        String phone,
        String countryCode,
        String platform,
        String storeId,
        int totalOrders,
        BigDecimal totalSpent,
        Instant lastOrderAt,
        List<CustomerTag> tags,
        Instant createdAt,
        Instant updatedAt
) {
    /** 计算客单价 = 总消费/总订单数 */
    public BigDecimal getAverageOrderValue() {
        return totalOrders > 0 ? totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /** 是否VIP客户(累计消费≥1000) */
    public boolean isVip() {
        return totalSpent != null && totalSpent.compareTo(new BigDecimal("1000")) >= 0;
    }
}

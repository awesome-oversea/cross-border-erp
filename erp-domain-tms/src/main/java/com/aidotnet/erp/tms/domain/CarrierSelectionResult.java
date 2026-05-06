package com.aidotnet.erp.tms.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * 承运商选择结果领域模型
 * <p>
 * 描述: 承运商选择引擎的完整输出，包含推荐承运商和备选方案列表。
 * </p>
 *
 * @author ERP系统
 */
public record CarrierSelectionResult(
        String recommendedCarrier,
        String recommendedMethod,
        BigDecimal estimatedCost,
        int estimatedDays,
        BigDecimal score,
        List<CarrierOption> alternatives
) {
    /** 备选承运商方案 */
    public record CarrierOption(
            String carrierCode,
            String methodName,
            BigDecimal cost,
            int estimatedDays,
            BigDecimal score,
            String reason
    ) {}
}

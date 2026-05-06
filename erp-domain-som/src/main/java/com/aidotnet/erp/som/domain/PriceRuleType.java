package com.aidotnet.erp.som.domain;

/** 价格规则类型 */
public enum PriceRuleType {
    /** 固定价 - 直接设置售价上限 */
    FIXED,
    /** 百分比 - 按比例调整，保底价兜底 */
    PERCENTAGE,
    /** 竞品跟踪 - 根据竞品价格自动调整 */
    COMPETITIVE,
    /** 成本加成 - 采购成本+加成比例 */
    COST_PLUS
}

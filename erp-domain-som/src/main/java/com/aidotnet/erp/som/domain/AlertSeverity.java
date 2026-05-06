package com.aidotnet.erp.som.domain;

/** 告警严重程度 */
public enum AlertSeverity {
    /** 低 - 提示性信息 */
    LOW,
    /** 中 - 需关注 */
    MEDIUM,
    /** 高 - 需及时处理 */
    HIGH,
    /** 紧急 - 需立即处理 */
    CRITICAL
}

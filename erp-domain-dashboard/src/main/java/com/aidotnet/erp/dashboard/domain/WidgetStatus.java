package com.aidotnet.erp.dashboard.domain;

/**
 * 组件状态枚举
 * <p>
 * 描述: 定义仪表盘组件的生命周期状态
 * </p>
 */
public enum WidgetStatus {
    /** 启用 - 组件正常展示 */
    ACTIVE,
    /** 停用 - 组件暂时不展示，可重新启用 */
    INACTIVE,
    /** 已废弃 - 组件已废弃，不再展示但保留历史数据 */
    DEPRECATED
}

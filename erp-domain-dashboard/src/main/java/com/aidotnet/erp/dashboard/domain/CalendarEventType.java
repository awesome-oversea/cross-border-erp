package com.aidotnet.erp.dashboard.domain;

/**
 * 日历事件类型枚举
 * <p>
 * 描述: 定义日历事件的分类类型，用于前端按类型筛选和颜色区分
 * </p>
 */
public enum CalendarEventType {
    /** 运营事件 - 日常运营相关，如库存预警、订单高峰 */
    OPERATION,
    /** 任务事件 - 待办任务截止日 */
    TASK,
    /** 促销事件 - 促销活动时间范围 */
    PROMOTION,
    /** 截止日 - 合规、财务等关键截止日期 */
    DEADLINE,
    /** 提醒事件 - 自定义提醒事项 */
    REMINDER
}

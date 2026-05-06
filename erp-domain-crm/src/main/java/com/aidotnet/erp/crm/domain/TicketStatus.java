package com.aidotnet.erp.crm.domain;

/** 工单状态 */
public enum TicketStatus {
    /** 待处理 */
    OPEN,
    /** 已分配 */
    ASSIGNED,
    /** 处理中 */
    IN_PROGRESS,
    /** 已解决 */
    RESOLVED,
    /** 已关闭 */
    CLOSED
}

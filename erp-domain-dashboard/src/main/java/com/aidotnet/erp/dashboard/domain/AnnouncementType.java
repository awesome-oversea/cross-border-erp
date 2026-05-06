package com.aidotnet.erp.dashboard.domain;

/**
 * 公告类型枚举
 * <p>
 * 描述: 定义系统公告的类型分类，用于前端按类型筛选展示
 * </p>
 */
public enum AnnouncementType {
    /** 系统公告 - 系统维护、升级、功能变更等通知 */
    SYSTEM,
    /** 业务公告 - 业务规则变更、促销活动、政策调整等通知 */
    BUSINESS,
    /** 紧急公告 - 紧急故障、安全事件、合规要求等需要立即关注的通知 */
    URGENT
}

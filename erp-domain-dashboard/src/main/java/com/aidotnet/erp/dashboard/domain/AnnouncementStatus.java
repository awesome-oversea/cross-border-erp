package com.aidotnet.erp.dashboard.domain;

/**
 * 公告状态枚举
 * <p>
 * 描述: 定义公告的生命周期状态，控制公告的可见性和可操作性
 * </p>
 */
public enum AnnouncementStatus {
    /** 草稿 - 公告已创建但未发布，仅创建人可见 */
    DRAFT,
    /** 已发布 - 公告已发布，所有用户可见 */
    PUBLISHED,
    /** 已过期 - 公告已过有效期，自动从展示列表过滤 */
    EXPIRED
}

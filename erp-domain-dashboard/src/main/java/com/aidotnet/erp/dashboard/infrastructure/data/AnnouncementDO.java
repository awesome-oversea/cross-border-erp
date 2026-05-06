package com.aidotnet.erp.dashboard.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 公告数据对象
 * <p>
 * 描述: 对应dashboard_announcement表，用于存储系统公告信息。
 *       公告支持草稿/已发布状态流转，按优先级排序展示。
 * </p>
 * <p>
 * 业务规则:
 *   1. 公告类型(type): SYSTEM(系统)/OPERATION(运营)/PROMOTION(促销)
 *   2. 公告状态(status): DRAFT(草稿)/PUBLISHED(已发布)
 *   3. 已发布公告按priority降序、publishTime降序排列
 *   4. expireTime过期后不再展示给用户
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.dashboard.domain.Announcement
 */
@TableName("dashboard_announcement")
public class AnnouncementDO {

    /** 公告唯一标识，UUID格式 */
    @TableId(type = IdType.ASSIGN_ID)
    private String announceId;

    /** 租户ID，实现多租户数据隔离 */
    private String tenantId;

    /** 公告标题 */
    private String title;

    /** 公告内容，支持富文本 */
    private String content;

    /** 公告类型: SYSTEM/OPERATION/PROMOTION */
    private String type;

    /** 优先级，数值越大优先级越高 */
    private int priority;

    /** 发布时间 */
    private Instant publishTime;

    /** 过期时间，过期后不再展示 */
    private Instant expireTime;

    /** 公告状态: DRAFT/PUBLISHED */
    private String status;

    /** 创建时间，UTC时区 */
    private Instant createdAt;

    public AnnouncementDO() {}

    public String getAnnounceId() { return announceId; }
    public void setAnnounceId(String announceId) { this.announceId = announceId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public Instant getPublishTime() { return publishTime; }
    public void setPublishTime(Instant publishTime) { this.publishTime = publishTime; }
    public Instant getExpireTime() { return expireTime; }
    public void setExpireTime(Instant expireTime) { this.expireTime = expireTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

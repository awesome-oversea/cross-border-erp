package com.aidotnet.erp.dashboard.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 日历事件数据对象
 * <p>
 * 描述: 对应dashboard_calendar_event表，用于存储用户日历事件。
 *       日历事件可关联业务对象(如订单交付、采购到货等)，支持全天事件和提醒。
 * </p>
 * <p>
 * 业务规则:
 *   1. 事件类型(eventType): MEETING(会议)/DELIVERY(交付)/PURCHASE(采购)/REMINDER(提醒)/OTHER(其他)
 *   2. 全天事件(allDay=true)时startTime为当天0点，endTime为次日0点
 *   3. reminder存储提醒配置，如: "5m"(5分钟前)/"1h"(1小时前)/"1d"(1天前)
 *   4. businessType+businessId关联业务对象
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.dashboard.domain.CalendarEvent
 */
@TableName("dashboard_calendar_event")
public class CalendarEventDO {

    /** 事件唯一标识，UUID格式 */
    @TableId(type = IdType.ASSIGN_ID)
    private String eventId;

    /** 租户ID，实现多租户数据隔离 */
    private String tenantId;

    /** 用户ID，关联IAM域用户 */
    private String userId;

    /** 事件标题 */
    private String title;

    /** 事件描述 */
    private String description;

    /** 事件类型: MEETING/DELIVERY/PURCHASE/REMINDER/OTHER */
    private String eventType;

    /** 开始时间，UTC时区 */
    private Instant startTime;

    /** 结束时间，UTC时区 */
    private Instant endTime;

    /** 关联业务类型，如: ORDER/SHIPMENT/PURCHASE */
    private String businessType;

    /** 关联业务ID */
    private String businessId;

    /** 显示颜色，如: #FF5733 */
    private String color;

    /** 是否全天事件 */
    private boolean allDay;

    /** 提醒配置，如: 5m/1h/1d */
    private String reminder;

    /** 创建时间，UTC时区 */
    private Instant createdAt;

    /** 更新时间，UTC时区 */
    private Instant updatedAt;

    public CalendarEventDO() {}

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public boolean isAllDay() { return allDay; }
    public void setAllDay(boolean allDay) { this.allDay = allDay; }
    public String getReminder() { return reminder; }
    public void setReminder(String reminder) { this.reminder = reminder; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

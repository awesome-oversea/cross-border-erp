package com.aidotnet.erp.dashboard.domain;

import java.time.Instant;

/**
 * 日历事件领域模型
 * <p>
 * 描述: 工作台日历事件实体，将各业务域的时间节点统一展示在日历视图中。
 *       支持运营事件、任务截止日、促销活动等多种事件类型。
 * </p>
 * <p>
 * 业务规则:
 *   1. 事件时间范围不可重叠(同一用户同类型)
 *   2. 业务事件(businessType+businessId)与日历事件一一关联
 *   3. 全天事件(allDay=true)的startTime取当天0时，endTime取当天23:59:59
 * </p>
 *
 * @param eventId      事件唯一标识
 * @param tenantId     租户ID
 * @param userId       所属用户ID
 * @param title        事件标题
 * @param description  事件描述
 * @param eventType    事件类型: OPERATION(运营)、TASK(任务)、PROMOTION(促销)、DEADLINE(截止日)、REMINDER(提醒)
 * @param startTime    开始时间
 * @param endTime      结束时间
 * @param businessType 关联业务类型，如 ORDER/PURCHASE/SHIPMENT
 * @param businessId   关联业务ID，实现日历事件与业务单据的关联
 * @param color        显示颜色，前端渲染用
 * @param allDay       是否全天事件
 * @param reminder     提醒设置，JSON格式，如 {"before":15,"unit":"MINUTES"}
 * @param createdAt    创建时间
 * @param updatedAt    更新时间
 * @author ERP系统
 */
public record CalendarEvent(String eventId, String tenantId, String userId, String title, String description,
                            CalendarEventType eventType, Instant startTime, Instant endTime,
                            String businessType, String businessId, String color,
                            boolean allDay, String reminder, Instant createdAt, Instant updatedAt) {}

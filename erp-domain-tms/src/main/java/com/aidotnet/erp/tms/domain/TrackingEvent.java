package com.aidotnet.erp.tms.domain;

import java.time.Instant;

/**
 * 物流轨迹事件领域模型
 * <p>
 * 描述: 物流追踪事件，记录状态、地点、描述和发生时间。
 * </p>
 *
 * @author ERP系统
 */
public record TrackingEvent(String eventId, String status, String location, String description, Instant occurredAt) {}

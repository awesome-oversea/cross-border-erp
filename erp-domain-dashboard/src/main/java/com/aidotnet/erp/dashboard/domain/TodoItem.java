package com.aidotnet.erp.dashboard.domain;

import java.time.Instant;

/**
 * 待办事项领域模型
 * <p>
 * 描述: 工作台待办事项实体，将各业务域需要人工处理的任务统一展示。
 *       支持多种待办类型和状态流转，与业务单据关联实现一键跳转。
 * </p>
 * <p>
 * 业务规则:
 *   1. 待办状态流转: PENDING → IN_PROGRESS → COMPLETED/CANCELLED
 *   2. 超期未处理的待办自动升级严重等级
 *   3. 待办与业务单据(businessType+businessId)强关联
 * </p>
 *
 * @param todoId       待办唯一标识
 * @param tenantId     租户ID
 * @param userId       负责人ID
 * @param type         待办类型: APPROVAL(审批)、EXCEPTION(异常)、REVIEW(复核)
 * @param businessType 关联业务类型，如 PURCHASE_ORDER/SHIPMENT/REFUND
 * @param businessId   关联业务单据ID
 * @param title        待办标题
 * @param status       待办状态: PENDING(待处理)、IN_PROGRESS(进行中)、COMPLETED(已完成)、CANCELLED(已取消)
 * @param dueTime      截止时间
 * @param createdAt    创建时间
 * @param updatedAt    更新时间
 * @author ERP系统
 */
public record TodoItem(String todoId, String tenantId, String userId, String type, String businessType,
                       String businessId, String title, TodoStatus status, Instant dueTime,
                       Instant createdAt, Instant updatedAt) {}

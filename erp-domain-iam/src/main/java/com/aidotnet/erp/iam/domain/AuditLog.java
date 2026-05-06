package com.aidotnet.erp.iam.domain;

import java.time.Instant;

/**
 * 审计日志领域模型
 * <p>
 * 描述: 操作审计日志实体，记录系统中所有敏感操作的轨迹。
 *       用于安全审计、问题排查和合规要求。
 * </p>
 * <p>
 * 业务规则:
 *   1. 审计日志只增不改，不可修改或删除
 *   2. 每条日志关联traceId，支持链路追踪
 *   3. success字段标识操作是否成功
 *   4. 日志保留期限根据合规要求设定(默认3年)
 * </p>
 *
 * @param auditId    审计日志唯一标识
 * @param tenantId   租户ID
 * @param actor      操作人，用户名或system
 * @param action     操作类型，如 USER_CREATE、ROLE_UPDATE、LOGIN
 * @param module     操作模块，如 iam、oms、fms
 * @param target     操作目标，如 用户名、角色编码
 * @param traceId    链路追踪ID，关联请求链路
 * @param success    操作是否成功
 * @param occurredAt 操作发生时间
 * @author ERP系统
 */
public record AuditLog(String auditId, String tenantId, String actor, String action, String module, String target,
                       String traceId, boolean success, Instant occurredAt) {
}

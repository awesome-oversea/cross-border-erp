package com.aidotnet.erp.pdm.domain;

import java.time.Instant;

/**
 * 知识产权领域模型
 * <p>
 * 描述: 产品知识产权记录实体，支持商标、专利、版权等IP类型的登记和监控。
 *       与SPU关联，实现产品上架前的知识产权合规检查。
 * </p>
 * <p>
 * 业务规则:
 *   1. 知识产权与SPU一对一关联
 *   2. 侵权(infringing)状态的IP关联SPU禁止上架
 *   3. IP到期前30天自动预警
 * </p>
 *
 * @param ipId           知识产权唯一标识
 * @param tenantId       租户ID
 * @param spuId          关联SPU ID
 * @param type           IP类型: TRADEMARK(商标)、PATENT(专利)、COPYRIGHT(版权)、DESIGN(外观设计)
 * @param name           IP名称
 * @param registrationNo 注册号
 * @param jurisdiction   管辖区域，如 US、CN、EU
 * @param status         IP状态: pending(审查中)、registered(已注册)、expired(已过期)、infringing(侵权)
 * @param filedAt        申请日期
 * @param expiresAt      到期日期
 * @param createdAt      创建时间
 * @param updatedAt      更新时间
 * @author ERP系统
 */
public record IntellectualProperty(String ipId, String tenantId, String spuId, IpType type, String name,
                                   String registrationNo, String jurisdiction, IpStatus status,
                                   Instant filedAt, Instant expiresAt, Instant createdAt, Instant updatedAt) {}

package com.aidotnet.erp.pdm.domain;

import java.time.Instant;

/**
 * 品牌领域模型
 * <p>
 * 描述: 产品品牌实体，用于产品的品牌归属管理。
 *       品牌与知识产权关联，支持品牌授权和侵权检测。
 * </p>
 * <p>
 * 业务规则:
 *   1. 品牌名称(name)在同一租户下唯一
 *   2. 品牌可关联知识产权记录
 * </p>
 *
 * @param brandId   品牌唯一标识
 * @param tenantId  租户ID
 * @param name      品牌名称，如 "Apple"、"Samsung"
 * @param createdAt 创建时间
 * @author ERP系统
 */
public record Brand(String brandId, String tenantId, String name, Instant createdAt) {}

package com.aidotnet.erp.pdm.domain;

import java.time.Instant;

/**
 * 产品类目领域模型
 * <p>
 * 描述: 产品分类体系实体，支持树形层级结构。
 *       类目用于产品的分类管理和筛选，是产品主数据的基础维度。
 * </p>
 * <p>
 * 业务规则:
 *   1. 类目名称(name)在同一租户同一父级下唯一
 *   2. 类目支持多级树形结构，通过parentId关联上级类目
 *   3. 已关联产品的类目不可删除
 * </p>
 *
 * @param categoryId 类目唯一标识
 * @param tenantId   租户ID
 * @param name       类目名称，如 "手机壳"、"数据线"
 * @param parentId   上级类目ID，null表示顶级类目
 * @param createdAt  创建时间
 * @author ERP系统
 */
public record Category(String categoryId, String tenantId, String name, String parentId, Instant createdAt) {}

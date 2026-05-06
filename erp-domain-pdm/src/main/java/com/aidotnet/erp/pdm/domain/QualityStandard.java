package com.aidotnet.erp.pdm.domain;

import java.time.Instant;

/**
 * 质检标准领域模型
 * <p>
 * 描述: 产品质检标准实体，定义产品类目下的质量检验规范。
 *       包含检验项目、验收标准等结构化信息，用于产品开发和入库质检。
 * </p>
 * <p>
 * 业务规则:
 *   1. 质检标准按类目(categoryId)分组
 *   2. 可启用/禁用，禁用后不参与质检流程
 *   3. 检验项目(inspectionItems)和验收标准(acceptanceCriteria)为JSON格式
 * </p>
 *
 * @param standardId        质检标准唯一标识
 * @param tenantId          租户ID
 * @param categoryId        适用类目ID
 * @param name              标准名称
 * @param description       标准描述
 * @param inspectionItems   检验项目，JSON格式
 * @param acceptanceCriteria 验收标准，JSON格式
 * @param enabled           是否启用
 * @param createdAt         创建时间
 * @param updatedAt         更新时间
 * @author ERP系统
 */
public record QualityStandard(String standardId, String tenantId, String categoryId, String name,
                              String description, String inspectionItems, String acceptanceCriteria,
                              boolean enabled, Instant createdAt, Instant updatedAt) {}

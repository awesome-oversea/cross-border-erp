package com.aidotnet.erp.pdm.domain;

import java.time.Instant;

/**
 * 产品采集领域模型
 * <p>
 * 描述: 竞品采集实体，记录从外部平台采集的产品信息。
 *       支持多平台采集(Amazon/Shopee/AliExpress等)，采集后可进行
 *       AI分析并转化为选品建议或产品开发流程。
 * </p>
 * <p>
 * 业务规则:
 *   1. 采集状态: COLLECTED(已采集) → ANALYZING(分析中) → ANALYZED(已分析) → CONVERTED(已转化) → ARCHIVED(已归档)
 *   2. 采集来源(sourcePlatform)支持主流跨境电商平台
 *   3. 分析后可转化为选品建议
 * </p>
 *
 * @param collectionId  采集记录唯一标识
 * @param tenantId      租户ID
 * @param sourcePlatform 来源平台，如 Amazon、Shopee、AliExpress
 * @param sourceUrl     来源URL
 * @param productName   产品名称
 * @param description   产品描述
 * @param images        产品图片，JSON格式
 * @param price         采集价格
 * @param currency      价格币种
 * @param categoryId    映射类目ID
 * @param collectedBy   采集人
 * @param status        状态: COLLECTED/ANALYZING/ANALYZED/CONVERTED/ARCHIVED
 * @param createdAt     创建时间
 * @param updatedAt     更新时间
 * @author ERP系统
 */
public record ProductCollection(
        String collectionId,
        String tenantId,
        String sourcePlatform,
        String sourceUrl,
        String productName,
        String description,
        String images,
        String price,
        String currency,
        String categoryId,
        String collectedBy,
        CollectionStatus status,
        Instant createdAt,
        Instant updatedAt
) {}

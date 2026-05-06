package com.aidotnet.erp.pdm.domain;

import java.time.Instant;

/**
 * SPU(标准产品单元)领域模型
 * <p>
 * 描述: 产品主数据域核心实体，代表一类产品的标准信息。
 *       SPU是商品信息的抽象层，一个SPU可包含多个SKU(库存单元)。
 *       如: "iPhone 15"是一个SPU，"iPhone 15 黑色 128G"是一个SKU。
 * </p>
 * <p>
 * 业务规则:
 *   1. SPU创建后为DRAFT状态，需审核通过(APPROVED)后才可上架
 *   2. 知识产权状态(ipStatus)为infringing时禁止发布
 *   3. SPU必须关联类目(categoryId)和品牌(brandId)
 *   4. SPU状态流转: DRAFT → APPROVED → ACTIVE → INACTIVE → ARCHIVED
 * </p>
 *
 * @param spuId       SPU唯一标识
 * @param tenantId    租户ID
 * @param sku         SPU编码，租户内唯一
 * @param title       产品标题
 * @param description 产品描述
 * @param categoryId  所属类目ID
 * @param brandId     所属品牌ID
 * @param status      产品状态: DRAFT(草稿)、APPROVED(已审核)、ACTIVE(上架)、INACTIVE(下架)、ARCHIVED(归档)
 * @param ipStatus    知识产权状态: none(无)、pending(审查中)、clear(无侵权)、infringing(侵权)
 * @param createdAt   创建时间
 * @param updatedAt   更新时间
 * @author ERP系统
 */
public record Spu(
        String spuId,
        String tenantId,
        String sku,
        String title,
        String description,
        String categoryId,
        String brandId,
        ProductStatus status,
        String ipStatus,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * 判断SPU是否可以发布上架
     * <p>
     * 条件: 已审核通过 且 无知识产权侵权
     * </p>
     *
     * @return true-可发布，false-不可发布
     */
    public boolean canPublish() {
        return status == ProductStatus.APPROVED && !"infringing".equals(ipStatus);
    }

    /**
     * 判断SPU是否存在知识产权冲突
     *
     * @return true-存在侵权，false-无侵权
     */
    public boolean hasIpConflict() {
        return "infringing".equals(ipStatus);
    }
}

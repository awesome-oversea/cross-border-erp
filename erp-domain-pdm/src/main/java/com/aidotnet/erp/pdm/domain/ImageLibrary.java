package com.aidotnet.erp.pdm.domain;

import java.time.Instant;

/**
 * 图片库领域模型
 * <p>
 * 描述: 产品上架前配置的图片资源库，运营可维护多张图片，
 *       刊登时系统自动随机调用，减少关联风险。
 * </p>
 * <p>
 * 业务规则:
 *   1. 图片按用途分组(主图/附图/详情图)
 *   2. 同一SPU下的多张同用途图片，刊登时随机匹配
 *   3. 通过MinIO对象存储管理图片文件
 * </p>
 *
 * @author ERP系统
 */
public record ImageLibrary(
        String imageId,
        String tenantId,
        String spuId,
        String imageUrl,
        String imageType,
        int sortOrder,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    /** 图片类型常量 - 主图 */
    public static final String TYPE_MAIN = "MAIN";
    /** 图片类型常量 - 附图 */
    public static final String TYPE_SUB = "SUB";
    /** 图片类型常量 - 详情图 */
    public static final String TYPE_DETAIL = "DETAIL";
}

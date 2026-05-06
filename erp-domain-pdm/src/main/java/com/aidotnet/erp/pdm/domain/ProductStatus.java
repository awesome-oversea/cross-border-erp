package com.aidotnet.erp.pdm.domain;

/**
 * 产品状态枚举
 * <p>
 * 描述: 定义产品的生命周期状态，控制产品的可见性和可操作性
 * </p>
 * <p>
 * 状态流转: DRAFT → APPROVED → ACTIVE → INACTIVE → ARCHIVED
 * </p>
 */
public enum ProductStatus {
    /** 草稿 - 产品已创建但未审核 */
    DRAFT,
    /** 已审核 - 产品审核通过，可上架 */
    APPROVED,
    /** 上架 - 产品已上架，在销售渠道可见 */
    ACTIVE,
    /** 下架 - 产品已下架，在销售渠道不可见 */
    INACTIVE,
    /** 归档 - 产品已归档，不再使用但保留数据 */
    ARCHIVED
}

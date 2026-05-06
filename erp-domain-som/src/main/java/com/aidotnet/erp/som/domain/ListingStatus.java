package com.aidotnet.erp.som.domain;

/**
 * Listing状态枚举
 * <p>
 * 状态流转: DRAFT → ACTIVE → INACTIVE → ARCHIVED
 * </p>
 */
public enum ListingStatus {
    /** 草稿 - 新创建未上架 */
    DRAFT,
    /** 上架中 - 在平台可见可售 */
    ACTIVE,
    /** 已下架 - 平台不可见 */
    INACTIVE,
    /** 已归档 - 不再使用 */
    ARCHIVED
}

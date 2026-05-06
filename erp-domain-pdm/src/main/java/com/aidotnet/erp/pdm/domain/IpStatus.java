package com.aidotnet.erp.pdm.domain;

/**
 * 知识产权状态枚举
 * <p>
 * 描述: 定义知识产权的生命周期状态
 * </p>
 * <p>
 * 状态流转: PENDING → REGISTERED/INVALIDATED，REGISTERED → EXPIRED/INFRINGING
 * </p>
 */
public enum IpStatus {
    /** 待审核 - 知识产权申请已提交 */
    PENDING,
    /** 已注册 - 知识产权已注册生效 */
    REGISTERED,
    /** 侵权 - 检测到知识产权侵权，关联SPU禁止上架 */
    INFRINGING,
    /** 已过期 - 知识产权保护期已过 */
    EXPIRED,
    /** 已无效 - 知识产权被宣告无效 */
    INVALIDATED
}

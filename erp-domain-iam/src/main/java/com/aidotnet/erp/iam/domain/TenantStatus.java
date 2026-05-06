package com.aidotnet.erp.iam.domain;

/**
 * 租户状态枚举
 * <p>
 * 描述: 定义租户的生命周期状态，控制租户的可用性
 * </p>
 */
public enum TenantStatus {
    /** 正常 - 租户可正常使用 */
    ACTIVE,
    /** 停用 - 租户被停用，所有用户无法登录 */
    DISABLED
}

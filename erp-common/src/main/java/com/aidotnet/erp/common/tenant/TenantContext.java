package com.aidotnet.erp.common.tenant;

import com.aidotnet.erp.common.security.DataScope;

/**
 * 租户上下文
 * <p>
 * 描述: 使用ThreadLocal存储当前请求的租户身份和数据权限范围。
 *       所有业务代码通过此上下文获取当前租户ID、用户身份和数据权限过滤条件。
 * </p>
 * <p>
 * 生命周期: 由TenantInterceptor在请求开始时初始化，请求结束时调用clear()清理，
 *           防止线程污染和内存泄漏。
 * </p>
 *
 * @author ERP系统
 */
public final class TenantContext {

    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    /** 当前用户的数据权限范围，用于SQL级别的行级数据过滤 */
    private static final ThreadLocal<DataScope> DATA_SCOPE = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static String getTenantId() {
        return TENANT_ID.get();
    }

    public static void setUserId(String userId) {
        USER_ID.set(userId);
    }

    public static String getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置当前用户的数据权限范围
     * <p>
     * 由PermissionAspect在@RequireDataScope注解处理方法前自动注入。
     * 业务代码可以通过此范围构建数据权限过滤条件。
     * </p>
     */
    public static void setDataScope(DataScope scope) {
        DATA_SCOPE.set(scope);
    }

    /**
     * 获取当前用户的数据权限范围
     * <p>
     * MyBatis-Plus拦截器或业务代码使用此范围自动追加数据权限过滤条件。
     * 返回null表示无数据权限约束(管理员或未配置)。
     * </p>
     */
    public static DataScope getDataScope() {
        return DATA_SCOPE.get();
    }

    /**
     * 清理所有上下文
     * <p>
     * 在请求结束时由TenantInterceptor调用，防止ThreadLocal内存泄漏。
     * </p>
     */
    public static void clear() {
        TENANT_ID.remove();
        USER_ID.remove();
        DATA_SCOPE.remove();
    }
}

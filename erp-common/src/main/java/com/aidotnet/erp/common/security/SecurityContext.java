package com.aidotnet.erp.common.security;

public final class SecurityContext {

    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    private SecurityContext() {
    }

    public static void set(String tenantId, String userId, String username) {
        TENANT_ID.set(tenantId);
        USER_ID.set(userId);
        USERNAME.set(username);
    }

    public static String getTenantId() {
        return TENANT_ID.get();
    }

    public static String getUserId() {
        return USER_ID.get();
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void clear() {
        TENANT_ID.remove();
        USER_ID.remove();
        USERNAME.remove();
    }
}

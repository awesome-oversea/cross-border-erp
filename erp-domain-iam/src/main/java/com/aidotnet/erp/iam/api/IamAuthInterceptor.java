package com.aidotnet.erp.iam.api;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.iam.application.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * IAM认证拦截器
 * <p>
 * 描述: 拦截IAM域内部API请求，校验JWT令牌和权限。
 *       对/api/iam/路径下的请求进行认证和权限检查，
 *       跳过/api/iam/auth/路径(登录/登出接口)。
 * </p>
 * <p>
 * 权限映射规则:
 *   - HTTP方法 + URI路径 → 权限编码
 *   - GET /api/iam/tenants → iam:tenant:read
 *   - POST /api/iam/tenants → iam:tenant:write
 *   - GET /api/iam/users → iam:user:read
 *   - POST /api/iam/users → iam:user:write
 * </p>
 *
 * @author ERP系统
 */
@Component
public class IamAuthInterceptor implements HandlerInterceptor {

    /** HTTP方法+路径 → 权限编码映射表 */
    private final Map<String, String> permissions = Map.of(
            "GET:/api/iam/tenants", "iam:tenant:read",
            "POST:/api/iam/tenants", "iam:tenant:write",
            "PATCH:/api/iam/tenants", "iam:tenant:write",
            "GET:/api/iam/users", "iam:user:read",
            "POST:/api/iam/users", "iam:user:write",
            "GET:/api/iam/roles", "iam:role:read",
            "POST:/api/iam/roles", "iam:role:write",
            "GET:/api/iam/audits", "iam:audit:read"
    );

    private final AuthService authService;

    /**
     * 构造函数 - 依赖注入认证服务
     *
     * @param authService 认证服务
     */
    public IamAuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 请求预处理 - 校验认证和权限
     * <p>
     * 1. 跳过非IAM路径和认证接口路径
     * 2. 解析当前请求所需权限
     * 3. 从Authorization头提取Token并校验权限
     * </p>
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        if (!request.getRequestURI().startsWith("/api/iam/") || request.getRequestURI().startsWith("/api/iam/auth/")) {
            return true;
        }
        String permission = resolvePermission(request);
        if (permission == null) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BizException("UNAUTHORIZED", "认证失败");
        }
        authService.requirePermission(authorization.substring(7), permission);
        return true;
    }

    /** 根据HTTP方法和URI解析所需权限编码 */
    private String resolvePermission(HttpServletRequest request) {
        String key = request.getMethod() + ":" + request.getRequestURI();
        if (request.getRequestURI().startsWith("/api/iam/tenants/") && "PATCH".equals(request.getMethod())) {
            return permissions.get("PATCH:/api/iam/tenants");
        }
        return permissions.get(key);
    }
}

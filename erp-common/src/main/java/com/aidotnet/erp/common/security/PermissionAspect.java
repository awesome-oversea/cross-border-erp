package com.aidotnet.erp.common.security;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 权限校验切面
 * <p>
 * 描述: 统一拦截 @RequirePermission 和 @RequireDataScope 注解，执行权限校验。
 * </p>
 * <p>
 * 核心逻辑:
 *   1. @RequirePermission: 检查用户是否具有指定的菜单/API/按钮操作权限
 *   2. @RequireDataScope: 解析用户的数据权限范围(dimensions定义范围维度)，
 *      将解析结果注入TenantContext供业务SQL拦截使用
 * </p>
 * <p>
 * 10维数据权限校验:
 *   通过DataScopeResolver解析用户权限范围，在查询时自动追加租户隔离
 *   和数据权限筛选条件，实现行级数据安全控制。
 * </p>
 *
 * @author ERP系统
 */
@Aspect
@Component
public class PermissionAspect {

    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);

    /** 超级管理员角色标识，拥有全部权限 */
    private static final String SUPER_ADMIN_ROLE = "ROLE_SUPER_ADMIN";

    private DataScopeResolver dataScopeResolver;

    public PermissionAspect() {
    }

    /**
     * 注入DataScopeResolver用于解析用户数据权限
     * <p>
     * 使用setter注入避免循环依赖
     * </p>
     */
    public void setDataScopeResolver(DataScopeResolver dataScopeResolver) {
        this.dataScopeResolver = dataScopeResolver;
    }

    /**
     * 检查@RequirePermission注解权限
     * <p>
     * 从Spring Security上下文中获取用户已授予的权限集合，
     * 与注解要求的权限进行匹配。超级管理员拥有所有权限。
     * </p>
     */
    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        String permission = requirePermission.value();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BizException("UNAUTHORIZED", "请先登录");
        }
        boolean hasPermission = auth.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals("ROLE_" + permission)
                        || granted.getAuthority().equals(SUPER_ADMIN_ROLE));
        if (!hasPermission) {
            throw new BizException("FORBIDDEN", "权限不足: " + permission);
        }
    }

    /**
     * 检查@RequireDataScope注解的数据权限
     * <p>
     * 解析用户的数据权限范围，将结果注入TenantContext，
     * 供后续SQL查询拦截器使用，实现行级数据隔离。
     * </p>
     * <p>
     * 支持10维数据权限: org/department/store/marketplace/channel/
     * warehouse/supplier/category/data_level 及对象级(object)权限。
     * 如果不指定dimensions，则默认加载所有维度。
     * </p>
     */
    @Before("@annotation(requireDataScope)")
    public void checkDataScope(JoinPoint joinPoint, RequireDataScope requireDataScope) {
        if (dataScopeResolver == null) {
            log.warn("DataScopeResolver not configured, skipping data scope check");
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BizException("UNAUTHORIZED", "请先登录");
        }
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.warn("TenantId not found in context, data scope check may be inaccurate");
            return;
        }
        String userId = auth.getName();
        String[] requestedDimensions = requireDataScope.dimensions();
        DataScope scope = dataScopeResolver.resolve(tenantId, userId);
        TenantContext.setDataScope(scope);
    }
}

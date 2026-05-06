package com.aidotnet.erp.common.security;

import com.aidotnet.erp.common.exception.BizException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        String permission = requirePermission.value();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BizException("UNAUTHORIZED", "请先登录");
        }
        boolean hasPermission = auth.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals("ROLE_" + permission)
                        || granted.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!hasPermission) {
            throw new BizException("FORBIDDEN", "权限不足: " + permission);
        }
    }
}

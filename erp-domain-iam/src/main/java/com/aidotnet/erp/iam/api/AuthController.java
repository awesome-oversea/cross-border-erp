package com.aidotnet.erp.iam.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.iam.application.AuthService;
import com.aidotnet.erp.iam.domain.AuthToken;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * <p>
 * 描述: IAM域认证REST API，提供用户登录、登出、Token验证等接口。
 *       路径前缀: /iam/api/in/v1/auth (内部接口)
 * </p>
 * <p>
 * 接口列表:
 *   - POST /login  - 用户登录，签发JWT令牌
 *   - POST /logout - 用户登出，注销令牌
 *   - POST /verify - 验证令牌有效性
 * </p>
 * <p>
 * 安全说明:
 *   - 登录需携带X-Tenant-Id请求头标识租户
 *   - 登出和验证需携带Authorization: Bearer {token}请求头
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/iam/api/in/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 构造函数 - 依赖注入认证服务
     *
     * @param authService 认证服务
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     * <p>
     * 校验租户状态、用户状态和密码后签发JWT令牌。
     * </p>
     *
     * @param tenantId 租户ID(请求头)
     * @param request  登录请求(username + password)
     * @return 认证令牌(包含JWT、用户信息、权限列表)
     */
    @PostMapping("/login")
    public Result<AuthToken> login(@RequestHeader("X-Tenant-Id") String tenantId, @Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(tenantId, request.username(), request.password()));
    }

    /**
     * 用户登出
     * <p>
     * 注销当前Token，后续使用该Token的请求将被拒绝。
     * </p>
     *
     * @param authorization Authorization请求头，格式: Bearer {token}
     * @return 空结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(extractToken(authorization));
        return Result.ok();
    }

    /**
     * 验证令牌有效性
     * <p>
     * 校验Token是否有效且未过期，返回令牌详情。
     * </p>
     *
     * @param authorization Authorization请求头，格式: Bearer {token}
     * @return 令牌详情
     */
    @PostMapping("/verify")
    public Result<AuthToken> verify(@RequestHeader("Authorization") String authorization) {
        return Result.ok(authService.verify(extractToken(authorization)));
    }

    /** 从Authorization头提取Token字符串，去除"Bearer "前缀 */
    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }
}

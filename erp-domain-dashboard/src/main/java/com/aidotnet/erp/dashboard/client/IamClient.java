package com.aidotnet.erp.dashboard.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * IAM域Feign客户端，用于工作台域与身份认证域的跨域通信。
 * <p>
 * 描述: 工作台域通过此客户端查询IAM域的用户信息，
 *       用于用户仪表盘配置、待办事项分配和AI洞察推送。
 * </p>
 * <p>
 * 跨域关联:
 *   - DASHBOARD → IAM: 查询用户信息(用于仪表盘用户关联)
 *   - DASHBOARD → IAM: 查询用户权限(用于组件权限控制)
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "iam-client-dashboard", path = "/iam/api/in/v1")
public interface IamClient {

    /**
     * 按用户ID查询用户信息。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 用户信息(包含用户名、角色、部门等)
     */
    @GetMapping("/users/{userId}")
    Result<Map<String, Object>> getUser(@RequestHeader("X-Tenant-Id") String tenantId,
                                        @PathVariable String userId);

    /**
     * 查询当前用户权限列表。
     * <p>
     * 用于工作台组件的权限控制，决定用户可见的组件范围。
     * </p>
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 权限列表(包含角色编码和权限编码)
     */
    @GetMapping("/users/{userId}/permissions")
    Result<Map<String, Object>> getUserPermissions(@RequestHeader("X-Tenant-Id") String tenantId,
                                                    @PathVariable String userId);
}

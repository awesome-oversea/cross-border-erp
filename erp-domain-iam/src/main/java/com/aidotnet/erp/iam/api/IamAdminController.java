package com.aidotnet.erp.iam.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.security.DataScope;
import com.aidotnet.erp.iam.application.DataScopeService;
import com.aidotnet.erp.iam.application.IamService;
import com.aidotnet.erp.iam.domain.AuditLog;
import com.aidotnet.erp.iam.domain.Department;
import com.aidotnet.erp.iam.domain.ObjectPermission;
import com.aidotnet.erp.iam.domain.Organization;
import com.aidotnet.erp.iam.domain.Permission;
import com.aidotnet.erp.iam.domain.Position;
import com.aidotnet.erp.iam.domain.Role;
import com.aidotnet.erp.iam.domain.Tenant;
import com.aidotnet.erp.iam.domain.UserAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * IAM管理控制器
 * <p>
 * 描述: IAM域管理后台REST API，提供租户、用户、角色、权限、组织、部门、岗位、
 *       数据权限、对象权限、审计日志等资源的增删改查接口。
 *       路径前缀: /iam/api/in/v1 (内部接口)
 * </p>
 * <p>
 * 接口分组:
 *   1. 租户管理 - /tenants (创建/列表/停用)
 *   2. 用户管理 - /users (创建/更新/改密/角色分配/列表/数据权限)
 *   3. 角色管理 - /roles (创建/权限更新/列表)
 *   4. 权限管理 - /permissions (树形查询/模块列表)
 *   5. 组织管理 - /orgs (创建/更新/列表)
 *   6. 部门管理 - /departments (创建/更新/停用/列表/部门用户)
 *   7. 岗位管理 - /positions (创建/更新/停用/列表)
 *   8. 对象权限 - /object-permissions (授予/撤销/列表)
 *   9. 审计日志 - /audits (列表)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/iam/api/in/v1")
public class IamAdminController {

    private final IamService iamService;
    private final DataScopeService dataScopeService;

    /**
     * 构造函数 - 依赖注入IAM服务和数据权限服务
     *
     * @param iamService       IAM组织权限应用服务
     * @param dataScopeService 数据权限范围服务
     */
    public IamAdminController(IamService iamService, DataScopeService dataScopeService) {
        this.iamService = iamService;
        this.dataScopeService = dataScopeService;
    }

    /** 创建租户 */
    @PostMapping("/tenants")
    public Result<Tenant> createTenant(@Valid @RequestBody TenantRequest request) {
        return Result.ok(iamService.createTenant(request.tenantId(), request.name(), request.code(), request.plan(), request.expireAt()));
    }

    @GetMapping("/tenants")
    public Result<List<Tenant>> listTenants() {
        return Result.ok(iamService.listTenants());
    }

    @PatchMapping("/tenants/{tenantId}/disable")
    public Result<Tenant> disableTenant(@PathVariable String tenantId) {
        return Result.ok(iamService.disableTenant(tenantId));
    }

    @PostMapping("/users")
    public Result<UserAccount> createUser(@Valid @RequestBody UserRequest request) {
        return Result.ok(iamService.createUser(request.username(), request.email(), request.phone(), request.password(), request.orgId()));
    }

    @PutMapping("/users/{userId}")
    public Result<UserAccount> updateUser(@PathVariable String userId, @Valid @RequestBody UpdateUserRequest request) {
        return Result.ok(iamService.updateUser(userId, request.email(), request.phone(), request.enabled()));
    }

    @PatchMapping("/users/{userId}/password")
    public Result<UserAccount> changePassword(@PathVariable String userId, @Valid @RequestBody ChangePasswordRequest request) {
        return Result.ok(iamService.changePassword(userId, request.oldPassword(), request.newPassword()));
    }

    @PostMapping("/users/{userId}/roles")
    public Result<Void> assignRoles(@PathVariable String userId, @Valid @RequestBody AssignRolesRequest request) {
        iamService.assignRoles(userId, request.roleIds(), request.orgId());
        return Result.ok();
    }

    @GetMapping("/users")
    public Result<List<UserAccount>> listUsers() {
        return Result.ok(iamService.listUsers());
    }

    @PostMapping("/roles")
    public Result<Role> createRole(@Valid @RequestBody RoleRequest request) {
        return Result.ok(iamService.createRole(request.name(), request.code(), request.type(), request.permIds()));
    }

    @PutMapping("/roles/{roleId}/permissions")
    public Result<Role> updateRolePermissions(@PathVariable String roleId, @Valid @RequestBody UpdatePermissionsRequest request) {
        return Result.ok(iamService.updateRolePermissions(roleId, request.permIds()));
    }

    @GetMapping("/roles")
    public Result<List<Role>> listRoles() {
        return Result.ok(iamService.listRoles());
    }

    @GetMapping("/permissions/tree")
    public Result<List<Permission>> listPermissions(@RequestParam(required = false) String resource) {
        if (resource != null && !resource.isBlank()) {
            return Result.ok(iamService.listPermissionsByResource(resource));
        }
        return Result.ok(iamService.listPermissions());
    }

    @PostMapping("/orgs")
    public Result<Organization> createOrganization(@Valid @RequestBody OrgRequest request) {
        return Result.ok(iamService.createOrganization(request.name(), request.parentOrgId(), request.type(), request.managerId()));
    }

    @PutMapping("/orgs/{orgId}")
    public Result<Organization> updateOrganization(@PathVariable String orgId, @Valid @RequestBody UpdateOrgRequest request) {
        return Result.ok(iamService.updateOrganization(orgId, request.name(), request.parentOrgId(), request.type(), request.managerId()));
    }

    @GetMapping("/orgs")
    public Result<List<Organization>> listOrganizations() {
        return Result.ok(iamService.listOrganizations());
    }

    @PutMapping("/users/{userId}/data-scope")
    public Result<Void> setDataScope(@PathVariable String userId, @Valid @RequestBody DataScopeRequest request) {
        dataScopeService.setDataScope(userId, request.tenantId(), request.resourceType(), request.resourceIds(), request.scopeType());
        return Result.ok();
    }

    @GetMapping("/users/{userId}/data-scope")
    public Result<DataScope> getUserDataScope(@PathVariable String userId,
                                              @RequestParam String tenantId) {
        return Result.ok(dataScopeService.resolve(tenantId, userId));
    }

    @GetMapping("/audits")
    public Result<List<AuditLog>> listAudit() {
        return Result.ok(iamService.listAudit());
    }

    @PostMapping("/departments")
    public Result<Department> createDepartment(@Valid @RequestBody DeptRequest request) {
        return Result.ok(iamService.createDepartment(request.name(), request.parentDeptId(), request.orgId(), request.managerId()));
    }

    @PutMapping("/departments/{deptId}")
    public Result<Department> updateDepartment(@PathVariable String deptId, @Valid @RequestBody UpdateDeptRequest request) {
        return Result.ok(iamService.updateDepartment(deptId, request.name(), request.parentDeptId(), request.managerId()));
    }

    @PatchMapping("/departments/{deptId}/disable")
    public Result<Department> disableDepartment(@PathVariable String deptId) {
        return Result.ok(iamService.disableDepartment(deptId));
    }

    @GetMapping("/departments")
    public Result<List<Department>> listDepartments(@RequestParam(required = false) String orgId) {
        if (orgId != null && !orgId.isBlank()) {
            return Result.ok(iamService.listDepartmentsByOrg(orgId));
        }
        return Result.ok(iamService.listDepartments());
    }

    @GetMapping("/departments/{deptId}/users")
    public Result<List<UserAccount>> listDeptUsers(@PathVariable String deptId) {
        return Result.ok(iamService.listUsersByDepartment(deptId));
    }

    @GetMapping("/permissions/modules")
    public Result<List<String>> listModules() {
        return Result.ok(iamService.listModules());
    }

    @PostMapping("/positions")
    public Result<Position> createPosition(@Valid @RequestBody PositionRequest request) {
        return Result.ok(iamService.createPosition(request.name(), request.code(), request.orgId(), request.level(), request.parentId()));
    }

    @PutMapping("/positions/{positionId}")
    public Result<Position> updatePosition(@PathVariable String positionId, @Valid @RequestBody UpdatePositionRequest request) {
        return Result.ok(iamService.updatePosition(positionId, request.name(), request.orgId(), request.level(), request.parentId()));
    }

    @PatchMapping("/positions/{positionId}/disable")
    public Result<Position> disablePosition(@PathVariable String positionId) {
        return Result.ok(iamService.disablePosition(positionId));
    }

    @GetMapping("/positions")
    public Result<List<Position>> listPositions(@RequestParam(required = false) String orgId) {
        return Result.ok(iamService.listPositions(orgId));
    }

    @PostMapping("/object-permissions")
    public Result<ObjectPermission> grantObjectPermission(@Valid @RequestBody ObjectPermissionRequest request) {
        return Result.ok(iamService.grantObjectPermission(request.userId(), request.resourceType(),
                request.resourceId(), request.permissions(), request.grantedBy()));
    }

    @DeleteMapping("/object-permissions/{objPermId}")
    public Result<Void> revokeObjectPermission(@PathVariable String objPermId) {
        iamService.revokeObjectPermission(objPermId);
        return Result.ok();
    }

    @GetMapping("/object-permissions")
    public Result<List<ObjectPermission>> listObjectPermissions(@RequestParam(required = false) String userId,
                                                                 @RequestParam(required = false) String resourceType) {
        return Result.ok(iamService.listObjectPermissions(userId, resourceType));
    }

    public record TenantRequest(@NotBlank String tenantId, @NotBlank String name, String code, String plan, Instant expireAt) {}

    public record UserRequest(@NotBlank String username, String email, String phone, @NotBlank String password, String orgId) {}

    public record UpdateUserRequest(String email, String phone, Boolean enabled) {}

    public record ChangePasswordRequest(@NotBlank String oldPassword, @NotBlank String newPassword) {}

    public record AssignRolesRequest(@NotEmpty Set<String> roleIds, String orgId) {}

    public record RoleRequest(@NotBlank String name, @NotBlank String code, String type, @NotEmpty Set<String> permIds) {}

    public record UpdatePermissionsRequest(@NotEmpty Set<String> permIds) {}

    public record OrgRequest(@NotBlank String name, String parentOrgId, String type, String managerId) {}

    public record UpdateOrgRequest(@NotBlank String name, String parentOrgId, String type, String managerId) {}

    public record DataScopeRequest(@NotBlank String tenantId, @NotBlank String resourceType,
                                   @NotEmpty Set<String> resourceIds, @NotBlank String scopeType) {}

    public record DeptRequest(@NotBlank String name, String parentDeptId, String orgId, String managerId) {}

    public record UpdateDeptRequest(@NotBlank String name, String parentDeptId, String managerId) {}

    public record PositionRequest(@NotBlank String name, @NotBlank String code, String orgId, int level, String parentId) {}

    public record UpdatePositionRequest(String name, String orgId, int level, String parentId) {}

    public record ObjectPermissionRequest(@NotBlank String userId, @NotBlank String resourceType,
                                          @NotBlank String resourceId, @NotEmpty List<String> permissions,
                                          String grantedBy) {}
}

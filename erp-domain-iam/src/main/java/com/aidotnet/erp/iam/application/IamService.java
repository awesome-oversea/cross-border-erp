package com.aidotnet.erp.iam.application;

import com.aidotnet.erp.common.context.TraceContext;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.exception.ErrorCode;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.iam.domain.AuditLog;
import com.aidotnet.erp.iam.domain.Department;
import com.aidotnet.erp.iam.domain.ObjectPermission;
import com.aidotnet.erp.iam.domain.Organization;
import com.aidotnet.erp.iam.domain.Permission;
import com.aidotnet.erp.iam.domain.Position;
import com.aidotnet.erp.iam.domain.Role;
import com.aidotnet.erp.iam.domain.Tenant;
import com.aidotnet.erp.iam.domain.TenantStatus;
import com.aidotnet.erp.iam.domain.UserAccount;
import com.aidotnet.erp.iam.domain.UserRole;
import com.aidotnet.erp.iam.infrastructure.IamStore;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * IAM组织权限应用服务
 * <p>
 * 描述: 身份与访问管理域核心服务，负责租户、用户、角色、权限、
 *       组织、部门、岗位的增删改查业务逻辑，以及操作审计日志记录。
 *       是整个ERP系统的安全基础设施。
 * </p>
 * <p>
 * 核心能力:
 *   1. 租户管理 - 创建/停用租户，实现多租户隔离
 *   2. 用户管理 - 创建/更新/停用用户，密码修改
 *   3. 角色管理 - 创建角色、分配权限
 *   4. 组织管理 - 创建/更新组织架构(树形结构)
 *   5. 部门管理 - 创建/更新/停用部门
 *   6. 岗位管理 - 创建/更新/停用岗位
 *   7. 权限分配 - 用户-角色关联、角色-权限关联
 *   8. 审计日志 - 记录所有敏感操作
 * </p>
 * <p>
 * 业务规则:
 *   1. 所有操作需校验租户上下文，确保多租户隔离
 *   2. 用户名在同一租户下唯一
 *   3. 角色编码在同一租户下唯一
 *   4. 组织架构为树形结构，通过path+level实现层级查询
 *   5. 密码修改需验证原密码
 *   6. 所有敏感操作记录审计日志
 * </p>
 *
 * @author ERP系统
 */
@Service
public class IamService {

    private final IamStore iamStore;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造函数 - 依赖注入IAM存储和密码编码器
     *
     * @param iamStore        IAM数据存储
     * @param passwordEncoder 密码编码器(BCrypt)
     */
    public IamService(IamStore iamStore, PasswordEncoder passwordEncoder) {
        this.iamStore = iamStore;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 创建租户
     * <p>
     * 校验租户ID唯一性后创建新租户，初始状态为ACTIVE。
     * </p>
     *
     * @param tenantId 租户ID，由外部系统分配
     * @param name     租户名称
     * @param code     租户编码
     * @param plan     套餐类型: standard/professional/enterprise
     * @param expireAt 过期时间
     * @return 新创建的租户
     * @throws BizException TENANT_EXISTS - 租户已存在
     */
    public Tenant createTenant(String tenantId, String name, String code, String plan, Instant expireAt) {
        if (iamStore.findTenant(tenantId).isPresent()) {
            throw new BizException(ErrorCode.TENANT_EXISTS, "租户已存在");
        }
        Instant now = Instant.now();
        Tenant tenant = iamStore.saveTenant(new Tenant(tenantId, name, code, TenantStatus.ACTIVE, plan, expireAt, now, now));
        audit(tenantId, "TENANT_CREATE", "tenant", tenantId, true);
        return tenant;
    }

    public List<Tenant> listTenants() {
        return iamStore.listTenants();
    }

    public Tenant disableTenant(String tenantId) {
        Tenant tenant = iamStore.findTenant(tenantId).orElseThrow(() -> new BizException("TENANT_NOT_FOUND", "租户不存在"));
        Tenant disabled = new Tenant(tenant.tenantId(), tenant.name(), tenant.code(), TenantStatus.DISABLED, tenant.plan(), tenant.expireAt(), tenant.createdAt(), Instant.now());
        iamStore.saveTenant(disabled);
        audit(tenantId, "TENANT_DISABLE", "tenant", tenantId, true);
        return disabled;
    }

    public UserAccount createUser(String username, String email, String phone, String password, String orgId) {
        String tenantId = currentTenant();
        iamStore.findTenant(tenantId).orElseThrow(() -> new BizException("TENANT_NOT_FOUND", "租户不存在"));
        if (iamStore.findUser(tenantId, username).isPresent()) {
            throw new BizException("USER_EXISTS", "用户已存在");
        }
        Instant now = Instant.now();
        UserAccount user = iamStore.saveUser(new UserAccount(UUID.randomUUID().toString(), tenantId, orgId, username,
                email, phone, passwordEncoder.encode(password), true, "active", null, now, now));
        audit(tenantId, "USER_CREATE", "user", username, true);
        return user;
    }

    public UserAccount updateUser(String userId, String email, String phone, Boolean enabled) {
        String tenantId = currentTenant();
        UserAccount user = iamStore.findUserById(userId)
                .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
        if (!user.tenantId().equals(tenantId)) {
            throw new BizException("USER_NOT_FOUND", "用户不存在");
        }
        String newEmail = email != null ? email : user.email();
        String newPhone = phone != null ? phone : user.phone();
        boolean newEnabled = enabled != null ? enabled : user.enabled();
        UserAccount updated = new UserAccount(user.userId(), user.tenantId(), user.orgId(), user.username(),
                newEmail, newPhone, user.passwordHash(), newEnabled, user.status(), user.lastLogin(), user.createdAt(), Instant.now());
        iamStore.saveUser(updated);
        audit(tenantId, "USER_UPDATE", "user", user.username(), true);
        return updated;
    }

    public UserAccount changePassword(String userId, String oldPassword, String newPassword) {
        String tenantId = currentTenant();
        UserAccount user = iamStore.findUserById(userId)
                .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
        if (!user.tenantId().equals(tenantId)) {
            throw new BizException("USER_NOT_FOUND", "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.passwordHash())) {
            throw new BizException("PASSWORD_MISMATCH", "原密码不正确");
        }
        UserAccount updated = new UserAccount(user.userId(), user.tenantId(), user.orgId(), user.username(),
                user.email(), user.phone(), passwordEncoder.encode(newPassword), user.enabled(), user.status(), user.lastLogin(), user.createdAt(), Instant.now());
        iamStore.saveUser(updated);
        audit(tenantId, "USER_CHANGE_PASSWORD", "user", user.username(), true);
        return updated;
    }

    public void assignRoles(String userId, Set<String> roleIds, String orgId) {
        String tenantId = currentTenant();
        UserAccount user = iamStore.findUserById(userId)
                .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
        if (!user.tenantId().equals(tenantId)) {
            throw new BizException("USER_NOT_FOUND", "用户不存在");
        }
        for (String roleId : roleIds) {
            iamStore.findRoleById(roleId).orElseThrow(() -> new BizException("ROLE_NOT_FOUND", "角色不存在: " + roleId));
        }
        iamStore.findUserRoles(userId).forEach(ur -> {});
        for (String roleId : roleIds) {
            iamStore.saveUserRole(new UserRole(userId, roleId, orgId, Instant.now()));
        }
        audit(tenantId, "USER_ASSIGN_ROLES", "user", user.username(), true);
    }

    public List<UserAccount> listUsers() {
        return iamStore.listUsers(currentTenant());
    }

    public Role createRole(String name, String code, String type, Set<String> permIds) {
        String tenantId = currentTenant();
        Instant now = Instant.now();
        Role role = iamStore.saveRole(new Role(UUID.randomUUID().toString(), tenantId, name, code, type, "active", permIds, now, now));
        audit(tenantId, "ROLE_CREATE", "role", code, true);
        return role;
    }

    public Role updateRolePermissions(String roleId, Set<String> permIds) {
        String tenantId = currentTenant();
        Role role = iamStore.findRoleById(roleId).orElseThrow(() -> new BizException("ROLE_NOT_FOUND", "角色不存在"));
        Role updated = new Role(role.roleId(), role.tenantId(), role.name(), role.code(), role.type(), role.status(), permIds, role.createdAt(), Instant.now());
        iamStore.saveRole(updated);
        audit(tenantId, "ROLE_UPDATE_PERMISSIONS", "role", role.code(), true);
        return updated;
    }

    public List<Role> listRoles() {
        return iamStore.listRoles(currentTenant());
    }

    public List<Permission> listPermissions() {
        return iamStore.listPermissions();
    }

    public List<Permission> listPermissionsByResource(String resource) {
        return iamStore.listPermissionsByResource(resource);
    }

    public Organization createOrganization(String name, String parentOrgId, String type, String managerId) {
        String tenantId = currentTenant();
        int level = 1;
        String path = "/";
        if (parentOrgId != null) {
            Organization parent = iamStore.findOrganization(tenantId, parentOrgId)
                    .orElseThrow(() -> new BizException("PARENT_ORG_NOT_FOUND", "上级组织不存在"));
            level = parent.level() + 1;
            path = parent.path() + parent.orgId() + "/";
        }
        Instant now = Instant.now();
        String orgId = UUID.randomUUID().toString();
        Organization org = iamStore.saveOrganization(new Organization(orgId, tenantId, name, parentOrgId, type, path, level, managerId, now, now));
        audit(tenantId, "ORG_CREATE", "org", org.orgId(), true);
        return org;
    }

    public Organization updateOrganization(String orgId, String name, String parentOrgId, String type, String managerId) {
        String tenantId = currentTenant();
        Organization org = iamStore.findOrganization(tenantId, orgId)
                .orElseThrow(() -> new BizException("ORG_NOT_FOUND", "组织不存在"));
        if (parentOrgId != null && iamStore.findOrganization(tenantId, parentOrgId).isEmpty()) {
            throw new BizException("PARENT_ORG_NOT_FOUND", "上级组织不存在");
        }
        Organization updated = new Organization(org.orgId(), tenantId, name, parentOrgId,
                type != null ? type : org.type(), org.path(), org.level(), managerId, org.createdAt(), Instant.now());
        iamStore.saveOrganization(updated);
        audit(tenantId, "ORG_UPDATE", "org", orgId, true);
        return updated;
    }

    public List<Organization> listOrganizations() {
        return iamStore.listOrganizations(currentTenant());
    }

    public List<AuditLog> listAudit() {
        return iamStore.listAudit(currentTenant());
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "缺少租户上下文");
        }
        return tenantId;
    }

    private void audit(String tenantId, String action, String module, String target, boolean success) {
        iamStore.appendAudit(new AuditLog(UUID.randomUUID().toString(), tenantId, "system", action, module, target,
                TraceContext.getTraceId(), success, Instant.now()));
    }

    public Department createDepartment(String name, String parentDeptId, String orgId, String managerId) {
        String tenantId = currentTenant();
        if (orgId != null && iamStore.findOrganization(tenantId, orgId).isEmpty()) {
            throw new BizException("ORG_NOT_FOUND", "所属组织不存在");
        }
        if (parentDeptId != null && iamStore.findDepartment(tenantId, parentDeptId).isEmpty()) {
            throw new BizException("PARENT_DEPT_NOT_FOUND", "上级部门不存在");
        }
        Instant now = Instant.now();
        Department dept = iamStore.saveDepartment(new Department(UUID.randomUUID().toString(), tenantId, name,
                parentDeptId, orgId, managerId, true, now, now));
        audit(tenantId, "DEPT_CREATE", "dept", dept.deptId(), true);
        return dept;
    }

    public Department updateDepartment(String deptId, String name, String parentDeptId, String managerId) {
        String tenantId = currentTenant();
        Department dept = iamStore.findDepartment(tenantId, deptId)
                .orElseThrow(() -> new BizException("DEPT_NOT_FOUND", "部门不存在"));
        if (parentDeptId != null && iamStore.findDepartment(tenantId, parentDeptId).isEmpty()) {
            throw new BizException("PARENT_DEPT_NOT_FOUND", "上级部门不存在");
        }
        Department updated = new Department(dept.deptId(), tenantId, name, parentDeptId, dept.orgId(),
                managerId, dept.enabled(), dept.createdAt(), Instant.now());
        iamStore.saveDepartment(updated);
        audit(tenantId, "DEPT_UPDATE", "dept", deptId, true);
        return updated;
    }

    public Department disableDepartment(String deptId) {
        String tenantId = currentTenant();
        Department dept = iamStore.findDepartment(tenantId, deptId)
                .orElseThrow(() -> new BizException("DEPT_NOT_FOUND", "部门不存在"));
        Department updated = dept.withEnabled(false);
        iamStore.saveDepartment(updated);
        audit(tenantId, "DEPT_DISABLE", "dept", deptId, true);
        return updated;
    }

    public List<Department> listDepartments() {
        return iamStore.listDepartments(currentTenant());
    }

    public List<Department> listDepartmentsByOrg(String orgId) {
        return iamStore.listDepartmentsByOrg(currentTenant(), orgId);
    }

    public List<UserAccount> listUsersByDepartment(String deptId) {
        return iamStore.listUsersByDepartment(currentTenant(), deptId);
    }

    public List<String> listModules() {
        return iamStore.listModules();
    }

    public Position createPosition(String name, String code, String orgId, int level, String parentId) {
        String tenantId = currentTenant();
        if (orgId != null && iamStore.findOrganization(tenantId, orgId).isEmpty()) {
            throw new BizException("ORG_NOT_FOUND", "所属组织不存在");
        }
        if (parentId != null && iamStore.findPosition(tenantId, parentId).isEmpty()) {
            throw new BizException("PARENT_POSITION_NOT_FOUND", "上级岗位不存在");
        }
        Instant now = Instant.now();
        Position position = iamStore.savePosition(new Position(
                UUID.randomUUID().toString(), tenantId, orgId, name, code, level, parentId, "active", now, now));
        audit(tenantId, "POSITION_CREATE", "position", position.positionId(), true);
        return position;
    }

    public Position updatePosition(String positionId, String name, String orgId, int level, String parentId) {
        String tenantId = currentTenant();
        Position existing = iamStore.findPosition(tenantId, positionId)
                .orElseThrow(() -> new BizException("POSITION_NOT_FOUND", "岗位不存在"));
        if (parentId != null && iamStore.findPosition(tenantId, parentId).isEmpty()) {
            throw new BizException("PARENT_POSITION_NOT_FOUND", "上级岗位不存在");
        }
        Position updated = new Position(
                existing.positionId(), tenantId,
                orgId != null ? orgId : existing.orgId(),
                name != null ? name : existing.name(),
                existing.code(),
                level > 0 ? level : existing.level(),
                parentId != null ? parentId : existing.parentId(),
                existing.status(), existing.createdAt(), Instant.now());
        iamStore.savePosition(updated);
        audit(tenantId, "POSITION_UPDATE", "position", positionId, true);
        return updated;
    }

    public Position disablePosition(String positionId) {
        String tenantId = currentTenant();
        Position existing = iamStore.findPosition(tenantId, positionId)
                .orElseThrow(() -> new BizException("POSITION_NOT_FOUND", "岗位不存在"));
        Position updated = new Position(
                existing.positionId(), existing.tenantId(), existing.orgId(), existing.name(), existing.code(),
                existing.level(), existing.parentId(), "inactive", existing.createdAt(), Instant.now());
        iamStore.savePosition(updated);
        audit(tenantId, "POSITION_DISABLE", "position", positionId, true);
        return updated;
    }

    public List<Position> listPositions(String orgId) {
        String tenantId = currentTenant();
        if (orgId != null && !orgId.isBlank()) {
            return iamStore.listPositionsByOrg(tenantId, orgId);
        }
        return iamStore.listPositions(tenantId);
    }

    public ObjectPermission grantObjectPermission(String userId, String resourceType, String resourceId,
                                                   List<String> permissions, String grantedBy) {
        String tenantId = currentTenant();
        iamStore.findUserById(userId).orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
        List<ObjectPermission> existing = iamStore.findObjectPermissions(tenantId, userId, resourceType).stream()
                .filter(p -> p.resourceId().equals(resourceId))
                .toList();
        if (!existing.isEmpty()) {
            ObjectPermission prev = existing.get(0);
            iamStore.removeObjectPermission(prev.objPermId());
        }
        Instant now = Instant.now();
        ObjectPermission perm = iamStore.saveObjectPermission(new ObjectPermission(
                UUID.randomUUID().toString(), tenantId, userId, resourceType, resourceId,
                permissions, grantedBy, now));
        audit(tenantId, "OBJECT_PERMISSION_GRANT", "object_permission", perm.objPermId(), true);
        return perm;
    }

    public void revokeObjectPermission(String objPermId) {
        String tenantId = currentTenant();
        ObjectPermission perm = iamStore.findObjectPermissions(tenantId, null, null).stream()
                .filter(p -> p.objPermId().equals(objPermId))
                .findFirst()
                .orElseThrow(() -> new BizException("OBJECT_PERMISSION_NOT_FOUND", "对象权限不存在"));
        iamStore.removeObjectPermission(objPermId);
        audit(tenantId, "OBJECT_PERMISSION_REVOKE", "object_permission", objPermId, true);
    }

    public List<ObjectPermission> listObjectPermissions(String userId, String resourceType) {
        return iamStore.findObjectPermissions(currentTenant(), userId, resourceType);
    }

    public boolean checkObjectPermission(String userId, String resourceType, String resourceId, String action) {
        String tenantId = currentTenant();
        List<ObjectPermission> perms = iamStore.findObjectPermissions(tenantId, userId, resourceType).stream()
                .filter(p -> p.resourceId().equals(resourceId))
                .toList();
        return perms.stream().anyMatch(p -> p.permissions().contains(action) || p.permissions().contains("*"));
    }
}

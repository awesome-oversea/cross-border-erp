package com.aidotnet.erp.iam.infrastructure;

import com.aidotnet.erp.iam.domain.AuditLog;
import com.aidotnet.erp.iam.domain.AuthToken;
import com.aidotnet.erp.iam.domain.Department;
import com.aidotnet.erp.iam.domain.ObjectPermission;
import com.aidotnet.erp.iam.domain.Organization;
import com.aidotnet.erp.iam.domain.Permission;
import com.aidotnet.erp.iam.domain.Position;
import com.aidotnet.erp.iam.domain.Role;
import com.aidotnet.erp.iam.domain.Tenant;
import com.aidotnet.erp.iam.domain.TenantStatus;
import com.aidotnet.erp.iam.domain.UserAccount;
import com.aidotnet.erp.iam.domain.UserDataScope;
import com.aidotnet.erp.iam.domain.UserRole;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * IAM域数据存储
 * <p>
 * 描述: IAM域的核心数据存储层，负责租户、用户、角色、权限、组织、部门、岗位等
 *       实体的CRUD操作。当前采用ConcurrentHashMap内存存储实现，后续可切换为
 *       基于MyBatis-Plus的持久化存储。
 * </p>
 * <p>
 * 数据结构说明:
 *   - tenants: 租户表，key=tenantId
 *   - users: 用户表(按租户+用户名索引)，key=tenantId:username
 *   - usersById: 用户表(按用户ID索引)，key=userId
 *   - rolesById: 角色表(按角色ID索引)，key=roleId
 *   - rolesByCode: 角色表(按角色编码索引)，key=code
 *   - tokens: 认证令牌表，key=JWT token字符串
 *   - auditLogs: 审计日志列表，按时间顺序追加
 *   - dataScopes: 数据权限范围表，key=scopeId
 *   - organizations: 组织架构表，key=orgId
 *   - departments: 部门表，key=deptId
 *   - permissions: 权限定义表，key=permId(格式: domain:resource:action)
 *   - userRoles: 用户-角色关联列表
 *   - positions: 岗位表，key=positionId
 *   - objectPermissions: 对象权限表，key=objPermId
 * </p>
 * <p>
 * 线程安全: 所有Map使用ConcurrentHashMap，List使用Collections.synchronizedList
 * </p>
 *
 * @author ERP系统
 */
@Repository
public class IamStore {

    /** 租户表，key=tenantId，存储所有租户信息 */
    private final Map<String, Tenant> tenants = new ConcurrentHashMap<>();
    /** 用户表(复合索引)，key=tenantId:username，支持按租户+用户名快速查找 */
    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();
    /** 用户表(主键索引)，key=userId，支持按用户ID快速查找 */
    private final Map<String, UserAccount> usersById = new ConcurrentHashMap<>();
    /** 角色表(主键索引)，key=roleId */
    private final Map<String, Role> rolesById = new ConcurrentHashMap<>();
    /** 角色表(编码索引)，key=code，支持按角色编码快速查找 */
    private final Map<String, Role> rolesByCode = new ConcurrentHashMap<>();
    /** 认证令牌表，key=JWT token字符串，支持Token验证和注销 */
    private final Map<String, AuthToken> tokens = new ConcurrentHashMap<>();
    /** 审计日志列表，按时间顺序追加，只增不改 */
    private final List<AuditLog> auditLogs = Collections.synchronizedList(new ArrayList<>());
    /** 数据权限范围表，key=scopeId，控制用户可访问的数据范围 */
    private final Map<String, UserDataScope> dataScopes = new ConcurrentHashMap<>();
    /** 组织架构表，key=orgId，支持树形层级结构 */
    private final Map<String, Organization> organizations = new ConcurrentHashMap<>();
    /** 部门表，key=deptId，隶属于组织架构 */
    private final Map<String, Department> departments = new ConcurrentHashMap<>();
    /** 权限定义表，key=permId(格式: domain:resource:action) */
    private final Map<String, Permission> permissions = new ConcurrentHashMap<>();
    /** 用户-角色关联列表，多对多关系 */
    private final List<UserRole> userRoles = Collections.synchronizedList(new ArrayList<>());
    /** 岗位表，key=positionId，定义组织内职能角色 */
    private final Map<String, Position> positions = new ConcurrentHashMap<>();
    /** 对象权限表，key=objPermId，细粒度对象级权限控制 */
    private final Map<String, ObjectPermission> objectPermissions = new ConcurrentHashMap<>();
    /** 密码编码器，用于密码哈希和验证 */
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * 构造函数 - 初始化默认数据
     * <p>
     * 创建演示租户、默认角色(管理员/运营人员/只读用户)和管理员用户。
     * 初始化系统权限定义，覆盖所有14个业务域的读写权限。
     * </p>
     *
     * @param passwordEncoder 密码编码器(BCrypt)，用于密码哈希
     */
    public IamStore(org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        Instant now = Instant.now();
        tenants.put("tenant-demo", new Tenant("tenant-demo", "Demo Tenant", "DEMO", TenantStatus.ACTIVE, "standard", null, now, now));

        Role adminRole = new Role("r-admin", "tenant-demo", "管理员", "ADMIN", "system", "active", Set.of("iam:tenant:read", "iam:tenant:write", "iam:user:read", "iam:user:write", "iam:role:read", "iam:role:write", "iam:audit:read", "iam:org:read", "iam:org:write", "iam:permission:read"), now, now);
        Role operatorRole = new Role("r-operator", "tenant-demo", "运营人员", "OPERATOR", "system", "active", Set.of("iam:user:read", "pdm:product:read", "pdm:product:write", "oms:order:read", "oms:order:write", "som:store:read"), now, now);
        Role viewerRole = new Role("r-viewer", "tenant-demo", "只读用户", "VIEWER", "system", "active", Set.of("iam:user:read", "pdm:product:read", "oms:order:read", "bi:report:read"), now, now);
        rolesById.put(adminRole.roleId(), adminRole);
        rolesById.put(operatorRole.roleId(), operatorRole);
        rolesById.put(viewerRole.roleId(), viewerRole);
        rolesByCode.put(adminRole.code(), adminRole);
        rolesByCode.put(operatorRole.code(), operatorRole);
        rolesByCode.put(viewerRole.code(), viewerRole);

        UserAccount admin = new UserAccount("u-admin", "tenant-demo", null, "admin", "admin@demo.com", null, passwordEncoder.encode("admin123"), true, "active", now, now, now);
        users.put(userKey("tenant-demo", "admin"), admin);
        usersById.put("u-admin", admin);
        userRoles.add(new UserRole("u-admin", "r-admin", null, now));

        initPermissions();
    }

    /**
     * 初始化系统权限定义
     * <p>
     * 定义所有14个业务域的读写权限，权限编码格式: {域}:{资源}:{操作}
     * 写权限通过parentCode关联对应的读权限，实现权限继承。
     * </p>
     */
    private void initPermissions() {
        Instant now = Instant.now();
        String[][] perms = {
                {"iam:tenant:read", "tenant", "read", "读取租户", null, "active"},
                {"iam:tenant:write", "tenant", "write", "写入租户", "iam:tenant:read", "active"},
                {"iam:user:read", "user", "read", "读取用户", null, "active"},
                {"iam:user:write", "user", "write", "写入用户", "iam:user:read", "active"},
                {"iam:role:read", "role", "read", "读取角色", null, "active"},
                {"iam:role:write", "role", "write", "写入角色", "iam:role:read", "active"},
                {"iam:org:read", "org", "read", "读取组织", null, "active"},
                {"iam:org:write", "org", "write", "写入组织", "iam:org:read", "active"},
                {"iam:permission:read", "permission", "read", "读取权限", null, "active"},
                {"iam:audit:read", "audit", "read", "读取审计", null, "active"},
                {"iam:dept:read", "dept", "read", "读取部门", null, "active"},
                {"iam:dept:write", "dept", "write", "写入部门", "iam:dept:read", "active"},
                {"iam:datascope:read", "datascope", "read", "读取数据权限", null, "active"},
                {"iam:datascope:write", "datascope", "write", "写入数据权限", "iam:datascope:read", "active"},
                {"pdm:product:read", "product", "read", "读取产品", null, "active"},
                {"pdm:product:write", "product", "write", "写入产品", "pdm:product:read", "active"},
                {"oms:order:read", "order", "read", "读取订单", null, "active"},
                {"oms:order:write", "order", "write", "写入订单", "oms:order:read", "active"},
                {"scm:purchase:read", "purchase", "read", "读取采购", null, "active"},
                {"scm:purchase:write", "purchase", "write", "写入采购", "scm:purchase:read", "active"},
                {"wms:inventory:read", "inventory", "read", "读取库存", null, "active"},
                {"wms:inventory:write", "inventory", "write", "写入库存", "wms:inventory:read", "active"},
                {"fms:finance:read", "finance", "read", "读取财务", null, "active"},
                {"fms:finance:write", "finance", "write", "写入财务", "fms:finance:read", "active"},
                {"som:store:read", "store", "read", "读取店铺", null, "active"},
                {"som:store:write", "store", "write", "写入店铺", "som:store:read", "active"},
                {"bi:report:read", "report", "read", "读取报表", null, "active"},
                {"ads:campaign:read", "campaign", "read", "读取广告", null, "active"},
                {"ads:campaign:write", "campaign", "write", "写入广告", "ads:campaign:read", "active"},
                {"fba:shipment:read", "shipment", "read", "读取FBA货件", null, "active"},
                {"fba:shipment:write", "shipment", "write", "写入FBA货件", "fba:shipment:read", "active"},
                {"tms:shipment:read", "logistics", "read", "读取物流", null, "active"},
                {"tms:shipment:write", "logistics", "write", "写入物流", "tms:shipment:read", "active"},
                {"crm:customer:read", "customer", "read", "读取客户", null, "active"},
                {"crm:customer:write", "customer", "write", "写入客户", "crm:customer:read", "active"},
                {"crm:ticket:read", "ticket", "read", "读取工单", null, "active"},
                {"crm:ticket:write", "ticket", "write", "写入工单", "crm:ticket:read", "active"},
                {"bi:report:write", "report", "write", "写入报表", "bi:report:read", "active"},
                {"sys:config:read", "config", "read", "读取系统配置", null, "active"},
                {"sys:config:write", "config", "write", "写入系统配置", "sys:config:read", "active"},
                {"dashboard:view", "dashboard", "view", "查看工作台", null, "active"},
        };
        for (String[] p : perms) {
            permissions.put(p[0], new Permission(p[0], p[1], p[2], p[3], p[4], p[5], now));
        }
    }

    /** 保存租户，存在则更新 */
    public Tenant saveTenant(Tenant tenant) {
        tenants.put(tenant.tenantId(), tenant);
        return tenant;
    }

    /** 按租户ID查找租户 */
    public Optional<Tenant> findTenant(String tenantId) {
        return Optional.ofNullable(tenants.get(tenantId));
    }

    /** 查询所有租户列表 */
    public List<Tenant> listTenants() {
        return new ArrayList<>(new LinkedHashMap<>(tenants).values());
    }

    /** 保存用户，同时维护复合索引(tenantId:username)和主键索引(userId) */
    public UserAccount saveUser(UserAccount user) {
        users.put(userKey(user.tenantId(), user.username()), user);
        usersById.put(user.userId(), user);
        return user;
    }

    /** 按租户ID+用户名查找用户 */
    public Optional<UserAccount> findUser(String tenantId, String username) {
        return Optional.ofNullable(users.get(userKey(tenantId, username)));
    }

    /** 按用户ID查找用户 */
    public Optional<UserAccount> findUserById(String userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    /** 按租户ID查询用户列表 */
    public List<UserAccount> listUsers(String tenantId) {
        return users.values().stream().filter(user -> user.tenantId().equals(tenantId)).toList();
    }

    /** 保存角色，同时维护主键索引(roleId)和编码索引(code) */
    public Role saveRole(Role role) {
        rolesById.put(role.roleId(), role);
        rolesByCode.put(role.code(), role);
        return role;
    }

    /** 按角色ID查找角色 */
    public Optional<Role> findRoleById(String roleId) {
        return Optional.ofNullable(rolesById.get(roleId));
    }

    /** 按角色编码查找角色 */
    public Optional<Role> findRoleByCode(String code) {
        return Optional.ofNullable(rolesByCode.get(code));
    }

    /** 按租户ID查询角色列表(含系统全局角色) */
    public List<Role> listRoles(String tenantId) {
        return rolesById.values().stream()
                .filter(r -> r.tenantId() == null || r.tenantId().equals(tenantId))
                .toList();
    }

    /** 查询所有角色列表 */
    public List<Role> listAllRoles() {
        return new ArrayList<>(rolesById.values());
    }

    /** 保存用户-角色关联，先去重再追加 */
    public void saveUserRole(UserRole userRole) {
        userRoles.removeIf(ur -> ur.userId().equals(userRole.userId()) && ur.roleId().equals(userRole.roleId()));
        userRoles.add(userRole);
    }

    /** 按用户ID查询关联的角色列表 */
    public List<UserRole> findUserRoles(String userId) {
        return userRoles.stream().filter(ur -> ur.userId().equals(userId)).toList();
    }

    /** 按用户ID查询关联的角色编码集合 */
    public Set<String> findUserRoleCodes(String userId) {
        return userRoles.stream()
                .filter(ur -> ur.userId().equals(userId))
                .map(ur -> rolesById.get(ur.roleId()))
                .filter(r -> r != null)
                .map(Role::code)
                .collect(Collectors.toSet());
    }

    /** 按用户ID查询所有权限编码集合(通过角色关联) */
    public Set<String> findUserPermissions(String userId) {
        return userRoles.stream()
                .filter(ur -> ur.userId().equals(userId))
                .map(ur -> rolesById.get(ur.roleId()))
                .filter(r -> r != null)
                .flatMap(r -> r.permIds().stream())
                .collect(Collectors.toSet());
    }

    /** 保存认证令牌 */
    public AuthToken saveToken(AuthToken token) {
        tokens.put(token.token(), token);
        return token;
    }

    /** 查找有效令牌(已过期的不返回) */
    public Optional<AuthToken> findToken(String token) {
        return Optional.ofNullable(tokens.get(token)).filter(authToken -> !authToken.expired());
    }

    /** 移除令牌(用于登出) */
    public void removeToken(String token) {
        tokens.remove(token);
    }

    /** 追加审计日志(只增不改) */
    public void appendAudit(AuditLog auditLog) {
        auditLogs.add(auditLog);
    }

    /** 按租户ID查询审计日志列表 */
    public List<AuditLog> listAudit(String tenantId) {
        return auditLogs.stream().filter(log -> log.tenantId().equals(tenantId)).toList();
    }

    /** 生成用户复合索引key: tenantId:username */
    private String userKey(String tenantId, String username) {
        return tenantId + ":" + username;
    }

    /** 保存数据权限范围 */
    public void saveDataScope(UserDataScope scope) {
        dataScopes.put(scope.scopeId(), scope);
    }

    /** 按用户ID查询数据权限范围集合 */
    public Set<UserDataScope> findDataScopes(String userId) {
        return dataScopes.values().stream()
                .filter(ds -> ds.userId().equals(userId))
                .collect(Collectors.toSet());
    }

    /** 保存组织 */
    public Organization saveOrganization(Organization org) {
        organizations.put(org.orgId(), org);
        return org;
    }

    /** 按租户ID+组织ID查找组织 */
    public Optional<Organization> findOrganization(String tenantId, String orgId) {
        return Optional.ofNullable(organizations.get(orgId))
                .filter(org -> org.tenantId().equals(tenantId));
    }

    /** 按租户ID查询组织列表 */
    public List<Organization> listOrganizations(String tenantId) {
        return organizations.values().stream()
                .filter(org -> org.tenantId().equals(tenantId))
                .toList();
    }

    /** 查询所有权限定义列表 */
    public List<Permission> listPermissions() {
        return new ArrayList<>(permissions.values());
    }

    /** 按资源类型查询权限列表 */
    public List<Permission> listPermissionsByResource(String resource) {
        return permissions.values().stream()
                .filter(p -> p.resource().equals(resource))
                .toList();
    }

    /** 保存部门 */
    public Department saveDepartment(Department dept) {
        departments.put(dept.deptId(), dept);
        return dept;
    }

    /** 按租户ID+部门ID查找部门 */
    public Optional<Department> findDepartment(String tenantId, String deptId) {
        return Optional.ofNullable(departments.get(deptId))
                .filter(d -> d.tenantId().equals(tenantId));
    }

    /** 按租户ID查询部门列表 */
    public List<Department> listDepartments(String tenantId) {
        return departments.values().stream()
                .filter(d -> d.tenantId().equals(tenantId))
                .toList();
    }

    /** 按租户ID+组织ID查询部门列表 */
    public List<Department> listDepartmentsByOrg(String tenantId, String orgId) {
        return departments.values().stream()
                .filter(d -> d.tenantId().equals(tenantId) && d.orgId().equals(orgId))
                .toList();
    }

    /** 按租户ID+部门ID查询部门下用户列表(通过数据权限范围关联) */
    public List<UserAccount> listUsersByDepartment(String tenantId, String deptId) {
        Set<String> scopeUserIds = dataScopes.values().stream()
                .filter(ds -> ds.resourceType().equals("department") && ds.resourceIds().contains(deptId))
                .map(UserDataScope::userId)
                .collect(Collectors.toSet());
        return usersById.values().stream()
                .filter(u -> u.tenantId().equals(tenantId) && scopeUserIds.contains(u.userId()))
                .toList();
    }

    /** 按权限编码查找权限定义 */
    public Optional<Permission> findPermission(String permissionCode) {
        return Optional.ofNullable(permissions.get(permissionCode));
    }

    /** 查询所有权限模块列表(去重后的资源类型) */
    public List<String> listModules() {
        return permissions.values().stream()
                .map(Permission::resource)
                .distinct()
                .sorted()
                .toList();
    }

    /** 保存岗位 */
    public Position savePosition(Position position) {
        positions.put(position.positionId(), position);
        return position;
    }

    /** 按租户ID+岗位ID查找岗位 */
    public Optional<Position> findPosition(String tenantId, String positionId) {
        return Optional.ofNullable(positions.get(positionId))
                .filter(p -> p.tenantId().equals(tenantId));
    }

    /** 按租户ID查询岗位列表 */
    public List<Position> listPositions(String tenantId) {
        return positions.values().stream()
                .filter(p -> p.tenantId().equals(tenantId))
                .toList();
    }

    /** 按租户ID+组织ID查询岗位列表 */
    public List<Position> listPositionsByOrg(String tenantId, String orgId) {
        return positions.values().stream()
                .filter(p -> p.tenantId().equals(tenantId) && (p.orgId() == null || p.orgId().equals(orgId)))
                .toList();
    }

    /** 保存对象权限 */
    public ObjectPermission saveObjectPermission(ObjectPermission perm) {
        objectPermissions.put(perm.objPermId(), perm);
        return perm;
    }

    /** 按条件查询对象权限列表(支持按用户ID和资源类型过滤) */
    public List<ObjectPermission> findObjectPermissions(String tenantId, String userId, String resourceType) {
        return objectPermissions.values().stream()
                .filter(p -> p.tenantId().equals(tenantId))
                .filter(p -> userId == null || p.userId().equals(userId))
                .filter(p -> resourceType == null || p.resourceType().equals(resourceType))
                .toList();
    }

    /** 按资源类型+资源ID查询对象权限列表 */
    public List<ObjectPermission> findObjectPermissionsByResource(String tenantId, String resourceType, String resourceId) {
        return objectPermissions.values().stream()
                .filter(p -> p.tenantId().equals(tenantId))
                .filter(p -> p.resourceType().equals(resourceType) && p.resourceId().equals(resourceId))
                .toList();
    }

    /** 移除对象权限 */
    public void removeObjectPermission(String objPermId) {
        objectPermissions.remove(objPermId);
    }
}

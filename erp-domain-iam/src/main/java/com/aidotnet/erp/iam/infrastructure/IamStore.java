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
import com.aidotnet.erp.iam.infrastructure.data.AuditLogDO;
import com.aidotnet.erp.iam.infrastructure.data.AuthTokenDO;
import com.aidotnet.erp.iam.infrastructure.data.DepartmentDO;
import com.aidotnet.erp.iam.infrastructure.data.ObjectPermissionDO;
import com.aidotnet.erp.iam.infrastructure.data.OrganizationDO;
import com.aidotnet.erp.iam.infrastructure.data.PermissionDO;
import com.aidotnet.erp.iam.infrastructure.data.PositionDO;
import com.aidotnet.erp.iam.infrastructure.data.RoleDO;
import com.aidotnet.erp.iam.infrastructure.data.TenantDO;
import com.aidotnet.erp.iam.infrastructure.data.UserAccountDO;
import com.aidotnet.erp.iam.infrastructure.data.UserDataScopeDO;
import com.aidotnet.erp.iam.infrastructure.data.UserRoleDO;
import com.aidotnet.erp.iam.infrastructure.mapper.AuditLogMapper;
import com.aidotnet.erp.iam.infrastructure.mapper.AuthTokenMapper;
import com.aidotnet.erp.iam.infrastructure.mapper.DepartmentMapper;
import com.aidotnet.erp.iam.infrastructure.mapper.IamMapper;
import com.aidotnet.erp.iam.infrastructure.mapper.ObjectPermissionMapper;
import com.aidotnet.erp.iam.infrastructure.mapper.OrganizationMapper;
import com.aidotnet.erp.iam.infrastructure.mapper.PermissionMapper;
import com.aidotnet.erp.iam.infrastructure.mapper.PositionMapper;
import com.aidotnet.erp.iam.infrastructure.mapper.RoleMapper;
import com.aidotnet.erp.iam.infrastructure.mapper.TenantMapper;
import com.aidotnet.erp.iam.infrastructure.mapper.UserDataScopeMapper;
import com.aidotnet.erp.iam.infrastructure.mapper.UserRoleMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * IAM域数据存储
 * <p>
 * 描述: IAM域的核心数据存储层，负责租户、用户、角色、权限、组织、部门、岗位等
 *       实体的CRUD操作。基于MyBatis持久化存储，提供领域对象与数据对象的转换。
 * </p>
 * <p>
 * 数据转换: 领域模型(Domain) ↔ 数据对象(DO)，通过手动映射实现
 * </p>
 *
 * @author ERP系统
 */
@Repository
public class IamStore {

    private final TenantMapper tenantMapper;
    private final IamMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final AuthTokenMapper authTokenMapper;
    private final AuditLogMapper auditLogMapper;
    private final UserDataScopeMapper dataScopeMapper;
    private final OrganizationMapper organizationMapper;
    private final DepartmentMapper departmentMapper;
    private final PermissionMapper permissionMapper;
    private final PositionMapper positionMapper;
    private final ObjectPermissionMapper objectPermissionMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public IamStore(TenantMapper tenantMapper,
                    IamMapper userMapper,
                    RoleMapper roleMapper,
                    UserRoleMapper userRoleMapper,
                    AuthTokenMapper authTokenMapper,
                    AuditLogMapper auditLogMapper,
                    UserDataScopeMapper dataScopeMapper,
                    OrganizationMapper organizationMapper,
                    DepartmentMapper departmentMapper,
                    PermissionMapper permissionMapper,
                    PositionMapper positionMapper,
                    ObjectPermissionMapper objectPermissionMapper,
                    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.tenantMapper = tenantMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.authTokenMapper = authTokenMapper;
        this.auditLogMapper = auditLogMapper;
        this.dataScopeMapper = dataScopeMapper;
        this.organizationMapper = organizationMapper;
        this.departmentMapper = departmentMapper;
        this.permissionMapper = permissionMapper;
        this.positionMapper = positionMapper;
        this.objectPermissionMapper = objectPermissionMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Tenant saveTenant(Tenant tenant) {
        TenantDO existing = tenantMapper.selectById(tenant.tenantId());
        TenantDO data = toTenantData(tenant);
        if (existing == null) {
            tenantMapper.insert(data);
        } else {
            tenantMapper.update(data);
        }
        return tenant;
    }

    public Optional<Tenant> findTenant(String tenantId) {
        return Optional.ofNullable(tenantMapper.selectById(tenantId)).map(this::toTenantDomain);
    }

    public List<Tenant> listTenants() {
        return tenantMapper.selectAll().stream().map(this::toTenantDomain).collect(Collectors.toList());
    }

    public UserAccount saveUser(UserAccount user) {
        UserAccountDO existing = userMapper.selectUserByIdGlobal(user.userId());
        UserAccountDO data = toUserData(user);
        if (existing == null) {
            userMapper.insertUser(data);
        } else {
            userMapper.updateUser(data);
        }
        return user;
    }

    public Optional<UserAccount> findUser(String tenantId, String username) {
        return Optional.ofNullable(userMapper.selectUserByTenantAndUsername(tenantId, username)).map(this::toUserDomain);
    }

    public Optional<UserAccount> findUserById(String userId) {
        return Optional.ofNullable(userMapper.selectUserByIdGlobal(userId)).map(this::toUserDomain);
    }

    public List<UserAccount> listUsers(String tenantId) {
        return userMapper.selectUsersByTenant(tenantId).stream().map(this::toUserDomain).collect(Collectors.toList());
    }

    public Role saveRole(Role role) {
        RoleDO existing = roleMapper.selectById(role.roleId());
        RoleDO data = toRoleData(role);
        if (existing == null) {
            roleMapper.insert(data);
        } else {
            roleMapper.update(data);
        }
        return role;
    }

    public Optional<Role> findRoleById(String roleId) {
        return Optional.ofNullable(roleMapper.selectById(roleId)).map(this::toRoleDomain);
    }

    public Optional<Role> findRoleByCode(String code) {
        return Optional.ofNullable(roleMapper.selectByCode(code)).map(this::toRoleDomain);
    }

    public List<Role> listRoles(String tenantId) {
        return roleMapper.selectByTenant(tenantId).stream().map(this::toRoleDomain).collect(Collectors.toList());
    }

    public List<Role> listAllRoles() {
        return roleMapper.selectAll().stream().map(this::toRoleDomain).collect(Collectors.toList());
    }

    public void saveUserRole(UserRole userRole) {
        List<UserRoleDO> existing = userRoleMapper.selectByUserId(userRole.userId());
        boolean exists = existing.stream().anyMatch(ur -> ur.getRoleId().equals(userRole.roleId()));
        if (!exists) {
            userRoleMapper.insert(toUserRoleData(userRole));
        }
    }

    public List<UserRole> findUserRoles(String userId) {
        return userRoleMapper.selectByUserId(userId).stream().map(this::toUserRoleDomain).collect(Collectors.toList());
    }

    public Set<String> findUserRoleCodes(String userId) {
        return userRoleMapper.selectByUserId(userId).stream()
                .map(ur -> roleMapper.selectById(ur.getRoleId()))
                .filter(r -> r != null)
                .map(RoleDO::getCode)
                .collect(Collectors.toSet());
    }

    public Set<String> findUserPermissions(String userId) {
        return userRoleMapper.selectByUserId(userId).stream()
                .map(ur -> roleMapper.selectById(ur.getRoleId()))
                .filter(r -> r != null)
                .flatMap(r -> parsePermIds(r.getPermIds()).stream())
                .collect(Collectors.toSet());
    }

    public AuthToken saveToken(AuthToken token) {
        AuthTokenDO data = toAuthTokenData(token);
        authTokenMapper.insert(data);
        return token;
    }

    public Optional<AuthToken> findToken(String token) {
        return Optional.ofNullable(authTokenMapper.selectByToken(token))
                .filter(authTokenDO -> authTokenDO.getExpiresAt().isAfter(Instant.now()))
                .map(this::toAuthTokenDomain);
    }

    public void removeToken(String token) {
        authTokenMapper.deleteByToken(token);
    }

    public void appendAudit(AuditLog auditLog) {
        auditLogMapper.insert(toAuditLogData(auditLog));
    }

    public List<AuditLog> listAudit(String tenantId) {
        return auditLogMapper.selectByTenant(tenantId).stream().map(this::toAuditLogDomain).collect(Collectors.toList());
    }

    public void saveDataScope(UserDataScope scope) {
        dataScopeMapper.deleteByUserIdAndResourceType(scope.userId(), scope.resourceType());
        dataScopeMapper.insert(toDataScopeData(scope));
    }

    public Set<UserDataScope> findDataScopes(String userId) {
        return dataScopeMapper.selectByUserId(userId).stream().map(this::toDataScopeDomain).collect(Collectors.toSet());
    }

    public Organization saveOrganization(Organization org) {
        OrganizationDO existing = organizationMapper.selectById(org.tenantId(), org.orgId());
        OrganizationDO data = toOrganizationData(org);
        if (existing == null) {
            organizationMapper.insert(data);
        } else {
            organizationMapper.update(data);
        }
        return org;
    }

    public Optional<Organization> findOrganization(String tenantId, String orgId) {
        return Optional.ofNullable(organizationMapper.selectById(tenantId, orgId)).map(this::toOrganizationDomain);
    }

    public List<Organization> listOrganizations(String tenantId) {
        return organizationMapper.selectByTenant(tenantId).stream().map(this::toOrganizationDomain).collect(Collectors.toList());
    }

    public List<Permission> listPermissions() {
        return permissionMapper.selectAll().stream().map(this::toPermissionDomain).collect(Collectors.toList());
    }

    public List<Permission> listPermissionsByResource(String resource) {
        return permissionMapper.selectByModule(resource).stream().map(this::toPermissionDomain).collect(Collectors.toList());
    }

    public Optional<Permission> findPermission(String permissionCode) {
        return Optional.ofNullable(permissionMapper.selectByCode(permissionCode)).map(this::toPermissionDomain);
    }

    public List<String> listModules() {
        return permissionMapper.selectDistinctModules();
    }

    public Department saveDepartment(Department dept) {
        DepartmentDO existing = departmentMapper.selectById(dept.tenantId(), dept.deptId());
        DepartmentDO data = toDepartmentData(dept);
        if (existing == null) {
            departmentMapper.insert(data);
        } else {
            departmentMapper.update(data);
        }
        return dept;
    }

    public Optional<Department> findDepartment(String tenantId, String deptId) {
        return Optional.ofNullable(departmentMapper.selectById(tenantId, deptId)).map(this::toDepartmentDomain);
    }

    public List<Department> listDepartments(String tenantId) {
        return departmentMapper.selectByTenant(tenantId).stream().map(this::toDepartmentDomain).collect(Collectors.toList());
    }

    public List<Department> listDepartmentsByOrg(String tenantId, String orgId) {
        return departmentMapper.selectByOrg(tenantId, orgId).stream().map(this::toDepartmentDomain).collect(Collectors.toList());
    }

    public List<UserAccount> listUsersByDepartment(String tenantId, String deptId) {
        Set<String> scopeUserIds = dataScopeMapper.selectByUserId(null).stream()
                .filter(ds -> "department".equals(ds.getResourceType()) && ds.getResourceIds().contains(deptId))
                .map(UserDataScopeDO::getUserId)
                .collect(Collectors.toSet());
        return userMapper.selectUsersByTenant(tenantId).stream()
                .filter(u -> scopeUserIds.contains(u.getUserId()))
                .map(this::toUserDomain)
                .collect(Collectors.toList());
    }

    public Position savePosition(Position position) {
        PositionDO existing = positionMapper.selectById(position.tenantId(), position.positionId());
        PositionDO data = toPositionData(position);
        if (existing == null) {
            positionMapper.insert(data);
        } else {
            positionMapper.update(data);
        }
        return position;
    }

    public Optional<Position> findPosition(String tenantId, String positionId) {
        return Optional.ofNullable(positionMapper.selectById(tenantId, positionId)).map(this::toPositionDomain);
    }

    public List<Position> listPositions(String tenantId) {
        return positionMapper.selectByTenant(tenantId).stream().map(this::toPositionDomain).collect(Collectors.toList());
    }

    public List<Position> listPositionsByOrg(String tenantId, String orgId) {
        return positionMapper.selectByOrg(tenantId, orgId).stream().map(this::toPositionDomain).collect(Collectors.toList());
    }

    public ObjectPermission saveObjectPermission(ObjectPermission perm) {
        objectPermissionMapper.insert(toObjectPermissionData(perm));
        return perm;
    }

    public void removeObjectPermission(String objPermId) {
        objectPermissionMapper.deleteById(objPermId);
    }

    public List<ObjectPermission> listObjectPermissions(String tenantId, String userId) {
        return objectPermissionMapper.selectByUserId(tenantId, userId).stream().map(this::toObjectPermissionDomain).collect(Collectors.toList());
    }

    public List<ObjectPermission> findObjectPermissions(String tenantId, String userId, String resourceType) {
        List<ObjectPermissionDO> allPerms;
        if (userId != null) {
            allPerms = objectPermissionMapper.selectByUserId(tenantId, userId);
        } else {
            allPerms = objectPermissionMapper.selectByTenant(tenantId);
        }
        return allPerms.stream()
                .filter(d -> resourceType == null || resourceType.equals(d.getResourceType()))
                .map(this::toObjectPermissionDomain)
                .collect(Collectors.toList());
    }

    private Set<String> parsePermIds(String permIds) {
        if (permIds == null || permIds.isBlank()) return Set.of();
        return Arrays.stream(permIds.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    private String joinPermIds(Set<String> permIds) {
        if (permIds == null || permIds.isEmpty()) return "";
        return String.join(",", permIds);
    }

    private TenantDO toTenantData(Tenant t) {
        TenantDO data = new TenantDO();
        data.setTenantId(t.tenantId());
        data.setName(t.name());
        data.setCode(t.code());
        data.setStatus(t.status().name());
        data.setPlan(t.plan());
        data.setExpireAt(t.expireAt());
        data.setCreatedAt(t.createdAt() != null ? t.createdAt() : Instant.now());
        data.setUpdatedAt(t.updatedAt() != null ? t.updatedAt() : Instant.now());
        return data;
    }

    private Tenant toTenantDomain(TenantDO d) {
        return new Tenant(d.getTenantId(), d.getName(), d.getCode(),
                TenantStatus.valueOf(d.getStatus()), d.getPlan(), d.getExpireAt(),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private UserAccountDO toUserData(UserAccount u) {
        UserAccountDO data = new UserAccountDO();
        data.setUserId(u.userId());
        data.setTenantId(u.tenantId());
        data.setOrgId(u.orgId());
        data.setUsername(u.username());
        data.setEmail(u.email());
        data.setPhone(u.phone());
        data.setPasswordHash(u.passwordHash());
        data.setEnabled(u.enabled());
        data.setStatus(u.status());
        data.setLastLogin(u.lastLogin());
        data.setCreatedAt(u.createdAt() != null ? u.createdAt() : Instant.now());
        data.setUpdatedAt(u.updatedAt() != null ? u.updatedAt() : Instant.now());
        return data;
    }

    private UserAccount toUserDomain(UserAccountDO d) {
        return new UserAccount(d.getUserId(), d.getTenantId(), d.getOrgId(), d.getUsername(),
                d.getEmail(), d.getPhone(), d.getPasswordHash(),
                d.getEnabled() != null ? d.getEnabled() : true,
                d.getStatus(), d.getLastLogin(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private RoleDO toRoleData(Role r) {
        RoleDO data = new RoleDO();
        data.setRoleId(r.roleId());
        data.setTenantId(r.tenantId());
        data.setName(r.name());
        data.setCode(r.code());
        data.setType(r.type());
        data.setStatus(r.status());
        data.setPermIds(joinPermIds(r.permIds()));
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(r.updatedAt() != null ? r.updatedAt() : Instant.now());
        return data;
    }

    private Role toRoleDomain(RoleDO d) {
        return new Role(d.getRoleId(), d.getTenantId(), d.getName(), d.getCode(),
                d.getType(), d.getStatus(), parsePermIds(d.getPermIds()),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private UserRoleDO toUserRoleData(UserRole ur) {
        UserRoleDO data = new UserRoleDO();
        data.setUserId(ur.userId());
        data.setRoleId(ur.roleId());
        data.setOrgId(ur.orgId());
        data.setCreatedAt(ur.createdAt() != null ? ur.createdAt() : Instant.now());
        return data;
    }

    private UserRole toUserRoleDomain(UserRoleDO d) {
        return new UserRole(d.getUserId(), d.getRoleId(), d.getOrgId(), d.getCreatedAt());
    }

    private AuthTokenDO toAuthTokenData(AuthToken t) {
        AuthTokenDO data = new AuthTokenDO();
        data.setTokenHash(t.token());
        data.setTenantId(t.tenantId());
        data.setUserId(t.userId());
        data.setUsername(t.username());
        data.setPermissions(joinPermIds(t.permissions()));
        data.setExpiresAt(t.expiresAt());
        data.setCreatedAt(Instant.now());
        return data;
    }

    private AuthToken toAuthTokenDomain(AuthTokenDO d) {
        return new AuthToken(d.getTokenHash(), d.getTenantId(), d.getUserId(), d.getUsername(),
                parsePermIds(d.getPermissions()), d.getExpiresAt());
    }

    private AuditLogDO toAuditLogData(AuditLog a) {
        AuditLogDO data = new AuditLogDO();
        data.setAuditId(a.auditId());
        data.setTenantId(a.tenantId());
        data.setActor(a.actor());
        data.setAction(a.action());
        data.setModule(a.module());
        data.setTarget(a.target());
        data.setTraceId(a.traceId());
        data.setSuccess(a.success());
        data.setOccurredAt(a.occurredAt());
        return data;
    }

    private AuditLog toAuditLogDomain(AuditLogDO d) {
        return new AuditLog(d.getAuditId(), d.getTenantId(), d.getActor(), d.getAction(),
                d.getModule(), d.getTarget(), d.getTraceId(),
                d.getSuccess() != null ? d.getSuccess() : false, d.getOccurredAt());
    }

    private UserDataScopeDO toDataScopeData(UserDataScope s) {
        UserDataScopeDO data = new UserDataScopeDO();
        data.setScopeId(s.scopeId());
        data.setUserId(s.userId());
        data.setTenantId(s.tenantId());
        data.setResourceType(s.resourceType());
        data.setResourceIds(s.resourceIds() != null ? String.join(",", s.resourceIds()) : "");
        data.setScopeType(s.scopeType());
        return data;
    }

    private UserDataScope toDataScopeDomain(UserDataScopeDO d) {
        Set<String> ids = d.getResourceIds() != null && !d.getResourceIds().isBlank()
                ? Arrays.stream(d.getResourceIds().split(",")).collect(Collectors.toSet())
                : Set.of();
        return new UserDataScope(d.getScopeId(), d.getUserId(), d.getTenantId(),
                d.getResourceType(), ids, d.getScopeType());
    }

    private OrganizationDO toOrganizationData(Organization o) {
        OrganizationDO data = new OrganizationDO();
        data.setOrgId(o.orgId());
        data.setTenantId(o.tenantId());
        data.setName(o.name());
        data.setParentOrgId(o.parentOrgId());
        data.setType(o.type());
        data.setPath(o.path());
        data.setLevel(o.level());
        data.setManagerId(o.managerId());
        data.setCreatedAt(o.createdAt() != null ? o.createdAt() : Instant.now());
        data.setUpdatedAt(o.updatedAt() != null ? o.updatedAt() : Instant.now());
        return data;
    }

    private Organization toOrganizationDomain(OrganizationDO d) {
        return new Organization(d.getOrgId(), d.getTenantId(), d.getName(), d.getParentOrgId(),
                d.getType(), d.getPath(), d.getLevel() != null ? d.getLevel() : 1,
                d.getManagerId(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private PermissionDO toPermissionData(Permission p) {
        PermissionDO data = new PermissionDO();
        data.setPermId(p.permId());
        data.setResource(p.resource());
        data.setAction(p.action());
        data.setName(p.name());
        data.setParentCode(p.parentCode());
        data.setStatus(p.status());
        data.setCreatedAt(p.createdAt() != null ? p.createdAt() : Instant.now());
        return data;
    }

    private Permission toPermissionDomain(PermissionDO d) {
        return new Permission(d.getPermId(), d.getResource(), d.getAction(), d.getName(),
                d.getParentCode(), d.getStatus(), d.getCreatedAt());
    }

    private DepartmentDO toDepartmentData(Department d) {
        DepartmentDO data = new DepartmentDO();
        data.setDeptId(d.deptId());
        data.setTenantId(d.tenantId());
        data.setName(d.name());
        data.setParentDeptId(d.parentDeptId());
        data.setOrgId(d.orgId());
        data.setManagerId(d.managerId());
        data.setEnabled(d.enabled());
        data.setCreatedAt(d.createdAt() != null ? d.createdAt() : Instant.now());
        data.setUpdatedAt(d.updatedAt() != null ? d.updatedAt() : Instant.now());
        return data;
    }

    private Department toDepartmentDomain(DepartmentDO d) {
        return new Department(d.getDeptId(), d.getTenantId(), d.getName(), d.getParentDeptId(),
                d.getOrgId(), d.getManagerId(), d.getEnabled() != null ? d.getEnabled() : true,
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private PositionDO toPositionData(Position p) {
        PositionDO data = new PositionDO();
        data.setPositionId(p.positionId());
        data.setTenantId(p.tenantId());
        data.setOrgId(p.orgId());
        data.setName(p.name());
        data.setCode(p.code());
        data.setLevel(p.level());
        data.setParentId(p.parentId());
        data.setStatus(p.status());
        data.setCreatedAt(p.createdAt() != null ? p.createdAt() : Instant.now());
        data.setUpdatedAt(p.updatedAt() != null ? p.updatedAt() : Instant.now());
        return data;
    }

    private Position toPositionDomain(PositionDO d) {
        return new Position(d.getPositionId(), d.getTenantId(), d.getOrgId(), d.getName(),
                d.getCode(), d.getLevel() != null ? d.getLevel() : 1, d.getParentId(),
                d.getStatus(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private ObjectPermissionDO toObjectPermissionData(ObjectPermission p) {
        ObjectPermissionDO data = new ObjectPermissionDO();
        data.setObjPermId(p.objPermId());
        data.setTenantId(p.tenantId());
        data.setUserId(p.userId());
        data.setResourceType(p.resourceType());
        data.setResourceId(p.resourceId());
        data.setPermissions(p.permissions() != null ? String.join(",", p.permissions()) : "");
        data.setGrantedBy(p.grantedBy());
        data.setCreatedAt(p.createdAt() != null ? p.createdAt() : Instant.now());
        return data;
    }

    private ObjectPermission toObjectPermissionDomain(ObjectPermissionDO d) {
        List<String> perms = d.getPermissions() != null && !d.getPermissions().isBlank()
                ? Arrays.stream(d.getPermissions().split(",")).collect(Collectors.toList())
                : List.of();
        return new ObjectPermission(d.getObjPermId(), d.getTenantId(), d.getUserId(),
                d.getResourceType(), d.getResourceId(), perms, d.getGrantedBy(), d.getCreatedAt());
    }
}

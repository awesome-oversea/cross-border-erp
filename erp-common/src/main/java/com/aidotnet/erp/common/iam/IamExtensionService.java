package com.aidotnet.erp.common.iam;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IamExtensionService {

    private static final Logger log = LoggerFactory.getLogger(IamExtensionService.class);
    private final Map<String, TenantInfo> tenants = new ConcurrentHashMap<>();
    private final Map<String, OrgUnit> orgUnits = new ConcurrentHashMap<>();
    private final Map<String, RoleDefinition> roleDefinitions = new ConcurrentHashMap<>();
    private final Map<String, DataPermission> dataPermissions = new ConcurrentHashMap<>();

    public TenantInfo createTenant(String tenantName, String plan, String adminEmail,
                                    String countryCode, int maxUsers, boolean active) {
        String tenantId = "TENANT-" + System.currentTimeMillis();
        TenantInfo tenant = new TenantInfo(tenantId, tenantName, plan, adminEmail,
                countryCode, maxUsers, active, Instant.now(), Instant.now());
        tenants.put(tenantId, tenant);
        log.info("Created tenant: id={}, name={}, plan={}", tenantId, tenantName, plan);
        return tenant;
    }

    public TenantInfo updateTenant(String tenantId, String plan, Integer maxUsers, Boolean active) {
        TenantInfo existing = tenants.get(tenantId);
        if (existing == null) throw new IllegalArgumentException("Tenant not found: " + tenantId);
        TenantInfo updated = new TenantInfo(tenantId, existing.tenantName(),
                plan != null ? plan : existing.plan(),
                existing.adminEmail(), existing.countryCode(),
                maxUsers != null ? maxUsers : existing.maxUsers(),
                active != null ? active : existing.active(),
                existing.createdAt(), Instant.now());
        tenants.put(tenantId, updated);
        log.info("Updated tenant: id={}", tenantId);
        return updated;
    }

    public List<TenantInfo> listTenants(String plan) {
        return tenants.values().stream()
                .filter(t -> plan == null || plan.equals(t.plan()))
                .toList();
    }

    public OrgUnit createOrgUnit(String tenantId, String unitName, String parentId,
                                  String unitType, String manager, int sortOrder) {
        String unitId = "ORG-" + System.currentTimeMillis();
        OrgUnit unit = new OrgUnit(unitId, tenantId, unitName, parentId,
                unitType, manager, sortOrder, true, Instant.now(), Instant.now());
        orgUnits.put(unitId, unit);
        log.info("Created org unit: id={}, name={}, type={}", unitId, unitName, unitType);
        return unit;
    }

    public OrgUnit updateOrgUnit(String unitId, String manager, Boolean active) {
        OrgUnit existing = orgUnits.get(unitId);
        if (existing == null) throw new IllegalArgumentException("Org unit not found: " + unitId);
        OrgUnit updated = new OrgUnit(unitId, existing.tenantId(), existing.unitName(),
                existing.parentId(), existing.unitType(),
                manager != null ? manager : existing.manager(),
                existing.sortOrder(),
                active != null ? active : existing.active(),
                existing.createdAt(), Instant.now());
        orgUnits.put(unitId, updated);
        log.info("Updated org unit: id={}", unitId);
        return updated;
    }

    public List<OrgUnit> listOrgUnits(String tenantId, String parentId) {
        return orgUnits.values().stream()
                .filter(u -> tenantId == null || tenantId.equals(u.tenantId()))
                .filter(u -> parentId == null || parentId.equals(u.parentId()))
                .toList();
    }

    public List<OrgUnit> getOrgTree(String tenantId) {
        return orgUnits.values().stream()
                .filter(u -> tenantId.equals(u.tenantId()) && u.active())
                .sorted((a, b) -> Integer.compare(a.sortOrder(), b.sortOrder()))
                .toList();
    }

    public RoleDefinition createRole(String tenantId, String roleName, String roleCode,
                                      String description, List<String> permissions,
                                      boolean isSystem) {
        String roleId = "ROLE-" + System.currentTimeMillis();
        RoleDefinition role = new RoleDefinition(roleId, tenantId, roleName, roleCode,
                description, permissions, isSystem, true, Instant.now(), Instant.now());
        roleDefinitions.put(roleId, role);
        log.info("Created role: id={}, name={}, permissions={}", roleId, roleName, permissions.size());
        return role;
    }

    public RoleDefinition updateRolePermissions(String roleId, List<String> permissions) {
        RoleDefinition existing = roleDefinitions.get(roleId);
        if (existing == null) throw new IllegalArgumentException("Role not found: " + roleId);
        RoleDefinition updated = new RoleDefinition(roleId, existing.tenantId(),
                existing.roleName(), existing.roleCode(), existing.description(),
                permissions, existing.isSystem(), existing.active(),
                existing.createdAt(), Instant.now());
        roleDefinitions.put(roleId, updated);
        log.info("Updated role permissions: id={}, count={}", roleId, permissions.size());
        return updated;
    }

    public List<RoleDefinition> listRoles(String tenantId) {
        return roleDefinitions.values().stream()
                .filter(r -> tenantId == null || tenantId.equals(r.tenantId()))
                .toList();
    }

    public DataPermission createDataPermission(String tenantId, String roleId, String userId,
                                                String dimension, String scope, List<String> scopeValues) {
        String permId = "DP-" + System.currentTimeMillis();
        DataPermission perm = new DataPermission(permId, tenantId, roleId, userId,
                dimension, scope, scopeValues, Instant.now());
        dataPermissions.put(permId, perm);
        log.info("Created data permission: id={}, dimension={}, scope={}", permId, dimension, scope);
        return perm;
    }

    public List<DataPermission> listDataPermissions(String tenantId, String userId, String dimension) {
        return dataPermissions.values().stream()
                .filter(p -> tenantId == null || tenantId.equals(p.tenantId()))
                .filter(p -> userId == null || userId.equals(p.userId()))
                .filter(p -> dimension == null || dimension.equals(p.dimension()))
                .toList();
    }

    public record TenantInfo(String tenantId, String tenantName, String plan, String adminEmail,
                              String countryCode, int maxUsers, boolean active,
                              Instant createdAt, Instant updatedAt) {}
    public record OrgUnit(String unitId, String tenantId, String unitName, String parentId,
                           String unitType, String manager, int sortOrder, boolean active,
                           Instant createdAt, Instant updatedAt) {}
    public record RoleDefinition(String roleId, String tenantId, String roleName, String roleCode,
                                  String description, List<String> permissions,
                                  boolean isSystem, boolean active, Instant createdAt, Instant updatedAt) {}
    public record DataPermission(String permId, String tenantId, String roleId, String userId,
                                  String dimension, String scope, List<String> scopeValues,
                                  Instant createdAt) {}
}

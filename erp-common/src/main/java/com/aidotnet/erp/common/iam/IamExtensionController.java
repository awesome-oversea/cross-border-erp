package com.aidotnet.erp.common.iam;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform/iam/api/v1")
public class IamExtensionController {

    private final IamExtensionService iamExtService;

    public IamExtensionController(IamExtensionService iamExtService) {
        this.iamExtService = iamExtService;
    }

    @PostMapping("/tenants")
    public Result<IamExtensionService.TenantInfo> createTenant(@RequestBody Map<String, Object> request) {
        return Result.ok(iamExtService.createTenant(
                (String) request.get("tenantName"),
                (String) request.getOrDefault("plan", "STANDARD"),
                (String) request.getOrDefault("adminEmail", ""),
                (String) request.getOrDefault("countryCode", "CN"),
                request.containsKey("maxUsers") ? ((Number) request.get("maxUsers")).intValue() : 10,
                !request.containsKey("active") || (Boolean) request.get("active")
        ));
    }

    @PutMapping("/tenants/{tenantId}")
    public Result<IamExtensionService.TenantInfo> updateTenant(
            @PathVariable String tenantId, @RequestBody Map<String, Object> request) {
        Integer maxUsers = request.containsKey("maxUsers") ? ((Number) request.get("maxUsers")).intValue() : null;
        Boolean active = request.containsKey("active") ? (Boolean) request.get("active") : null;
        return Result.ok(iamExtService.updateTenant(tenantId, (String) request.get("plan"), maxUsers, active));
    }

    @GetMapping("/tenants")
    public Result<List<IamExtensionService.TenantInfo>> listTenants(
            @RequestParam(required = false) String plan) {
        return Result.ok(iamExtService.listTenants(plan));
    }

    @PostMapping("/org-units")
    public Result<IamExtensionService.OrgUnit> createOrgUnit(@RequestBody Map<String, Object> request) {
        return Result.ok(iamExtService.createOrgUnit(
                (String) request.get("tenantId"),
                (String) request.get("unitName"),
                (String) request.getOrDefault("parentId", ""),
                (String) request.getOrDefault("unitType", "DEPARTMENT"),
                (String) request.getOrDefault("manager", ""),
                request.containsKey("sortOrder") ? ((Number) request.get("sortOrder")).intValue() : 0
        ));
    }

    @PutMapping("/org-units/{unitId}")
    public Result<IamExtensionService.OrgUnit> updateOrgUnit(
            @PathVariable String unitId, @RequestBody Map<String, Object> request) {
        Boolean active = request.containsKey("active") ? (Boolean) request.get("active") : null;
        return Result.ok(iamExtService.updateOrgUnit(unitId, (String) request.get("manager"), active));
    }

    @GetMapping("/org-units")
    public Result<List<IamExtensionService.OrgUnit>> listOrgUnits(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String parentId) {
        return Result.ok(iamExtService.listOrgUnits(tenantId, parentId));
    }

    @GetMapping("/org-units/tree")
    public Result<List<IamExtensionService.OrgUnit>> getOrgTree(@RequestParam String tenantId) {
        return Result.ok(iamExtService.getOrgTree(tenantId));
    }

    @PostMapping("/roles")
    public Result<IamExtensionService.RoleDefinition> createRole(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) request.getOrDefault("permissions", List.of());
        return Result.ok(iamExtService.createRole(
                (String) request.get("tenantId"),
                (String) request.get("roleName"),
                (String) request.get("roleCode"),
                (String) request.getOrDefault("description", ""),
                permissions,
                request.containsKey("isSystem") && (Boolean) request.get("isSystem")
        ));
    }

    @PutMapping("/roles/{roleId}/permissions")
    public Result<IamExtensionService.RoleDefinition> updateRolePermissions(
            @PathVariable String roleId, @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) request.getOrDefault("permissions", List.of());
        return Result.ok(iamExtService.updateRolePermissions(roleId, permissions));
    }

    @GetMapping("/roles")
    public Result<List<IamExtensionService.RoleDefinition>> listRoles(
            @RequestParam(required = false) String tenantId) {
        return Result.ok(iamExtService.listRoles(tenantId));
    }

    @PostMapping("/data-permissions")
    public Result<IamExtensionService.DataPermission> createDataPermission(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> scopeValues = (List<String>) request.getOrDefault("scopeValues", List.of());
        return Result.ok(iamExtService.createDataPermission(
                (String) request.get("tenantId"),
                (String) request.getOrDefault("roleId", ""),
                (String) request.getOrDefault("userId", ""),
                (String) request.get("dimension"),
                (String) request.getOrDefault("scope", "CUSTOM"),
                scopeValues
        ));
    }

    @GetMapping("/data-permissions")
    public Result<List<IamExtensionService.DataPermission>> listDataPermissions(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String dimension) {
        return Result.ok(iamExtService.listDataPermissions(tenantId, userId, dimension));
    }
}

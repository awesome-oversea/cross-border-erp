package com.aidotnet.erp.iam.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.security.DataScope;
import com.aidotnet.erp.common.security.DataScopeResolver;
import com.aidotnet.erp.iam.domain.ObjectPermission;
import com.aidotnet.erp.iam.domain.UserDataScope;
import com.aidotnet.erp.iam.infrastructure.IamStore;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * 数据权限范围服务
 * <p>
 * 描述: IAM域数据权限解析服务，负责将用户的数据权限范围定义解析为
 *       可用于业务查询的数据过滤条件。实现DataScopeResolver接口，
 *       供全局数据权限拦截器调用。
 * </p>
 * <p>
 * 数据权限维度(10维):
 *   1. org - 组织维度，控制可访问的组织范围
 *   2. department - 部门维度，控制可访问的部门范围
 *   3. store - 店铺维度，控制可访问的店铺范围
 *   4. marketplace - 市场维度，控制可访问的市场范围
 *   5. channel - 渠道维度，控制可访问的渠道范围
 *   6. warehouse - 仓库维度，控制可访问的仓库范围
 *   7. supplier - 供应商维度，控制可访问的供应商范围
 *   8. category - 品类维度，控制可访问的品类范围
 *   9. data_level - 数据层级(SUMMARY/DETAIL/MASKED)，控制数据可见粒度
 * </p>
 * <p>
 * 对象级权限(第10维):
 *   10. object_permission - 业务对象级权限(产品/Listing/广告等)，
 *       支持用户自定义产品角色，可查看全部或仅与自己相关的产品。
 *       通过 checkObjectPermission 方法实现校验。
 * </p>
 * <p>
 * 数据层级(DataLevel):
 *   - SUMMARY: 仅查看汇总数据，不可查看明细
 *   - DETAIL: 查看明细数据(默认)
 *   - MASKED: 查看已脱敏的明细数据
 * </p>
 *
 * @author ERP系统
 * @see DataScope
 * @see DataScopeResolver
 */
@Service
public class DataScopeService implements DataScopeResolver {

    private final IamStore iamStore;

    /**
     * 构造函数 - 依赖注入IAM数据存储
     *
     * @param iamStore IAM数据存储
     */
    public DataScopeService(IamStore iamStore) {
        this.iamStore = iamStore;
    }

    /**
     * 解析用户的数据权限范围
     * <p>
     * 根据用户ID查找所有数据权限定义，按资源类型分组后构建DataScope对象。
     * 无数据权限定义时返回默认范围(空集合+DETAIL层级)。
     * </p>
     * <p>
     * 返回的DataScope包含完整的10维数据权限信息，SQL拦截器或业务方法
     * 可以通过 DataScopeResolver 获取用户的权限范围，在查询时自动追加
     * 租户隔离和数据权限筛选条件。
     * </p>
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 数据权限范围对象，包含各维度的资源ID集合
     */
    @Override
    public DataScope resolve(String tenantId, String userId) {
        Set<UserDataScope> scopes = iamStore.findDataScopes(userId);
        if (scopes.isEmpty()) {
            // 默认：无数据权限约束时赋予DETAIL层级，表示可见全部明细数据
            return new DataScope(tenantId, Set.of(), Set.of(), Set.of(), Set.of(), Set.of(),
                    Set.of(), Set.of(), Set.of(), DataScope.DataLevel.DETAIL);
        }
        return new DataScope(
                tenantId,
                filterByType(scopes, "org"),
                filterByType(scopes, "department"),
                filterByType(scopes, "store"),
                filterByType(scopes, "marketplace"),
                filterByType(scopes, "channel"),
                filterByType(scopes, "warehouse"),
                filterByType(scopes, "supplier"),
                filterByType(scopes, "category"),
                resolveDataLevel(scopes)
        );
    }

    /**
     * 设置用户数据权限范围
     * <p>
     * 为指定用户设置某个资源类型的数据权限范围。
     * 同一用户同一资源类型只保留一条记录(scopeId格式: ds-{userId}-{resourceType})。
     * </p>
     *
     * @param userId       用户ID
     * @param tenantId     租户ID
     * @param resourceType 资源类型，如 org、department、store、warehouse
     * @param resourceIds  资源ID集合，CUSTOM类型时有效
     * @param scopeType    范围类型: ALL(全部访问)/DEPT(本部门)/DEPT_AND_SUB(本部门及下级)/CUSTOM(自定义)
     */
    public void setDataScope(String userId, String tenantId, String resourceType, Set<String> resourceIds, String scopeType) {
        String scopeId = "ds-" + userId + "-" + resourceType;
        iamStore.saveDataScope(new UserDataScope(scopeId, userId, tenantId, resourceType, resourceIds, scopeType));
    }

    /**
     * 校验用户对特定业务对象的操作权限
     * <p>
     * 对象级权限(10维中的第10维)用于产品、Listing、广告等业务对象的精细权限控制。
     * 支持两种模式:
     *   1. 全部可见: 用户拥有该资源类型的ALL范围，可访问所有对象
     *   2. 自定义可见: 用户只能访问resourceIds中指定的对象
     * 业务规则:
     *   - 无数据权限配置时默认放行(兼容简单场景)
     *   - 有"product"资源类型的ALL范围时，可见所有产品
     *   - 有"product"资源类型的CUSTOM范围时，只可见resourceIds中的产品
     *   - 资源类型匹配和*通配符(表示拥有该资源所有操作权限)
     *
     * @param tenantId     租户ID
     * @param userId       用户ID
     * @param resourceType 资源类型(product/listing/ad等)
     * @param resourceId   资源ID
     * @param action       操作类型(read/write/delete等)
     * @return true=有权限, false=无权限
     */
    @Override
    public boolean checkObjectPermission(String tenantId, String userId, String resourceType, String resourceId, String action) {
        Set<UserDataScope> scopes = iamStore.findDataScopes(userId);
        // 无数据权限配置时默认放行
        if (scopes.isEmpty()) {
            return true;
        }
        // 优先通过对象级权限(ObjectPermission)校验
        List<ObjectPermission> objPerms = iamStore.findObjectPermissions(tenantId, userId, resourceType);
        for (ObjectPermission perm : objPerms) {
            if (perm.resourceId().equals(resourceId)) {
                // *表示拥有该资源的所有操作权限
                return perm.permissions().contains(action) || perm.permissions().contains("*");
            }
        }
        // 回退到数据权限范围校验: 检查是否ALL范围或resourceIds中是否包含
        return scopes.stream()
                .filter(s -> s.resourceType().equals(resourceType))
                .anyMatch(s -> "ALL".equalsIgnoreCase(s.scopeType()) || s.resourceIds().contains(resourceId));
    }

    /** 按资源类型过滤数据权限范围，提取资源ID集合 */
    private Set<String> filterByType(Set<UserDataScope> scopes, String type) {
        return scopes.stream()
                .filter(s -> s.resourceType().equals(type))
                .flatMap(s -> s.resourceIds().stream())
                .collect(Collectors.toSet());
    }

    /**
     * 解析数据层级，默认为DETAIL(明细)
     * <p>
     * 数据层级控制用户可见的数据粒度:
     *   DETAIL - 可见完整明细数据
     *   SUMMARY - 仅可见汇总统计，不可查看明细
     *   MASKED - 可见已脱敏的明细数据(手机号/邮箱等敏感字段被遮挡)
     * </p>
     */
    private DataScope.DataLevel resolveDataLevel(Set<UserDataScope> scopes) {
        return scopes.stream()
                .filter(s -> "data_level".equals(s.resourceType()))
                .findFirst()
                .map(s -> {
                    try {
                        return DataScope.DataLevel.valueOf(s.scopeType().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return DataScope.DataLevel.DETAIL;
                    }
                })
                .orElse(DataScope.DataLevel.DETAIL);
    }
}

package com.aidotnet.erp.common.security;

/**
 * 数据权限解析器接口
 * <p>
 * 描述: 定义数据权限解析与校验的统一契约。
 *       实现类(IAM域DataScopeService)负责将用户的数据权限范围解析为可用于业务查询的过滤条件。
 * </p>
 * <p>
 * 10维数据权限维度:
 *   tenant/org/department/store/marketplace/channel/warehouse/supplier/category/data_level
 * </p>
 * <p>
 * 对象级权限: 支持产品、Listing、广告等业务对象的精细权限控制，
 *            通过objectPermissionCheck方法校验用户对特定业务对象的操作权限。
 * </p>
 *
 * @author ERP系统
 */
public interface DataScopeResolver {

    /**
     * 解析用户在指定租户下的完整数据权限范围
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 数据权限范围对象(包含10维维度)
     */
    DataScope resolve(String tenantId, String userId);

    /**
     * 校验用户对特定业务对象的操作权限
     * <p>
     * 对象级权限用于产品、Listing、广告等业务对象的精细权限控制。
     * 支持用户自定义产品角色（数据权限），可查看全部或仅与自己相关的产品。
     * </p>
     *
     * @param tenantId     租户ID
     * @param userId       用户ID
     * @param resourceType 资源类型(product/listing/ad等)
     * @param resourceId   资源ID
     * @param action       操作类型(read/write/delete等)
     * @return true=有权限, false=无权限
     */
    default boolean checkObjectPermission(String tenantId, String userId, String resourceType, String resourceId, String action) {
        // 默认实现：无对象级权限约束时返回true(放行)
        return true;
    }
}

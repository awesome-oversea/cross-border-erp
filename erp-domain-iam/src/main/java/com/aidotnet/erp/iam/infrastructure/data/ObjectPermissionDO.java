package com.aidotnet.erp.iam.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 对象权限数据对象
 * <p>
 * 描述: 对应iam_object_permission表，存储细粒度对象级权限。
 *       控制用户对特定业务对象的操作权限，优先级高于角色权限。
 * </p>
 *
 * @author ERP系统
 */
@TableName("iam_object_permission")
public class ObjectPermissionDO {

    /** 对象权限唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String objPermId;
    /** 租户ID */
    private String tenantId;
    /** 用户ID */
    private String userId;
    /** 资源类型 */
    private String resourceType;
    /** 资源实例ID */
    private String resourceId;
    /** 允许的操作列表，逗号分隔 */
    private String permissions;
    /** 授权人用户ID */
    private String grantedBy;
    /** 创建时间 */
    private Instant createdAt;

    public ObjectPermissionDO() {
    }

    public String getObjPermId() { return objPermId; }
    public void setObjPermId(String objPermId) { this.objPermId = objPermId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
    public String getGrantedBy() { return grantedBy; }
    public void setGrantedBy(String grantedBy) { this.grantedBy = grantedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

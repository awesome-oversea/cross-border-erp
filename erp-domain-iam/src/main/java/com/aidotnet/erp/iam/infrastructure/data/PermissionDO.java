package com.aidotnet.erp.iam.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 权限数据对象
 * <p>
 * 描述: 对应iam_permission表，存储细粒度权限定义。
 *       权限编码格式: {域}:{资源}:{操作}，如 iam:tenant:read
 * </p>
 *
 * @author ERP系统
 */
@TableName("iam_permission")
public class PermissionDO {

    /** 权限唯一标识，格式: {域}:{资源}:{操作} */
    @TableId(type = IdType.ASSIGN_ID)
    private String permId;
    /** 资源类型，如 tenant、user、role */
    private String resource;
    /** 操作类型: read(读)/write(写) */
    private String action;
    /** 权限名称 */
    private String name;
    /** 父权限编码，写权限的父权限为对应读权限 */
    private String parentCode;
    /** 权限状态: active(启用)/inactive(停用) */
    private String status;
    /** 创建时间 */
    private Instant createdAt;

    public PermissionDO() {
    }

    public String getPermId() { return permId; }
    public void setPermId(String permId) { this.permId = permId; }
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) { this.parentCode = parentCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

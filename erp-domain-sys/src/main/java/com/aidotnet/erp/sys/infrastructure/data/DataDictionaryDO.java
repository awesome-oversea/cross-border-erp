package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 数据字典数据对象(DataDictionaryDO)
 * <p>
 * 描述: 数据字典数据对象，对应sys_data_dictionary表。
 *       存储系统全局数据字典项，为各子域提供标准化的枚举值和选项集。
 *       支持树形结构（通过parentCode关联父子项）和按类型分组管理。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一租户下(dictType, dictCode)组合必须唯一
 *   2. enabled=false的字典项在业务查询时自动过滤
 *   3. parentCode为空表示顶级字典项，非空表示子项
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_data_dictionary
 *   - 主键: dict_id (ASSIGN_ID策略)
 *   - 唯一约束: (tenant_id, dict_type, dict_code)
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_data_dictionary")
public class DataDictionaryDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String dictId;
    private String tenantId;
    private String dictCode;
    private String dictName;
    private String dictType;
    private String parentCode;
    private int sortOrder;
    private boolean enabled;
    private String remark;
    private Instant createdAt;
    private Instant updatedAt;

    public DataDictionaryDO() {}

    public String getDictId() { return dictId; }
    public void setDictId(String dictId) { this.dictId = dictId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getDictCode() { return dictCode; }
    public void setDictCode(String dictCode) { this.dictCode = dictCode; }
    public String getDictName() { return dictName; }
    public void setDictName(String dictName) { this.dictName = dictName; }
    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }
    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) { this.parentCode = parentCode; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

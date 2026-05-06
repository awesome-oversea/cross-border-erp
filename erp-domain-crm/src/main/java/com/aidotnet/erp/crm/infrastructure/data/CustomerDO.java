package com.aidotnet.erp.crm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 客户数据对象
 * <p>
 * 描述: 对应crm_customer表，存储客户基本信息和消费统计。
 *       tags字段以JSON字符串形式存储CustomerTag列表。
 * </p>
 *
 * @author ERP系统
 */
@TableName("crm_customer")
public class CustomerDO {

    /** 客户ID(主键) */
    @TableId(type = IdType.ASSIGN_ID)
    private String customerId;
    /** 租户ID */
    private String tenantId;
    /** 客户姓名 */
    private String name;
    /** 邮箱 */
    private String email;
    /** 电话 */
    private String phone;
    /** 国家编码 */
    private String countryCode;
    /** 所属平台 */
    private String platform;
    /** 所属店铺ID */
    private String storeId;
    /** 总订单数 */
    private Integer totalOrders;
    /** 总消费金额 */
    private BigDecimal totalSpent;
    /** 最后下单时间 */
    private Instant lastOrderAt;
    /** 客户标签(JSON格式) */
    private String tags;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public CustomerDO() {}

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
    public Instant getLastOrderAt() { return lastOrderAt; }
    public void setLastOrderAt(Instant lastOrderAt) { this.lastOrderAt = lastOrderAt; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

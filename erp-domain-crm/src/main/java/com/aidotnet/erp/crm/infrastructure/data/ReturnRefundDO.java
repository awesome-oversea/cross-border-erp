package com.aidotnet.erp.crm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 退货退款数据对象
 * <p>
 * 描述: 对应crm_return_refund表，存储退货退款记录。
 *       reason、status字段以枚举名称字符串存储。
 * </p>
 *
 * @author ERP系统
 */
@TableName("crm_return_refund")
public class ReturnRefundDO {

    /** 退货ID(主键) */
    @TableId(type = IdType.ASSIGN_ID)
    private String returnId;
    /** 租户ID */
    private String tenantId;
    /** 关联订单ID */
    private String orderId;
    /** 卖家SKU */
    private String sellerSku;
    /** 客户ID */
    private String customerId;
    /** 退货数量 */
    private Integer quantity;
    /** 退款金额 */
    private BigDecimal refundAmount;
    /** 币种 */
    private String currency;
    /** 退货原因(枚举名称) */
    private String reason;
    /** 退货状态(枚举名称) */
    private String status;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public ReturnRefundDO() {}

    public String getReturnId() { return returnId; }
    public void setReturnId(String returnId) { this.returnId = returnId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

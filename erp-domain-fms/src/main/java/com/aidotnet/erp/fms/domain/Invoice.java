package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 发票领域模型
 * <p>
 * 描述: FMS域实体，记录发票信息。支持销售发票和采购发票，
 *       关联凭证(voucherId)实现业财一体化。
 *       状态流转: DRAFT → ISSUED → PAID → CANCELLED
 * </p>
 *
 * @author ERP系统
 * @see InvoiceStatus
 * @see Voucher
 */
public record Invoice(
        /** 发票ID */
        String invoiceId,
        /** 租户ID */
        String tenantId,
        /** 发票号码 */
        String invoiceNumber,
        /** 发票类型(SALES/PURCHASE) */
        String invoiceType,
        /** 客户ID */
        String customerId,
        /** 客户名称 */
        String customerName,
        /** 国家编码(用于VAT计算) */
        String countryCode,
        /** 币种 */
        String currency,
        /** 小计金额(不含税) */
        BigDecimal subtotalAmount,
        /** 税额 */
        BigDecimal taxAmount,
        /** 总金额(含税) */
        BigDecimal totalAmount,
        /** 税号 */
        String taxIdNumber,
        /** 发票状态 */
        String status,
        /** 关联凭证ID */
        String voucherId,
        /** 开票日期 */
        Instant invoiceDate,
        /** 到期日期 */
        Instant dueDate,
        /** 备注 */
        String remark,
        /** 创建时间 */
        Instant createdAt,
        /** 更新时间 */
        Instant updatedAt
) {}

package com.aidotnet.erp.fms.domain;

import java.time.Instant;

/**
 * 自定义发票设置领域模型
 * <p>
 * 描述: FMS财务域的发票配置，支持多店铺/多市场维度的发票模板和税务参数设置。
 *       系统根据配置在出账时自动生成合规发票。
 * </p>
 * <p>
 * 配置项:
 *   1. 发票模板(关联打印模板)
 *   2. 发票抬头(店铺名称/公司名称)
 *   3. 税务信息(VAT/GST税号、欧洲VAT、北美销售税等)
 *   4. 显示项配置(单价/税率/折扣等)
 * </p>
 * <p>
 * 关联实体:
 *   - Invoice: 实际生成的发票记录
 *   - VoucherTemplate: 凭证模板(用于生成会计凭证)
 *   - TaxRule: 税率规则(用于自动计税)
 * </p>
 *
 * @author ERP系统
 */
public record InvoiceSetting(
        String settingId,
        String tenantId,
        String storeId,
        String marketplaceId,
        /** 发票模板ID(关联SYS域的PrintTemplate) */
        String templateId,
        /** 发票抬头 */
        String invoiceTitle,
        /** 税务登记号(VAT/GST等) */
        String taxRegistrationNo,
        /** 是否显示单价明细 */
        boolean showUnitPrice,
        /** 是否显示税率 */
        boolean showTaxRate,
        /** 是否显示折扣 */
        boolean showDiscount,
        /** 备注信息 */
        String remark,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}

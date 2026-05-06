package com.aidotnet.erp.scm.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 供应商报价领域模型
 * <p>
 * 描述: 供应商对SKU的报价记录，包含单价、总价、交期和有效期。
 * </p>
 *
 * @author ERP系统
 */
public record Quote(String quoteId, String tenantId, String supplierId, String sellerSku, String currency,
                    BigDecimal unitPrice, BigDecimal totalPrice, int quotedQuantity, int leadTimeDays,
                    QuoteStatus status, Instant validUntil, String remark, Instant createdAt, Instant updatedAt) {}

package com.aidotnet.erp.fba.domain;

import java.math.BigDecimal;

/**
 * 移除明细领域模型
 * <p>
 * 描述: FBA移除订单的明细行，记录移除数量、处置费和清仓收入。
 * </p>
 *
 * @author ERP系统
 */
public record RemovalItem(
        String itemId,
        String removalId,
        String fbaSku,
        int quantityRemoved,
        BigDecimal disposalFee,
        BigDecimal liquidationRevenue
) {}

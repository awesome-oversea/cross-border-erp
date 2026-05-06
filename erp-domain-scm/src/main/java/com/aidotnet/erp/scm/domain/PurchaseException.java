package com.aidotnet.erp.scm.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 采购异常领域模型
 * <p>
 * 描述: 采购过程中的异常记录，如超交/短交/涨价/损坏/错发等。
 * </p>
 *
 * @author ERP系统
 */
public record PurchaseException(
        String exceptionId,
        String tenantId,
        String poId,
        String lineId,
        String sellerSku,
        PurchaseExceptionType exceptionType,
        BigDecimal expectedValue,
        BigDecimal actualValue,
        String description,
        PurchaseExceptionStatus status,
        String handlerId,
        String handlerNote,
        Instant createdAt,
        Instant handledAt
) {}

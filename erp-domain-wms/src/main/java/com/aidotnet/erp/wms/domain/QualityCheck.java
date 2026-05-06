package com.aidotnet.erp.wms.domain;

import java.time.Instant;

/**
 * 质检记录领域模型
 * <p>
 * 描述: 入库质检记录，记录抽样数量、合格/不合格数量和质检结果。
 * </p>
 *
 * @author ERP系统
 */
public record QualityCheck(String checkId, String tenantId, String warehouseId, String inboundOrderId,
                           String sellerSku, int sampleQuantity, int passQuantity, int failQuantity,
                           QualityCheckResult result, String inspector, String remark,
                           Instant checkedAt, Instant createdAt) {}

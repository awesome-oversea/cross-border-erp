package com.aidotnet.erp.som.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 销售告警领域模型
 * <p>
 * 描述: 销售运营异常告警实体，当店铺指标超出阈值时自动触发。
 *       支持多级别告警(LOW/MEDIUM/HIGH/CRITICAL)，需人工确认。
 * </p>
 *
 * @param alertId        告警唯一标识
 * @param tenantId       租户ID
 * @param storeId        关联店铺ID
 * @param alertType      告警类型: low_stock/price_anomaly/high_return等
 * @param severity       严重程度: LOW/MEDIUM/HIGH/CRITICAL
 * @param message        告警消息
 * @param relatedSku     关联SKU编码
 * @param metricValue    当前指标值
 * @param thresholdValue 阈值
 * @param acknowledged   是否已确认
 * @param createdAt      创建时间
 * @author ERP系统
 */
public record SalesAlert(String alertId, String tenantId, String storeId, String alertType,
                         String severity, String message, String relatedSku,
                         BigDecimal metricValue, BigDecimal thresholdValue,
                         boolean acknowledged, Instant createdAt) {}

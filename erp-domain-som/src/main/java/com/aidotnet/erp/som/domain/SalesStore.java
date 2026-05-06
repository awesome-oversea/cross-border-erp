package com.aidotnet.erp.som.domain;

import java.time.Instant;

/**
 * 店铺领域模型
 * <p>
 * 描述: 销售运营域基础实体，表示一个跨境外卖店铺(如Amazon US店铺)。
 *       每个店铺关联一个平台和站点，是Listing和销售数据的归属主体。
 * </p>
 * <p>
 * 状态流转: CREATED(已创建) → CONNECTED(已连接) → DISABLED(已停用)
 * </p>
 *
 * @param storeId     店铺唯一标识
 * @param tenantId    租户ID
 * @param platform    销售平台: amazon/ebay/shopify/shopee等
 * @param storeCode   平台店铺编码，租户+平台内唯一
 * @param storeName   店铺名称
 * @param status      状态: CREATED/CONNECTED/DISABLED
 * @param createdAt   创建时间
 * @param updatedAt   更新时间
 * @author ERP系统
 */
public record SalesStore(String storeId, String tenantId, String platform, String storeCode, String storeName,
                         StoreStatus status, Instant createdAt, Instant updatedAt) {}

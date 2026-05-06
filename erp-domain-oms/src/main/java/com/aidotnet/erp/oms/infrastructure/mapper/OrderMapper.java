package com.aidotnet.erp.oms.infrastructure.mapper;

import com.aidotnet.erp.oms.infrastructure.data.BuyerBlacklistDO;
import com.aidotnet.erp.oms.infrastructure.data.OriginalOrderSnapshotDO;
import com.aidotnet.erp.oms.infrastructure.data.OrderDO;
import com.aidotnet.erp.oms.infrastructure.data.OrderLineDO;
import com.aidotnet.erp.oms.infrastructure.data.OrderRefundDO;
import com.aidotnet.erp.oms.infrastructure.data.OrderRiskCheckDO;
import com.aidotnet.erp.oms.infrastructure.data.OrderStrategyDO;
import com.aidotnet.erp.oms.infrastructure.data.OrderSyncLogDO;
import com.aidotnet.erp.oms.infrastructure.data.PmsRiskAlertDO;
import com.aidotnet.erp.oms.infrastructure.data.PromotionDO;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * OMS域订单数据MyBatis映射器
 * <p>
 * 描述: 订单域核心数据访问层，提供订单、订单行、退款、风控、策略、
 *       同步日志、黑名单、促销、PMS告警等表的数据操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface OrderMapper {

    // ---- 订单 ----

    /** 新增订单 */
    void insert(OrderDO order);

    /** 更新订单 */
    void update(OrderDO order);

    /** 按订单ID+租户ID查询 */
    OrderDO selectById(@Param("orderId") String orderId, @Param("tenantId") String tenantId);

    /** 按平台订单号查询(唯一性校验) */
    OrderDO selectByPlatformOrderNo(@Param("tenantId") String tenantId, @Param("platform") String platform,
                                    @Param("platformOrderNo") String platformOrderNo);

    /** 按租户ID查询订单列表 */
    List<OrderDO> selectByTenant(@Param("tenantId") String tenantId);

    /** 按买家查询近期订单(风控重复下单检查) */
    List<OrderDO> selectRecentOrdersByBuyer(@Param("tenantId") String tenantId, @Param("platform") String platform,
                                            @Param("buyerName") String buyerName, @Param("createdAfter") Instant createdAfter);

    // ---- 原始订单快照 ----

    void insertOriginalOrderSnapshot(OriginalOrderSnapshotDO snapshot);

    void updateOriginalOrderSnapshot(OriginalOrderSnapshotDO snapshot);

    OriginalOrderSnapshotDO selectOriginalOrderSnapshot(@Param("tenantId") String tenantId, @Param("snapshotId") String snapshotId);

    // ---- 订单行 ----

    /** 新增订单行 */
    void insertOrderLine(OrderLineDO line);

    /** 删除订单行(更新时先删后插) */
    void deleteOrderLines(@Param("orderId") String orderId);

    /** 按订单ID查询订单行 */
    List<OrderLineDO> selectOrderLines(@Param("orderId") String orderId);

    // ---- 退款 ----

    /** 新增退款记录 */
    void insertRefund(OrderRefundDO refund);

    /** 更新退款记录 */
    void updateRefund(OrderRefundDO refund);

    /** 按退款ID查询 */
    OrderRefundDO selectRefundById(@Param("tenantId") String tenantId, @Param("refundId") String refundId);

    /** 按订单ID查询退款列表 */
    List<OrderRefundDO> selectRefunds(@Param("tenantId") String tenantId, @Param("orderId") String orderId);

    // ---- 风控检查 ----

    /** 新增风控检查记录 */
    void insertRiskCheck(OrderRiskCheckDO riskCheck);

    /** 按订单ID查询风控检查列表 */
    List<OrderRiskCheckDO> selectRiskChecks(@Param("tenantId") String tenantId, @Param("orderId") String orderId);

    // ---- 买家黑名单 ----

    /** 新增黑名单记录 */
    void insertBuyerBlacklist(BuyerBlacklistDO entry);

    /** 按记录ID查询黑名单 */
    BuyerBlacklistDO selectBuyerBlacklist(@Param("tenantId") String tenantId, @Param("entryId") String entryId);

    /** 按租户ID查询黑名单列表 */
    List<BuyerBlacklistDO> selectBuyerBlacklistByTenant(@Param("tenantId") String tenantId);

    /** 删除黑名单记录 */
    void deleteBuyerBlacklist(@Param("tenantId") String tenantId, @Param("entryId") String entryId);

    // ---- 促销 ----

    /** 新增促销记录 */
    void insertPromotion(PromotionDO promotion);

    /** 按订单ID查询促销列表 */
    List<PromotionDO> selectPromotions(@Param("tenantId") String tenantId, @Param("orderId") String orderId);

    // ---- PMS风控告警 ----

    /** 新增PMS风控告警 */
    void insertPmsRiskAlert(PmsRiskAlertDO alert);

    /** 按告警ID查询 */
    PmsRiskAlertDO selectPmsRiskAlert(@Param("tenantId") String tenantId, @Param("alertId") String alertId);

    /** 按幂等键查询(防重复接收) */
    PmsRiskAlertDO selectPmsRiskAlertByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    /** 按订单ID查询PMS告警列表 */
    List<PmsRiskAlertDO> selectPmsRiskAlertsByOrder(@Param("tenantId") String tenantId, @Param("orderId") String orderId);

    /** 查询待处理的PMS告警 */
    List<PmsRiskAlertDO> selectPendingPmsRiskAlerts(@Param("tenantId") String tenantId);

    /** 更新PMS告警状态 */
    void updatePmsRiskAlertStatus(@Param("alertId") String alertId, @Param("status") String status);

    // ---- 同步日志 ----

    /** 新增同步日志 */
    void insertOrderSyncLog(OrderSyncLogDO syncLog);

    /** 按租户+平台查询同步日志 */
    List<OrderSyncLogDO> selectOrderSyncLogs(@Param("tenantId") String tenantId, @Param("platform") String platform);

    // ---- 订单策略 ----

    /** 新增订单策略 */
    void insertOrderStrategy(OrderStrategyDO strategy);

    /** 按策略ID查询 */
    OrderStrategyDO selectOrderStrategy(@Param("tenantId") String tenantId, @Param("strategyId") String strategyId);

    /** 按策略类型查询策略列表 */
    List<OrderStrategyDO> selectOrderStrategies(@Param("tenantId") String tenantId, @Param("strategyType") String strategyType);

    /** 更新订单策略 */
    void updateOrderStrategy(OrderStrategyDO strategy);
}

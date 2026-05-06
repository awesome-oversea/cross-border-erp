package com.aidotnet.erp.dashboard.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * OMS域Feign客户端，用于工作台域与订单域的跨域通信。
 * <p>
 * 描述: 工作台域通过此客户端查询OMS域的订单数据，
 *       用于仪表盘指标展示、待办事项关联和AI洞察生成。
 * </p>
 * <p>
 * 跨域关联:
 *   - DASHBOARD → OMS: 查询订单详情(用于待办事项关联业务单据)
 *   - DASHBOARD → OMS: 查询订单统计(用于销售指标展示)
 *   - DASHBOARD → OMS: 按状态查询订单数(用于待办计数)
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "oms-client-dashboard", path = "/oms/api/in/v1")
public interface OmsClient {

    /**
     * 按订单ID查询订单详情。
     *
     * @param tenantId 租户ID
     * @param orderId  订单ID
     * @return 订单信息(包含订单号、金额、状态、商品列表等)
     */
    @GetMapping("/orders/{orderId}")
    Result<Map<String, Object>> getOrder(@RequestHeader("X-Tenant-Id") String tenantId,
                                         @PathVariable String orderId);

    /**
     * 查询订单统计摘要。
     * <p>
     * 返回指定时间范围内的订单数量、总金额等统计数据，
     * 用于仪表盘销售指标展示。
     * </p>
     *
     * @param tenantId 租户ID
     * @param startDate 开始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 订单统计(包含orderCount、totalAmount等)
     */
    @GetMapping("/orders/statistics")
    Result<Map<String, Object>> getOrderStatistics(@RequestHeader("X-Tenant-Id") String tenantId,
                                                    @RequestParam String startDate,
                                                    @RequestParam String endDate);

    /**
     * 按状态查询待处理订单数量。
     * <p>
     * 用于工作台待办事项计数展示。
     * </p>
     *
     * @param tenantId 租户ID
     * @param status   订单状态
     * @return 待处理订单数量
     */
    @GetMapping("/orders/count")
    Result<Map<String, Object>> countByStatus(@RequestHeader("X-Tenant-Id") String tenantId,
                                              @RequestParam String status);
}

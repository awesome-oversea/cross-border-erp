package com.aidotnet.erp.dashboard.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * FMS域Feign客户端，用于工作台域与财务域的跨域通信。
 * <p>
 * 描述: 工作台域通过此客户端查询FMS域的财务数据，
 *       用于仪表盘财务指标展示和AI洞察生成。
 * </p>
 * <p>
 * 跨域关联:
 *   - DASHBOARD → FMS: 查询财务统计(用于收入/支出指标展示)
 *   - DASHBOARD → FMS: 查询应收账款(用于财务预警洞察)
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "fms-client-dashboard", path = "/fms/api/in/v1")
public interface FmsClient {

    /**
     * 查询财务统计摘要。
     * <p>
     * 返回指定时间范围内的收入、支出、利润等统计数据，
     * 用于仪表盘财务指标展示。
     * </p>
     *
     * @param tenantId  租户ID
     * @param startDate 开始日期(yyyy-MM-dd)
     * @param endDate   结束日期(yyyy-MM-dd)
     * @return 财务统计(包含totalIncome、totalExpense、profit等)
     */
    @GetMapping("/finance/statistics")
    Result<Map<String, Object>> getFinanceStatistics(@RequestHeader("X-Tenant-Id") String tenantId,
                                                      @RequestParam String startDate,
                                                      @RequestParam String endDate);

    /**
     * 查询应收账款概要。
     * <p>
     * 返回逾期应收、待收金额等数据，用于AI洞察财务预警。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 应收账款概要(包含overdueAmount、pendingAmount等)
     */
    @GetMapping("/finance/receivable/summary")
    Result<Map<String, Object>> getReceivableSummary(@RequestHeader("X-Tenant-Id") String tenantId);

    /**
     * 按账单ID查询账单详情。
     *
     * @param tenantId 租户ID
     * @param billId   账单ID
     * @return 账单信息(包含金额、状态、到期日等)
     */
    @GetMapping("/bills/{billId}")
    Result<Map<String, Object>> getBill(@RequestHeader("X-Tenant-Id") String tenantId,
                                        @PathVariable String billId);
}

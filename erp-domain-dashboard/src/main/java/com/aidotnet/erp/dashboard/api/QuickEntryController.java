package com.aidotnet.erp.dashboard.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.dashboard.application.WorkspaceService;
import com.aidotnet.erp.dashboard.application.WorkspaceService.CreateQuickEntryCommand;
import com.aidotnet.erp.dashboard.application.WorkspaceService.UpdateQuickEntryCommand;
import com.aidotnet.erp.dashboard.domain.QuickEntry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 快捷入口入站控制器，提供快捷入口的RESTful API。
 * <p>
 * 描述: 管理用户自定义的快捷功能入口，支持创建、更新、删除和查询。
 *       快捷入口提供常用功能的快速访问通道，支持自定义排序和分类。
 * </p>
 * <p>
 * 接口规范:
 *   - 路径: /dashboard/api/in/v1/quick-entries
 *   - 认证: 需登录用户，通过Token获取租户ID
 *   - 返回: 统一Result包装，包含code/msg/data
 * </p>
 * <p>
 * 入口分类:
 *   - ORDER: 订单相关，如创建订单、查看订单
 *   - PRODUCT: 产品相关，如产品列表、库存查询
 *   - FINANCE: 财务相关，如收款管理、对账单
 *   - LOGISTICS: 物流相关，如发货管理、物流追踪
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/dashboard/api/in/v1/quick-entries")
public class QuickEntryController {

    private final WorkspaceService workspaceService;

    /**
     * 构造函数 - 依赖注入工作台服务
     *
     * @param workspaceService 工作台应用服务
     */
    public QuickEntryController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * 创建快捷入口。
     *
     * @param request 创建快捷入口请求体
     * @return 新创建的快捷入口
     */
    @PostMapping
    public Result<QuickEntry> create(@Valid @RequestBody CreateQuickEntryRequest request) {
        return Result.ok(workspaceService.createQuickEntry(currentTenant(), new CreateQuickEntryCommand(
                request.userId(), request.entryCode(), request.entryName(), request.icon(),
                request.url(), request.category(), request.sortOrder())));
    }

    /**
     * 更新快捷入口。
     *
     * @param entryId 入口ID
     * @param request 更新快捷入口请求体
     * @return 更新后的快捷入口
     */
    @PutMapping("/{entryId}")
    public Result<QuickEntry> update(@PathVariable String entryId, @Valid @RequestBody UpdateQuickEntryRequest request) {
        return Result.ok(workspaceService.updateQuickEntry(currentTenant(), entryId, new UpdateQuickEntryCommand(
                request.entryName(), request.icon(), request.url(), request.category(), request.sortOrder())));
    }

    /**
     * 删除快捷入口。
     *
     * @param entryId 入口ID
     * @return 操作结果
     */
    @DeleteMapping("/{entryId}")
    public Result<Void> delete(@PathVariable String entryId) {
        workspaceService.deleteQuickEntry(currentTenant(), entryId);
        return Result.ok(null);
    }

    /**
     * 查询用户快捷入口列表。
     * <p>
     * 按sortOrder升序排列，sortOrder越小越靠前。
     * </p>
     *
     * @param userId 用户ID
     * @return 快捷入口列表
     */
    @GetMapping
    public Result<List<QuickEntry>> list(@RequestParam String userId) {
        return Result.ok(workspaceService.listQuickEntries(currentTenant(), userId));
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID
     * @throws BizException TENANT_REQUIRED - 租户不能为空
     */
    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    /**
     * 创建快捷入口请求体
     *
     * @param userId    用户ID，必填
     * @param entryCode 入口编码，用户内唯一，必填
     * @param entryName 入口名称，必填
     * @param icon      图标标识，前端渲染用
     * @param url       跳转路径
     * @param category  入口分类，如 ORDER/PRODUCT/FINANCE/LOGISTICS
     * @param sortOrder 排序值，越小越靠前
     */
    public record CreateQuickEntryRequest(
            @NotBlank String userId, @NotBlank String entryCode, @NotBlank String entryName,
            String icon, String url, String category, int sortOrder) {}

    /**
     * 更新快捷入口请求体
     *
     * @param entryName 入口名称
     * @param icon      图标标识
     * @param url       跳转路径
     * @param category  入口分类
     * @param sortOrder 排序值
     */
    public record UpdateQuickEntryRequest(
            String entryName, String icon, String url, String category, int sortOrder) {}
}

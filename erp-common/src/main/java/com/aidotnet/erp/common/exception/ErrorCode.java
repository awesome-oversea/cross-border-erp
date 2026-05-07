package com.aidotnet.erp.common.exception;

/**
 * 统一错误码枚举
 * <p>
 * 描述: 系统全局错误码定义，覆盖14个业务域的通用错误场景。
 *       所有BizException应优先使用此枚举中的错误码，确保错误码统一。
 * </p>
 * <p>
 * 命名规范: {域前缀}_{具体错误}，如 OMS_ORDER_NOT_FOUND、SCM_SUPPLIER_NOT_FOUND
 * </p>
 * <p>
 * 使用方式:
 *   throw new BizException(ErrorCode.OMS_ORDER_NOT_FOUND, "订单不存在");
 * </p>
 *
 * @author ERP系统
 */
public enum ErrorCode {
    // ========== 通用 ==========
    /** 操作成功 */
    SUCCESS,
    /** 请求参数错误 */
    BAD_REQUEST,
    /** 服务器内部错误 */
    INTERNAL_ERROR,
    /** 缺少租户上下文 */
    TENANT_REQUIRED,
    /** 租户不存在 */
    TENANT_NOT_FOUND,
    /** 租户已存在 */
    TENANT_EXISTS,
    /** 租户已停用 */
    TENANT_DISABLED,
    /** 租户已过期 */
    TENANT_EXPIRED,
    /** 未认证(未登录或Token无效) */
    UNAUTHORIZED,
    /** 权限不足 */
    FORBIDDEN,
    /** 资源不存在 */
    NOT_FOUND,
    /** 状态不允许操作 */
    INVALID_STATUS,
    /** 幂等Key冲突(重复请求) */
    IDEMPOTENCY_CONFLICT,
    /** 被限流 */
    RATE_LIMITED,
    /** 任务已禁用 */
    JOB_DISABLED,

    // ========== IAM 组织权限域 ==========
    /** 用户已存在 */
    USER_EXISTS,
    /** 用户不存在 */
    USER_NOT_FOUND,
    /** 用户名或密码错误 */
    AUTH_FAILED,
    /** 原密码不正确 */
    PASSWORD_MISMATCH,
    /** 角色已存在 */
    ROLE_EXISTS,
    /** 角色不存在 */
    ROLE_NOT_FOUND,
    /** 上级组织不存在 */
    PARENT_ORG_NOT_FOUND,
    /** 组织不存在 */
    ORG_NOT_FOUND,
    /** 上级部门不存在 */
    PARENT_DEPT_NOT_FOUND,
    /** 部门不存在 */
    DEPT_NOT_FOUND,
    /** 岗位不存在 */
    POSITION_NOT_FOUND,
    /** 对象权限不存在 */
    OBJECT_PERMISSION_NOT_FOUND,
    /** 数据权限策略不存在 */
    DATA_SCOPE_NOT_FOUND,

    // ========== PDM 产品开发域 ==========
    /** 类目不存在 */
    CATEGORY_NOT_FOUND,
    /** 品牌不存在 */
    BRAND_NOT_FOUND,
    /** SPU不存在 */
    SPU_NOT_FOUND,
    /** SPU状态不允许 */
    SPU_STATUS_INVALID,
    /** SKU已存在 */
    SKU_DUPLICATED,
    /** 产品存在知识产权冲突，无法上架 */
    IP_CONFLICT,
    /** 上架前至少需要一个SKU */
    SKU_REQUIRED,
    /** 选品提报不存在 */
    PROPOSAL_NOT_FOUND,
    /** 选品提报状态不允许 */
    PROPOSAL_STATUS_INVALID,
    /** 开发流程不存在 */
    DEV_NOT_FOUND,
    /** 开发阶段不允许跳级或逆行 */
    DEV_STAGE_INVALID,
    /** 开发流程状态不允许 */
    DEV_STATUS_INVALID,
    /** 产品变体不存在 */
    VARIANT_NOT_FOUND,
    /** 产品变体状态不允许 */
    VARIANT_STATUS_INVALID,
    /** 产品采集不存在 */
    COLLECTION_NOT_FOUND,
    /** 产品采集状态不允许 */
    COLLECTION_STATUS_INVALID,
    /** 敏感词不存在 */
    SENSITIVE_WORD_NOT_FOUND,
    /** 知识产权不存在 */
    IP_NOT_FOUND,
    /** 知识产权状态不允许 */
    IP_STATUS_INVALID,
    /** UPC码不存在 */
    UPC_NOT_FOUND,
    /** UPC码状态不允许 */
    UPC_STATUS_INVALID,
    /** 没有可用的UPC码 */
    NO_AVAILABLE_UPC,
    /** 质检标准不存在 */
    QUALITY_STANDARD_NOT_FOUND,

    // ========== SOM 销售运营域 ==========
    /** Listing不存在 */
    LISTING_NOT_FOUND,
    /** 平台Listing已存在 */
    LISTING_DUPLICATED,
    /** Listing已上架 */
    LISTING_ALREADY_ACTIVE,
    /** Listing状态不允许操作 */
    LISTING_STATUS_INVALID,
    /** 渠道SKU不存在 */
    CHANNEL_SKU_NOT_FOUND,
    /** 渠道SKU映射已存在 */
    CHANNEL_SKU_DUPLICATED,
    /** 渠道SKU冲突(被其他内部SKU占用) */
    CHANNEL_SKU_CONFLICT,
    /** 价格规则不存在 */
    PRICE_RULE_NOT_FOUND,
    /** 价格计算参数异常(佣金率与目标利润率之和需小于1) */
    PRICE_CALCULATION_INVALID,
    /** PMS建议不存在 */
    SUGGESTION_NOT_FOUND,
    /** PMS建议状态不允许 */
    SUGGESTION_STATUS_INVALID,
    /** 店铺已存在 */
    STORE_DUPLICATED,
    /** 店铺不存在 */
    STORE_NOT_FOUND,

    // ========== ADS 广告管理域 ==========
    /** 广告活动不存在 */
    CAMPAIGN_NOT_FOUND,
    /** 广告活动预算必须大于0 */
    CAMPAIGN_BUDGET_REQUIRED,
    /** 关键词出价不存在 */
    KEYWORD_BID_NOT_FOUND,
    /** 广告组不存在 */
    AD_GROUP_NOT_FOUND,
    /** 广告关键词不存在 */
    AD_KEYWORD_NOT_FOUND,

    // ========== OMS 订单域 ==========
    /** 订单不存在 */
    ORDER_NOT_FOUND,
    /** 订单已存在 */
    ORDER_DUPLICATED,
    /** 订单状态不允许操作 */
    ORDER_STATUS_INVALID,
    /** 订单明细不能为空 */
    ORDER_LINE_REQUIRED,
    /** 履约计划不存在 */
    FULFILLMENT_PLAN_NOT_FOUND,
    /** 履约计划状态不允许调整 */
    FULFILLMENT_PLAN_STATUS_INVALID,
    /** 履约包裹收货仓库不能为空 */
    FULFILLMENT_WAREHOUSE_REQUIRED,
    /** 履约包裹物流商不能为空 */
    FULFILLMENT_CARRIER_REQUIRED,
    /** 无待发货的履约包裹 */
    FULFILLMENT_PACKAGE_NOT_READY,
    /** 订单策略不存在 */
    STRATEGY_NOT_FOUND,
    /** 黑名单买家已存在 */
    BLACKLIST_DUPLICATED,
    /** 风险检测订单不存在 */
    RISK_CHECK_NOT_FOUND,

    // ========== SCM 供应链域 ==========
    /** 供应商不存在 */
    SUPPLIER_NOT_FOUND,
    /** 供应商已停用或未生效 */
    SUPPLIER_DISABLED,
    /** 采购单不存在 */
    PO_NOT_FOUND,
    /** 采购单状态不允许操作 */
    PO_STATUS_INVALID,
    /** 采购明细不能为空 */
    PO_LINE_REQUIRED,
    /** 采购模式不能为空 */
    PURCHASE_TYPE_REQUIRED,
    /** 不支持的采购模式 */
    PURCHASE_TYPE_INVALID,
    /** 采购审批流不存在 */
    PURCHASE_APPROVAL_FLOW_REQUIRED,
    /** 付款申请金额无效 */
    PAYMENT_REQUEST_AMOUNT_INVALID,
    /** 收货数量超过采购数量 */
    PO_RECEIVE_EXCEEDS_ORDERED,
    /** 采购计划不存在 */
    PURCHASE_PLAN_NOT_FOUND,
    /** 采购计划无需求可生成 */
    PURCHASE_PLAN_EMPTY,
    /** 采购计划行不存在 */
    PURCHASE_PLAN_LINE_NOT_FOUND,
    /** 采购计划行已转单 */
    PURCHASE_PLAN_LINE_ALREADY_ORDERED,
    /** 采购计划行无可执行采购量 */
    PURCHASE_PLAN_LINE_NOT_ACTIONABLE,
    /** 采购计划行重复转单 */
    PURCHASE_PLAN_LINE_DUPLICATED,
    /** 采购单价必须大于0 */
    PURCHASE_PLAN_LINE_COST_INVALID,
    /** 补货建议不存在 */
    SUGGESTION_NOT_FOUND_SCM,
    /** 补货建议状态不允许 */
    SUGGESTION_STATUS_INVALID_SCM,
    /** 跟单已存在 */
    TRACKING_ALREADY_EXISTS,
    /** 跟单不存在 */
    TRACKING_NOT_FOUND,
    /** 采购异常不存在 */
    EXCEPTION_NOT_FOUND,
    /** 采购异常已处理 */
    EXCEPTION_STATUS_INVALID,
    /** 采购需求获取失败 */
    OMS_PROCUREMENT_DEMAND_FAILED,
    /** 可用库存获取失败 */
    WMS_AVAILABILITY_FAILED,
    /** 询价不存在 */
    QUOTE_NOT_FOUND,

    // ========== WMS 仓储域 ==========
    /** 仓库不存在 */
    WAREHOUSE_NOT_FOUND,
    /** 库存不存在 */
    INVENTORY_NOT_FOUND,
    /** 可用库存不足 */
    INVENTORY_NOT_ENOUGH,
    /** 释放数量超过预占库存 */
    INVENTORY_RELEASE_EXCEEDS_RESERVED,
    /** 扣减数量超过预占库存 */
    INVENTORY_DEDUCT_EXCEEDS_RESERVED,
    /** 入库单不存在 */
    INBOUND_ORDER_NOT_FOUND,
    /** 入库单状态不允许 */
    INBOUND_STATUS_INVALID,
    /** 入库明细不存在 */
    INBOUND_LINE_NOT_FOUND,
    /** 收货数量超过预期数量 */
    RECEIVE_EXCEEDS_EXPECTED,
    /** 出库单不存在 */
    OUTBOUND_ORDER_NOT_FOUND,
    /** 出库单状态不允许 */
    OUTBOUND_STATUS_INVALID,
    /** 出库明细不存在 */
    OUTBOUND_LINE_NOT_FOUND,
    /** 拣货数量超过需求数量 */
    PICK_EXCEEDS_REQUIRED,
    /** 拣货数量超过预占库存 */
    PICK_EXCEEDS_RESERVED,
    /** 包裹明细不能为空 */
    OUTBOUND_PACKAGE_LINES_REQUIRED,
    /** 包裹状态不允许 */
    OUTBOUND_PACKAGE_STATUS_INVALID,
    /** 包裹必须称重后才能发货 */
    OUTBOUND_PACKAGE_WEIGHT_REQUIRED,
    /** 出库明细未拣货 */
    OUTBOUND_LINE_NOT_PICKED,
    /** 装箱数量超过拣货数量 */
    PACKAGE_EXCEEDS_PICKED,
    /** 调出仓库不存在 */
    FROM_WAREHOUSE_NOT_FOUND,
    /** 调入仓库不存在 */
    TO_WAREHOUSE_NOT_FOUND,
    /** 调拨单不存在 */
    TRANSFER_ORDER_NOT_FOUND,
    /** 调拨单状态不允许 */
    TRANSFER_STATUS_INVALID,
    /** 调拨明细不存在 */
    TRANSFER_LINE_NOT_FOUND,
    /** 收货数量超过调拨数量 */
    RECEIVE_EXCEEDS_TRANSFER,
    /** 库存不足(含来源) */
    INSUFFICIENT_INVENTORY,
    /** 盘点单不存在 */
    CHECK_ORDER_NOT_FOUND,
    /** 盘点单状态不允许 */
    CHECK_STATUS_INVALID,
    /** 盘点明细不存在 */
    CHECK_LINE_NOT_FOUND,
    /** 还有明细未完成盘点 */
    NOT_ALL_COUNTED,
    /** 盘点已调整 */
    STOCK_CHECK_ALREADY_ADJUSTED,
    /** 盘点明细不存在 */
    STOCK_CHECK_DATA_INVALID,
    /** 不良库存不足 */
    DEFECTIVE_INVENTORY_NOT_ENOUGH,
    /** 退货单不存在 */
    DEFECTIVE_RETURN_NOT_FOUND,
    /** 退货单状态不允许 */
    DEFECTIVE_RETURN_STATUS_INVALID,
    /** 产品返修不存在 */
    PRODUCT_REPAIR_NOT_FOUND,
    /** 产品返修状态不允许 */
    PRODUCT_REPAIR_STATUS_INVALID,
    /** 返修入库数量超过出库数量 */
    PRODUCT_REPAIR_INBOUND_EXCEEDS_OUTBOUND,
    /** 质检样本数量不符 */
    QUALITY_SAMPLE_MISMATCH,
    /** 质检数量超过收货数量 */
    QUALITY_CHECK_EXCEEDS_RECEIVED,
    /** 入库单SKU未找到 */
    INBOUND_SKU_NOT_FOUND,
    /** 事件发布失败 */
    EVENT_PUBLISH_FAILED,

    // ========== TMS 物流域 ==========
    /** 物流商不存在 */
    CARRIER_NOT_FOUND,
    /** 运输方式不存在 */
    SHIPPING_METHOD_NOT_FOUND,
    /** 物流订单不存在 */
    SHIPMENT_NOT_FOUND,
    /** 物流订单状态不允许 */
    SHIPMENT_STATUS_INVALID,
    /** 运费计算异常 */
    FREIGHT_CALCULATION_FAILED,
    /** 轨迹号不存在 */
    TRACKING_NOT_FOUND_TMS,

    // ========== FBA 域 ==========
    /** FBA入库计划不存在 */
    INBOUND_PLAN_NOT_FOUND,
    /** FBA货件不存在 */
    FBA_SHIPMENT_NOT_FOUND,
    /** FBA货件状态不允许 */
    FBA_SHIPMENT_STATUS_INVALID,
    /** 收货数量超过计划数量 */
    RECEIVE_EXCEEDS_PLANNED,

    // ========== CRM 客服售后域 ==========
    /** 客户不存在 */
    CUSTOMER_NOT_FOUND,
    /** 工单不存在 */
    TICKET_NOT_FOUND,
    /** 工单状态不允许 */
    TICKET_STATUS_INVALID,
    /** 退货记录不存在 */
    RETURN_NOT_FOUND,
    /** 退货状态不允许 */
    RETURN_STATUS_INVALID,
    /** 留言不存在 */
    MESSAGE_NOT_FOUND,
    /** 评价不存在 */
    REVIEW_NOT_FOUND,

    // ========== FMS 财务域 ==========
    /** 成本事件不存在 */
    COST_EVENT_NOT_FOUND,
    /** 归集规则不存在 */
    RULE_NOT_FOUND,
    /** 利润结果不存在 */
    PROFIT_RESULT_NOT_FOUND,
    /** 利润偏差告警不存在 */
    ALERT_NOT_FOUND,
    /** 凭证不存在 */
    VOUCHER_NOT_FOUND,
    /** 凭证状态不允许 */
    INVALID_VOUCHER_STATUS,
    /** 付款请求不存在 */
    PAYMENT_REQUEST_NOT_FOUND,
    /** 付款请求状态不允许 */
    PAYMENT_REQUEST_STATUS_INVALID,
    /** 汇率不存在 */
    RATE_NOT_FOUND,
    /** 同步已禁用 */
    SYNC_DISABLED,
    /** 外部财务同步失败 */
    FINANCE_SYNC_FAILED,

    // ========== BI 商业智能域 ==========
    /** 报表不存在 */
    REPORT_NOT_FOUND,
    /** 指标口径不存在 */
    CALIBER_NOT_FOUND,
    /** 指标口径已存在 */
    CALIBER_EXISTS,
    /** KPI目标不存在 */
    TARGET_NOT_FOUND,
    /** KPI目标已禁用 */
    TARGET_DISABLED,
    /** 权重必须在0-100之间 */
    INVALID_WEIGHT,

    // ========== SYS 系统设置域 ==========
    /** 配置不存在 */
    CONFIG_NOT_FOUND,
    /** 配置键已存在 */
    CONFIG_DUPLICATED,
    /** 审核流程不存在 */
    FLOW_NOT_FOUND,
    /** 流程编码已存在 */
    FLOW_CODE_DUPLICATED,
    /** PMS建议不存在 */
    PMS_RECOMMENDATION_NOT_FOUND,
    /** PMS建议状态不允许流转 */
    PMS_STATUS_TRANSITION_INVALID,
    /** PMS请求参数无效 */
    PMS_REQUEST_INVALID,
    /** 不支持的ERP域 */
    PMS_DOMAIN_UNSUPPORTED,
    /** PMS只能写入建议/草稿/待审批动作/风险预警/洞察卡片 */
    PMS_OBJECT_TYPE_FORBIDDEN,
    /** PMS来源必须为PMS系统 */
    PMS_SOURCE_INVALID,
    /** PMS请求头缺失 */
    PMS_HEADER_MISSING,
    /** 该域AI功能已关闭 */
    PMS_FEATURE_DISABLED,
    /** agent模式下agent_id不能为空 */
    PMS_AGENT_ID_MISSING,
    /** 模板编码已存在 */
    TEMPLATE_CODE_DUPLICATED,
    /** 模板不存在 */
    TEMPLATE_NOT_FOUND,
    /** 审批实例不存在 */
    APPROVAL_NOT_FOUND,
    /** 审批状态不允许 */
    APPROVAL_STATUS_INVALID,
    /** 业务规则版本不存在 */
    RULE_VERSION_NOT_FOUND,
    /** 连接器不存在 */
    CONNECTOR_NOT_FOUND,
    /** 连接器类型不支持 */
    CONNECTOR_TYPE_UNSUPPORTED,
    /** 通知模板编码已存在 */
    NOTIFICATION_TEMPLATE_DUPLICATED,
    /** 发票设置不存在 */
    INVOICE_SETTING_NOT_FOUND,
    /** 脱敏规则不存在 */
    MASKING_RULE_NOT_FOUND,
    /** 合规规则不存在 */
    COMPLIANCE_RULE_NOT_FOUND
}

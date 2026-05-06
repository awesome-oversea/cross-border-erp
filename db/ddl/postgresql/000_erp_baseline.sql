-- =========================================================
-- 跨境电商ERP系统 - 数据库基线脚本
-- 描述: 项目初始化数据库创建脚本，包含所有子系统表结构定义
-- 注意: 项目未上线，此脚本为唯一数据库定义来源，无需迁移脚本
-- 规范: 遵循阿里Java开发手册数据库规约，表名小写下划线分隔
--       字段名小写下划线分隔，每表必有tenant_id实现多租户隔离
--       主键采用业务ID(VARCHAR)便于分布式环境使用
--       时间字段统一TIMESTAMPTZ(带时区)，金额字段统一NUMERIC(18,4)
-- =========================================================

SET client_encoding = 'UTF8';
SET timezone = 'UTC';

-- 创建各子系统Schema，按业务域隔离
CREATE SCHEMA IF NOT EXISTS platform;   -- 平台公共域
CREATE SCHEMA IF NOT EXISTS iam;        -- 身份与访问管理域
CREATE SCHEMA IF NOT EXISTS pdm;        -- 产品主数据域
CREATE SCHEMA IF NOT EXISTS som;        -- 销售运营域
CREATE SCHEMA IF NOT EXISTS ads;        -- 广告管理域
CREATE SCHEMA IF NOT EXISTS oms;        -- 订单管理域
CREATE SCHEMA IF NOT EXISTS scm;        -- 供应链管理域
CREATE SCHEMA IF NOT EXISTS wms;        -- 仓储管理域
CREATE SCHEMA IF NOT EXISTS fba;        -- FBA管理域
CREATE SCHEMA IF NOT EXISTS tms;        -- 运输管理域
CREATE SCHEMA IF NOT EXISTS crm;        -- 客户关系管理域
CREATE SCHEMA IF NOT EXISTS fms;        -- 财务管理域
CREATE SCHEMA IF NOT EXISTS bi;         -- 商业智能域
CREATE SCHEMA IF NOT EXISTS sys;        -- 系统管理域
CREATE SCHEMA IF NOT EXISTS dashboard;  -- 仪表盘域

SET search_path TO platform, iam, pdm, som, ads, oms, scm, wms, fba, tms, crm, fms, bi, sys, dashboard, public;

-- #########################################################
-- platform: 平台公共表
-- 包含审批、定时任务、通知、翻译、连接器注册、事件发件箱、幂等记录
-- #########################################################

-- 审批实例表: 记录各业务域提交的审批流程实例
CREATE TABLE IF NOT EXISTS platform.common_approval_instance (
    id BIGSERIAL PRIMARY KEY,
    approval_id VARCHAR(64) NOT NULL UNIQUE,           -- 审批实例业务ID
    tenant_id VARCHAR(64) NOT NULL,                    -- 租户ID
    business_type VARCHAR(64) NOT NULL,                -- 业务类型(PURCHASE_ORDER/PAYMENT_REQUEST等)
    business_id VARCHAR(64) NOT NULL,                  -- 业务单据ID
    applicant VARCHAR(128) NOT NULL,                   -- 申请人
    status VARCHAR(32) NOT NULL,                       -- 审批状态(PENDING/APPROVED/REJECTED/CANCELLED)
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_common_approval_instance_tenant_status ON platform.common_approval_instance (tenant_id, status);
COMMENT ON TABLE platform.common_approval_instance IS '审批实例表-记录各业务域提交的审批流程实例';

-- 审批历史表: 记录审批流程中每一步的操作记录
CREATE TABLE IF NOT EXISTS platform.common_approval_history (
    id BIGSERIAL PRIMARY KEY,
    approval_id VARCHAR(64) NOT NULL,
    actor VARCHAR(128) NOT NULL,                       -- 操作人
    action VARCHAR(64) NOT NULL,                       -- 操作类型(APPROVE/REJECT/DELEGATE/WITHDRAW)
    comment TEXT,                                      -- 审批意见
    status VARCHAR(32) NOT NULL,                       -- 操作后状态
    operated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_common_approval_history_approval ON platform.common_approval_history (approval_id, operated_at DESC);
COMMENT ON TABLE platform.common_approval_history IS '审批历史表-记录审批流程中每一步的操作记录';

-- 定时任务配置表
CREATE TABLE IF NOT EXISTS platform.common_scheduled_job (
    id BIGSERIAL PRIMARY KEY,
    job_code VARCHAR(128) NOT NULL UNIQUE,             -- 任务编码(唯一标识)
    cron_expression VARCHAR(128) NOT NULL,             -- Cron表达式
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
COMMENT ON TABLE platform.common_scheduled_job IS '定时任务配置表-管理系统中所有定时调度任务';

-- 定时任务执行日志表
CREATE TABLE IF NOT EXISTS platform.common_job_execution_log (
    id BIGSERIAL PRIMARY KEY,
    job_code VARCHAR(128) NOT NULL,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    executed_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_common_job_execution_log_job_time ON platform.common_job_execution_log (job_code, executed_at DESC);
COMMENT ON TABLE platform.common_job_execution_log IS '定时任务执行日志表-记录每次定时任务的执行结果';

-- 通知记录表
CREATE TABLE IF NOT EXISTS platform.common_notification_record (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,                      -- 通知渠道(EMAIL/SMS/WECHAT/DINGTALK)
    receiver VARCHAR(256) NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_common_notification_record_tenant_time ON platform.common_notification_record (tenant_id, sent_at DESC);
COMMENT ON TABLE platform.common_notification_record IS '通知记录表-记录系统发送的所有通知消息';

-- 翻译记录表
CREATE TABLE IF NOT EXISTS platform.common_translation_record (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    source_language VARCHAR(16) NOT NULL,              -- 源语言代码(如: zh-CN)
    target_language VARCHAR(16) NOT NULL,              -- 目标语言代码(如: en-US)
    source_text TEXT NOT NULL,
    translated_text TEXT,
    glossary_hit BOOLEAN,
    translated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_common_translation_record_tenant_time ON platform.common_translation_record (tenant_id, translated_at DESC);
COMMENT ON TABLE platform.common_translation_record IS '翻译记录表-记录多语言翻译请求及结果';

-- 连接器注册表: 注册系统中所有外部连接器(平台/支付/物流等)
CREATE TABLE IF NOT EXISTS platform.common_connector_registration (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    connector_code VARCHAR(64) NOT NULL,               -- 连接器编码(如: amazon-sp-api/paypal)
    name VARCHAR(128) NOT NULL,
    enabled BOOLEAN NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, connector_code)
);
COMMENT ON TABLE platform.common_connector_registration IS '连接器注册表-注册系统中所有外部连接器';

-- API客户端表: 管理外部系统接入的API密钥
CREATE TABLE IF NOT EXISTS platform.common_api_client (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    client_id VARCHAR(128) NOT NULL,
    secret_hash VARCHAR(255) NOT NULL,                 -- 客户端密钥哈希值
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, client_id)
);
COMMENT ON TABLE platform.common_api_client IS 'API客户端表-管理外部系统接入的API密钥';

-- 事件发件箱表: 实现事务性消息发布，确保业务操作与事件发布的一致性
CREATE TABLE IF NOT EXISTS platform.common_outbox_event (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL UNIQUE,              -- 事件业务ID
    tenant_id VARCHAR(64) NOT NULL,
    trace_id VARCHAR(64),                              -- 链路追踪ID
    event_type VARCHAR(128) NOT NULL,                  -- 事件类型(如: ORDER_CREATED)
    aggregate_id VARCHAR(64),                          -- 聚合根ID
    payload_json TEXT NOT NULL,                        -- 事件载荷JSON
    status VARCHAR(32) NOT NULL,                       -- 状态(PENDING/PUBLISHED/FAILED)
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_common_outbox_event_status_time ON platform.common_outbox_event (status, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_common_outbox_event_tenant_event ON platform.common_outbox_event (tenant_id, event_type, created_at DESC);
COMMENT ON TABLE platform.common_outbox_event IS '事件发件箱表-实现事务性消息发布，确保业务操作与事件发布的一致性';

-- 幂等记录表: 保证接口调用的幂等性，防止重复处理
CREATE TABLE IF NOT EXISTS platform.common_idempotency_record (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,             -- 幂等键(由调用方提供)
    fingerprint VARCHAR(255),                          -- 请求指纹(校验重复请求内容一致性)
    result_json TEXT,                                  -- 首次处理结果JSON
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, idempotency_key)
);
COMMENT ON TABLE platform.common_idempotency_record IS '幂等记录表-保证接口调用的幂等性，防止重复处理';

-- #########################################################
-- iam: 身份与访问管理域
-- 包含租户、用户、角色、权限、组织、部门、数据范围、审计日志
-- #########################################################

-- 租户表: 管理多租户信息
CREATE TABLE IF NOT EXISTS iam.iam_tenant (
    tenant_id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,                        -- 租户名称
    status VARCHAR(32) NOT NULL,                       -- 租户状态(ACTIVE/SUSPENDED/DELETED)
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
COMMENT ON TABLE iam.iam_tenant IS '租户表-管理多租户信息，每个租户对应一个独立的业务实体';

-- 用户账号表: 管理系统登录用户
CREATE TABLE IF NOT EXISTS iam.iam_user_account (
    user_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    username VARCHAR(64) NOT NULL,                     -- 用户名(登录名)
    password_hash VARCHAR(255) NOT NULL,               -- 密码哈希值(BCrypt加密)
    enabled BOOLEAN NOT NULL,
    roles TEXT,                                        -- 角色列表(逗号分隔)
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, username)
);
CREATE INDEX IF NOT EXISTS idx_iam_user_account_tenant ON iam.iam_user_account (tenant_id);
COMMENT ON TABLE iam.iam_user_account IS '用户账号表-管理系统登录用户';

-- 角色表: 定义系统角色及其关联权限
CREATE TABLE IF NOT EXISTS iam.iam_role (
    role_code VARCHAR(64) PRIMARY KEY,                 -- 角色编码(如: ADMIN/OPERATOR/VIEWER)
    name VARCHAR(128) NOT NULL,
    permissions TEXT NOT NULL                          -- 权限列表(逗号分隔)
);
COMMENT ON TABLE iam.iam_role IS '角色表-定义系统角色及其关联权限';

-- 权限表: 定义系统所有功能权限点
CREATE TABLE IF NOT EXISTS iam.iam_permission (
    permission_code VARCHAR(128) PRIMARY KEY,          -- 权限编码(如: ORDER:CREATE)
    name VARCHAR(128) NOT NULL,
    module VARCHAR(64),                                -- 所属模块(如: OMS/WMS/FMS)
    parent_code VARCHAR(128),                          -- 父权限编码(构建权限树)
    created_at TIMESTAMPTZ
);
COMMENT ON TABLE iam.iam_permission IS '权限表-定义系统所有功能权限点';

-- 组织表: 管理企业组织架构
CREATE TABLE IF NOT EXISTS iam.iam_organization (
    org_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    parent_org_id VARCHAR(64),                         -- 父组织ID(构建组织树)
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_iam_organization_tenant ON iam.iam_organization (tenant_id);
COMMENT ON TABLE iam.iam_organization IS '组织表-管理企业组织架构';

-- 部门表: 管理企业部门信息
CREATE TABLE IF NOT EXISTS iam.iam_department (
    dept_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    parent_dept_id VARCHAR(64),                        -- 父部门ID(构建部门树)
    org_id VARCHAR(64),                                -- 所属组织ID
    manager_id VARCHAR(64),                            -- 部门负责人用户ID
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_iam_department_tenant_org ON iam.iam_department (tenant_id, org_id);
COMMENT ON TABLE iam.iam_department IS '部门表-管理企业部门信息';

-- 数据范围表: 控制用户可访问的数据范围(行级权限)
CREATE TABLE IF NOT EXISTS iam.iam_data_scope (
    scope_id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    resource_type VARCHAR(64) NOT NULL,                -- 资源类型(WAREHOUSE/STORE/SKU)
    resource_ids TEXT,                                 -- 可访问资源ID列表(逗号分隔)
    scope_type VARCHAR(64)                             -- 范围类型(ALL/DEPT/SELF/CUSTOM)
);
CREATE INDEX IF NOT EXISTS idx_iam_data_scope_user ON iam.iam_data_scope (tenant_id, user_id);
COMMENT ON TABLE iam.iam_data_scope IS '数据范围表-控制用户可访问的数据范围(行级权限)';

-- 审计日志表: 记录系统中所有关键操作的审计轨迹
CREATE TABLE IF NOT EXISTS iam.iam_audit_log (
    audit_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    actor VARCHAR(128) NOT NULL,                       -- 操作人
    action VARCHAR(64) NOT NULL,                       -- 操作类型(CREATE/UPDATE/DELETE/LOGIN)
    module VARCHAR(64) NOT NULL,                       -- 操作模块
    target VARCHAR(255),                               -- 操作目标
    trace_id VARCHAR(64),                              -- 链路追踪ID
    success BOOLEAN NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_iam_audit_log_tenant_time ON iam.iam_audit_log (tenant_id, occurred_at DESC);
COMMENT ON TABLE iam.iam_audit_log IS '审计日志表-记录系统中所有关键操作的审计轨迹';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS iam.iam_user_role (
    user_id VARCHAR(64) NOT NULL,
    role_id VARCHAR(64) NOT NULL,
    org_id VARCHAR(64),                                -- 限定角色生效的组织范围
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, role_id)
);
COMMENT ON TABLE iam.iam_user_role IS '用户角色关联表-用户与角色的多对多关联';

-- #########################################################
-- sys: 系统管理域
-- 包含系统配置、数据字典、AI功能开关、物流规则、连接器配置/运维、
-- 发票/通知设置、操作日志、数据脱敏、审批流、单据编号、PMS集成、
-- 内容审核、业务规则版本/模拟/执行日志、Webhook、手工导入、
-- PMS草稿/反馈/数据可信规则、领域事件目录、业务告警、打印模板、合规
-- #########################################################

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys.sys_config (
    config_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    config_key VARCHAR(128) NOT NULL,                  -- 配置键(如: system.currency.base)
    config_value TEXT NOT NULL,
    description VARCHAR(255),
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, config_key)
);
COMMENT ON TABLE sys.sys_config IS '系统配置表-管理系统级参数配置';

-- PMS建议池表: 存储PMS(AI)产生的业务建议
CREATE TABLE IF NOT EXISTS sys.sys_pms_recommendation (
    erp_reference_id VARCHAR(64) PRIMARY KEY,          -- ERP侧引用ID(用于PMS回写关联)
    tenant_id VARCHAR(64) NOT NULL,
    recommendation_id VARCHAR(64) NOT NULL,            -- PMS建议ID
    domain VARCHAR(64) NOT NULL,                       -- 业务域(PDM/SOM/SCM等)
    recommendation_type VARCHAR(64) NOT NULL,          -- 建议类型(SELECTION/REPLENISHMENT/LISTING等)
    object_type VARCHAR(64),                           -- 目标对象类型(SPU/SKU/STORE)
    target_object_type VARCHAR(64),
    target_object_id VARCHAR(64),
    content TEXT,                                      -- 建议内容JSON
    score NUMERIC(18,4),                               -- 建议评分(0-100)
    confidence NUMERIC(18,4),                          -- 置信度(0-1)
    evidence_chain_id VARCHAR(64),
    data_sources TEXT,                                 -- 数据来源JSON
    risk_flags TEXT,                                   -- 风险标记JSON
    explainability TEXT,                               -- 可解释性说明
    requested_action VARCHAR(64),
    status VARCHAR(32) NOT NULL,                       -- 状态(PENDING/APPROVED/REJECTED/EXECUTED/EXPIRED)
    approval_policy VARCHAR(64),                       -- 审批策略(AUTO/MANUAL/SKIP)
    rejection_reason TEXT,
    execution_result TEXT,
    measured_result TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, recommendation_id)
);
CREATE INDEX IF NOT EXISTS idx_sys_pms_recommendation_target ON sys.sys_pms_recommendation (tenant_id, target_object_type, target_object_id);
COMMENT ON TABLE sys.sys_pms_recommendation IS 'PMS建议池表-存储PMS(AI)产生的业务建议';

-- 数据字典表
CREATE TABLE IF NOT EXISTS sys.sys_data_dictionary (
    dict_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    dict_code VARCHAR(128) NOT NULL,                   -- 字典编码
    dict_name VARCHAR(128) NOT NULL,
    dict_type VARCHAR(64) NOT NULL,                    -- 字典类型(SYSTEM/BUSINESS)
    parent_code VARCHAR(128),                          -- 父级字典编码(树形结构)
    sort_order INTEGER,
    enabled BOOLEAN NOT NULL,
    remark TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, dict_code)
);
COMMENT ON TABLE sys.sys_data_dictionary IS '数据字典表-管理系统中所有枚举值和下拉选项';

-- AI功能开关表: 控制PMS/AI功能的启用状态
CREATE TABLE IF NOT EXISTS sys.sys_ai_feature_toggle (
    toggle_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    feature_code VARCHAR(128) NOT NULL,                -- 功能编码(如: pms.auto_bid)
    feature_name VARCHAR(128) NOT NULL,
    domain VARCHAR(64),
    enabled BOOLEAN NOT NULL,
    description TEXT,
    config_json TEXT,                                  -- 功能配置JSON
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, feature_code)
);
COMMENT ON TABLE sys.sys_ai_feature_toggle IS 'AI功能开关表-控制PMS/AI功能的启用状态和配置';

-- 物流规则表: 定义物流渠道选择规则
CREATE TABLE IF NOT EXISTS sys.sys_logistics_rule (
    rule_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    country_code VARCHAR(16),                          -- 目的国家代码
    channel VARCHAR(64),                               -- 物流渠道编码
    weight_min_kg NUMERIC(18,4),
    weight_max_kg NUMERIC(18,4),
    base_cost NUMERIC(18,4),
    cost_per_kg NUMERIC(18,4),
    estimated_days_min INTEGER,
    estimated_days_max INTEGER,
    enabled BOOLEAN NOT NULL,
    priority INTEGER,                                  -- 优先级(数值越小越高)
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_logistics_rule_tenant_country_channel ON sys.sys_logistics_rule (tenant_id, country_code, channel, priority);
COMMENT ON TABLE sys.sys_logistics_rule IS '物流规则表-定义物流渠道选择规则(按国家/重量/渠道匹配)';

-- 连接器配置表
CREATE TABLE IF NOT EXISTS sys.sys_connector_config (
    config_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    connector_type VARCHAR(32) NOT NULL,               -- 连接器类型(PLATFORM/PAYMENT/LOGISTICS/TAX)
    platform VARCHAR(64) NOT NULL,                     -- 平台编码
    connector_name VARCHAR(128) NOT NULL,
    config TEXT,                                       -- 连接器配置JSON
    status VARCHAR(32) NOT NULL,                       -- 状态(ACTIVE/INACTIVE/ERROR)
    version VARCHAR(32),
    description TEXT,
    last_sync_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_connector_config_tenant_type ON sys.sys_connector_config (tenant_id, connector_type, platform);
COMMENT ON TABLE sys.sys_connector_config IS '连接器配置表-管理各平台/支付/物流连接器的配置信息';

-- 发票设置表
CREATE TABLE IF NOT EXISTS sys.sys_invoice_setting (
    setting_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    setting_type VARCHAR(32) NOT NULL,                 -- 设置类型(VAT/COMMERCIAL/PROFORMA)
    setting_name VARCHAR(128) NOT NULL,
    config TEXT,
    enabled BOOLEAN NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
COMMENT ON TABLE sys.sys_invoice_setting IS '发票设置表-管理发票相关配置';

-- 通知渠道设置表
CREATE TABLE IF NOT EXISTS sys.sys_notification_setting (
    setting_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,                      -- 通知渠道(EMAIL/SMS/WECHAT/DINGTALK/WEBHOOK)
    channel_name VARCHAR(128),
    rules TEXT,                                        -- 通知规则JSON
    enabled BOOLEAN NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
COMMENT ON TABLE sys.sys_notification_setting IS '通知渠道设置表-管理各通知渠道的配置';

-- 通知模板表
CREATE TABLE IF NOT EXISTS sys.sys_notification_template (
    template_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    template_code VARCHAR(64) NOT NULL,
    template_name VARCHAR(128) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    subject VARCHAR(256),                              -- 消息主题(邮件时使用)
    content TEXT NOT NULL,                             -- 模板内容(支持变量占位符)
    variables TEXT,                                    -- 变量定义JSON
    enabled BOOLEAN NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, template_code)
);
COMMENT ON TABLE sys.sys_notification_template IS '通知模板表-管理各渠道的通知消息模板';

-- 操作日志表: 记录用户在系统中的操作行为
CREATE TABLE IF NOT EXISTS sys.sys_operation_log (
    log_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    username VARCHAR(128),
    module VARCHAR(32) NOT NULL,
    action VARCHAR(64) NOT NULL,
    target_object_type VARCHAR(64),
    target_object_id VARCHAR(64),
    detail TEXT,                                       -- 操作详情JSON
    ip_address VARCHAR(64),
    user_agent VARCHAR(512),
    trace_id VARCHAR(64),
    operated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_tenant_time ON sys.sys_operation_log (tenant_id, operated_at DESC);
COMMENT ON TABLE sys.sys_operation_log IS '操作日志表-记录用户在系统中的操作行为';

-- 数据脱敏规则表
CREATE TABLE IF NOT EXISTS sys.sys_data_masking_rule (
    rule_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    rule_code VARCHAR(64) NOT NULL,                    -- 规则编码(如: phone_mask)
    rule_name VARCHAR(128) NOT NULL,
    field_type VARCHAR(32) NOT NULL,                   -- 字段类型(PHONE/EMAIL/ID_CARD/BANK_CARD)
    mask_pattern VARCHAR(64) NOT NULL,                 -- 脱敏模式(REPLACE/TRUNCATE/HASH)
    replace_char VARCHAR(8),
    keep_prefix INTEGER,
    keep_suffix INTEGER,
    enabled BOOLEAN NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, rule_code)
);
COMMENT ON TABLE sys.sys_data_masking_rule IS '数据脱敏规则表-定义敏感数据的脱敏策略';

-- 审批流配置表
CREATE TABLE IF NOT EXISTS sys.sys_audit_flow_config (
    flow_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    flow_code VARCHAR(64) NOT NULL,                    -- 流程编码
    flow_name VARCHAR(128) NOT NULL,
    business_type VARCHAR(64) NOT NULL,                -- 业务类型(PURCHASE_ORDER/PAYMENT_REQUEST等)
    required_approvals INTEGER,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, flow_code)
);
COMMENT ON TABLE sys.sys_audit_flow_config IS '审批流配置表-定义各业务类型的审批流程';

-- 审批流步骤表
CREATE TABLE IF NOT EXISTS sys.sys_audit_flow_step (
    step_id VARCHAR(64) PRIMARY KEY,
    flow_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    step_order INTEGER NOT NULL,                       -- 步骤顺序(从1开始)
    step_name VARCHAR(128) NOT NULL,
    approver_role VARCHAR(128),                        -- 审批人角色编码
    auto_approve BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_audit_flow_step_flow_order ON sys.sys_audit_flow_step (flow_id, step_order);
COMMENT ON TABLE sys.sys_audit_flow_step IS '审批流步骤表-定义审批流程中每个步骤的审批人规则';

-- 单据编号规则表
CREATE TABLE IF NOT EXISTS sys.sys_document_number_rule (
    rule_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    document_type VARCHAR(64) NOT NULL,                -- 单据类型(PO/SO/SHIPMENT等)
    prefix VARCHAR(64),                                -- 编号前缀
    date_format VARCHAR(64),                           -- 日期格式(如: yyyyMMdd)
    sequence_length INTEGER NOT NULL,                  -- 序号位数
    current_sequence BIGINT NOT NULL,                  -- 当前序号值
    step BIGINT NOT NULL,                              -- 序号步长
    reset_daily BOOLEAN NOT NULL,
    reset_monthly BOOLEAN NOT NULL,
    reset_yearly BOOLEAN NOT NULL,
    last_reset_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, document_type)
);
COMMENT ON TABLE sys.sys_document_number_rule IS '单据编号规则表-定义各业务单据的编号生成规则';

-- 单据编号段表: 防止编号冲突
CREATE TABLE IF NOT EXISTS sys.sys_document_number_segment (
    segment_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    document_type VARCHAR(64) NOT NULL,
    date_part VARCHAR(32) NOT NULL,
    sequence_part BIGINT NOT NULL,
    full_number VARCHAR(128) NOT NULL,                 -- 完整编号(如: PO-20260101-000001)
    generated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, full_number)
);
COMMENT ON TABLE sys.sys_document_number_segment IS '单据编号段表-记录已生成的编号段，防止编号冲突';

-- 内容审核规则表: 定义产品标题/描述/图片的合规审核规则
CREATE TABLE IF NOT EXISTS sys.sys_content_audit_rule (
    rule_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    rule_type VARCHAR(64) NOT NULL,                    -- 规则类型(SENSITIVE_WORD/TRADEMARK/IMAGE_COMPLIANCE/PROHIBITED)
    target_field VARCHAR(64) NOT NULL,                 -- 审核目标字段(TITLE/DESCRIPTION/BULLET_POINT/IMAGE)
    pattern TEXT,                                      -- 匹配模式(正则或关键词列表)
    severity VARCHAR(32) NOT NULL,                     -- 严重程度(BLOCK/WARNING/INFO)
    platform VARCHAR(64),
    enabled BOOLEAN NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_content_audit_rule_tenant_type ON sys.sys_content_audit_rule (tenant_id, rule_type, enabled);
COMMENT ON TABLE sys.sys_content_audit_rule IS '内容审核规则表-定义产品标题/描述/图片的合规审核规则';

-- 内容审核违规记录表
CREATE TABLE IF NOT EXISTS sys.sys_content_audit_violation (
    violation_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    rule_id VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,                  -- 目标类型(SPU/SKU/LISTING)
    target_id VARCHAR(64) NOT NULL,
    target_field VARCHAR(64) NOT NULL,
    matched_content TEXT,                              -- 匹配到的违规内容
    severity VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,                       -- 处理状态(PENDING/FIXED/IGNORED)
    fixed_by VARCHAR(64),
    fixed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_content_audit_violation_tenant_status ON sys.sys_content_audit_violation (tenant_id, status, created_at DESC);
COMMENT ON TABLE sys.sys_content_audit_violation IS '内容审核违规记录表-记录内容审核发现的违规项';

-- 连接器健康状态表
CREATE TABLE IF NOT EXISTS sys.sys_connector_health (
    health_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    connector_type VARCHAR(32) NOT NULL,
    platform VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,                       -- 健康状态(UP/DOWN/DEGRADED/UNKNOWN)
    response_time_ms INTEGER,
    error_message TEXT,
    checked_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_connector_health_tenant_platform ON sys.sys_connector_health (tenant_id, connector_type, platform, checked_at DESC);
COMMENT ON TABLE sys.sys_connector_health IS '连接器健康状态表-记录各连接器的健康检查结果';

-- 连接器调用审计日志表
CREATE TABLE IF NOT EXISTS sys.sys_connector_call_log (
    log_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    connector_type VARCHAR(32) NOT NULL,
    platform VARCHAR(64) NOT NULL,
    api_name VARCHAR(128) NOT NULL,                    -- API名称(如: getOrders)
    request_summary TEXT,                              -- 请求摘要(脱敏后)
    response_status INTEGER,
    response_time_ms INTEGER,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    trace_id VARCHAR(64),
    called_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_connector_call_log_tenant_time ON sys.sys_connector_call_log (tenant_id, platform, called_at DESC);
COMMENT ON TABLE sys.sys_connector_call_log IS '连接器调用审计日志表-记录所有外部API调用的详细信息';

-- 连接器密钥加密存储表
CREATE TABLE IF NOT EXISTS sys.sys_connector_secret (
    secret_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    config_id VARCHAR(64) NOT NULL,                    -- 关联连接器配置ID
    secret_type VARCHAR(32) NOT NULL,                  -- 密钥类型(API_KEY/OAUTH_TOKEN/ACCESS_KEY_SECRET)
    encrypted_value TEXT NOT NULL,                     -- 加密后的密钥值(AES-256)
    iv VARCHAR(64),                                    -- 加密初始向量
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_connector_secret_tenant_config ON sys.sys_connector_secret (tenant_id, config_id);
COMMENT ON TABLE sys.sys_connector_secret IS '连接器密钥加密存储表-安全存储各连接器的认证密钥';

-- 业务规则版本表: 管理业务规则的多版本控制
CREATE TABLE IF NOT EXISTS sys.sys_business_rule_version (
    version_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    rule_code VARCHAR(128) NOT NULL,                   -- 规则编码
    rule_name VARCHAR(128) NOT NULL,
    version_number INTEGER NOT NULL,                   -- 版本号(递增)
    domain VARCHAR(64) NOT NULL,                       -- 所属业务域
    rule_type VARCHAR(64) NOT NULL,
    content_json TEXT NOT NULL,                        -- 规则内容JSON
    status VARCHAR(32) NOT NULL,                       -- 版本状态(DRAFT/ACTIVE/DEPRECATED/ARCHIVED)
    published_by VARCHAR(64),
    published_at TIMESTAMPTZ,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_business_rule_version_tenant_code ON sys.sys_business_rule_version (tenant_id, rule_code, version_number DESC);
COMMENT ON TABLE sys.sys_business_rule_version IS '业务规则版本表-管理业务规则的多版本控制';

-- 模拟回放表: 记录业务规则的模拟执行结果
CREATE TABLE IF NOT EXISTS sys.sys_simulation_replay (
    simulation_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    rule_code VARCHAR(128) NOT NULL,
    version_id VARCHAR(64) NOT NULL,
    input_json TEXT NOT NULL,                          -- 模拟输入JSON
    output_json TEXT,                                  -- 模拟输出JSON
    execution_time_ms INTEGER,
    status VARCHAR(32) NOT NULL,                       -- 模拟状态(SUCCESS/FAILED/TIMEOUT)
    error_message TEXT,
    simulated_by VARCHAR(64),
    simulated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_simulation_replay_tenant_rule ON sys.sys_simulation_replay (tenant_id, rule_code, simulated_at DESC);
COMMENT ON TABLE sys.sys_simulation_replay IS '模拟回放表-记录业务规则的模拟执行结果';

-- 规则执行日志表
CREATE TABLE IF NOT EXISTS sys.sys_rule_execution_log (
    log_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    rule_code VARCHAR(128) NOT NULL,
    version_id VARCHAR(64),
    domain VARCHAR(64) NOT NULL,
    trigger_type VARCHAR(32) NOT NULL,                 -- 触发类型(AUTO/MANUAL/SCHEDULED)
    input_json TEXT,
    output_json TEXT,
    execution_time_ms INTEGER,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    trace_id VARCHAR(64),
    executed_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_rule_execution_log_tenant_rule ON sys.sys_rule_execution_log (tenant_id, rule_code, executed_at DESC);
COMMENT ON TABLE sys.sys_rule_execution_log IS '规则执行日志表-记录业务规则的实际执行情况';

-- Webhook端点表
CREATE TABLE IF NOT EXISTS sys.sys_webhook_endpoint (
    endpoint_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    endpoint_name VARCHAR(128) NOT NULL,
    url TEXT NOT NULL,                                 -- 回调URL
    secret_key VARCHAR(255),                           -- HMAC签名密钥
    events TEXT NOT NULL,                              -- 订阅事件类型列表JSON
    enabled BOOLEAN NOT NULL,
    retry_policy TEXT,                                 -- 重试策略JSON
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_webhook_endpoint_tenant ON sys.sys_webhook_endpoint (tenant_id, enabled);
COMMENT ON TABLE sys.sys_webhook_endpoint IS 'Webhook端点表-管理外部系统的Webhook回调端点';

-- Webhook投递记录表
CREATE TABLE IF NOT EXISTS sys.sys_webhook_delivery (
    delivery_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    endpoint_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    http_status INTEGER,
    response_body TEXT,
    attempt_count INTEGER NOT NULL,
    next_retry_at TIMESTAMPTZ,
    status VARCHAR(32) NOT NULL,                       -- 投递状态(PENDING/SUCCESS/FAILED/ABANDONED)
    delivered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_webhook_delivery_tenant_status ON sys.sys_webhook_delivery (tenant_id, status, created_at ASC);
COMMENT ON TABLE sys.sys_webhook_delivery IS 'Webhook投递记录表-记录每次Webhook的投递情况';

-- 手工导入任务表
CREATE TABLE IF NOT EXISTS sys.sys_manual_import_task (
    task_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    task_name VARCHAR(128) NOT NULL,
    import_type VARCHAR(64) NOT NULL,                  -- 导入类型(PRODUCT/ORDER/CUSTOMER/COST)
    template_code VARCHAR(64),
    file_url TEXT NOT NULL,
    total_rows INTEGER,
    success_rows INTEGER,
    failed_rows INTEGER,
    error_report_url TEXT,
    status VARCHAR(32) NOT NULL,                       -- 任务状态(PENDING/PROCESSING/COMPLETED/FAILED)
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_by VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_manual_import_task_tenant_status ON sys.sys_manual_import_task (tenant_id, status, created_at DESC);
COMMENT ON TABLE sys.sys_manual_import_task IS '手工导入任务表-管理通过模板上传的数据导入任务';

-- PMS草稿单据表: 存储PMS(AI)生成的待审批业务单据
CREATE TABLE IF NOT EXISTS sys.sys_pms_draft_document (
    draft_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    erp_reference_id VARCHAR(64) NOT NULL,             -- ERP侧引用ID(关联PMS建议)
    domain VARCHAR(64) NOT NULL,
    draft_type VARCHAR(64) NOT NULL,                   -- 草稿类型(CREATE/UPDATE)
    target_business_type VARCHAR(64) NOT NULL,
    target_business_id VARCHAR(64),
    content_json TEXT NOT NULL,
    trust_level VARCHAR(8) NOT NULL,                   -- 数据可信等级(A/B/C/D，A最高)
    source_system VARCHAR(64) NOT NULL,                -- 来源系统(PMS/ERP/MANUAL)
    actor_id VARCHAR(64),
    actor_type VARCHAR(32),                            -- 操作者类型(HUMAN/AI/SYSTEM)
    agent_id VARCHAR(64),
    scope VARCHAR(64),                                 -- 操作范围(SINGLE/BATCH/GLOBAL)
    purpose VARCHAR(128),
    trace_id VARCHAR(64),
    approval_status VARCHAR(32) NOT NULL,              -- 审批状态(PENDING/APPROVED/REJECTED/CANCELLED)
    approved_by VARCHAR(64),
    approved_at TIMESTAMPTZ,
    execution_status VARCHAR(32),                      -- 执行状态(PENDING/EXECUTING/SUCCESS/FAILED)
    execution_result TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_pms_draft_document_tenant_domain ON sys.sys_pms_draft_document (tenant_id, domain, approval_status);
COMMENT ON TABLE sys.sys_pms_draft_document IS 'PMS草稿单据表-存储PMS(AI)生成的待审批业务单据';

-- PMS反馈表: 记录PMS建议执行后的结果反馈
CREATE TABLE IF NOT EXISTS sys.sys_pms_feedback (
    feedback_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    erp_reference_id VARCHAR(64) NOT NULL,
    recommendation_id VARCHAR(64),
    domain VARCHAR(64) NOT NULL,
    feedback_type VARCHAR(64) NOT NULL,                -- 反馈类型(EXECUTION_RESULT/PERFORMANCE_METRIC/ERROR)
    execution_status VARCHAR(32) NOT NULL,
    business_result TEXT,
    business_metrics_json TEXT,
    failure_reason TEXT,
    operator_id VARCHAR(64),
    trace_id VARCHAR(64),
    delivered BOOLEAN NOT NULL,                        -- 是否已投递给PMS
    retry_count INTEGER NOT NULL,
    delivered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_pms_feedback_tenant_ref ON sys.sys_pms_feedback (tenant_id, erp_reference_id);
COMMENT ON TABLE sys.sys_pms_feedback IS 'PMS反馈表-记录PMS建议执行后的结果反馈';

-- PMS数据可信规则表: 定义PMS写入ERP的数据可信等级规则
CREATE TABLE IF NOT EXISTS sys.sys_pms_data_trust_rule (
    rule_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    domain VARCHAR(64) NOT NULL,
    object_type VARCHAR(64) NOT NULL,                  -- 对象类型(SPU/SKU/ORDER/PRICE等)
    trust_level VARCHAR(8) NOT NULL,                   -- 可信等级(A/B/C/D，A为最高)
    description TEXT,
    allowed_actions TEXT NOT NULL,                     -- 允许操作列表JSON
    can_overwrite_erp BOOLEAN NOT NULL,                -- 是否允许覆盖ERP数据
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, domain, object_type)
);
COMMENT ON TABLE sys.sys_pms_data_trust_rule IS 'PMS数据可信规则表-定义PMS写入ERP的数据可信等级规则';

-- 领域事件目录表
CREATE TABLE IF NOT EXISTS sys.sys_domain_event_catalog (
    event_id VARCHAR(64) PRIMARY KEY,
    event_type VARCHAR(128) NOT NULL,                  -- 事件类型(如: ORDER_CREATED)
    domain VARCHAR(64) NOT NULL,
    aggregate_type VARCHAR(64) NOT NULL,
    description TEXT,
    payload_schema TEXT,                               -- 事件载荷JSON Schema
    version INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (event_type, version)
);
COMMENT ON TABLE sys.sys_domain_event_catalog IS '领域事件目录表-登记系统中所有领域事件类型';

-- 业务告警表
CREATE TABLE IF NOT EXISTS sys.sys_business_alert (
    alert_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    alert_type VARCHAR(64) NOT NULL,                   -- 告警类型(PROFIT_DEVIATION/INVENTORY_LOW/ORDER_RISK)
    severity VARCHAR(32) NOT NULL,                     -- 严重程度(CRITICAL/WARNING/INFO)
    domain VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_object_type VARCHAR(64),
    related_object_id VARCHAR(64),
    status VARCHAR(32) NOT NULL,                       -- 告警状态(ACTIVE/ACKNOWLEDGED/RESOLVED)
    acknowledged_by VARCHAR(64),
    acknowledged_at TIMESTAMPTZ,
    resolved_by VARCHAR(64),
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_business_alert_tenant_status ON sys.sys_business_alert (tenant_id, severity, status, created_at DESC);
COMMENT ON TABLE sys.sys_business_alert IS '业务告警表-管理系统运行时的业务告警';

-- 打印模板表
CREATE TABLE IF NOT EXISTS sys.sys_print_template (
    template_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    template_code VARCHAR(64) NOT NULL,
    template_name VARCHAR(128) NOT NULL,
    template_type VARCHAR(64) NOT NULL,               -- 模板类型(ORDER/LABEL/INVOICE/PICK_LIST)
    content TEXT NOT NULL,
    paper_size VARCHAR(32),
    orientation VARCHAR(32),                          -- 打印方向(PORTRAIT/LANDSCAPE)
    variables TEXT,                                   -- 模板变量定义JSON
    enabled BOOLEAN NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, template_code)
);
CREATE INDEX IF NOT EXISTS idx_sys_print_template_tenant_type ON sys.sys_print_template (tenant_id, template_type, created_at DESC);
COMMENT ON TABLE sys.sys_print_template IS '打印模板表-管理订单、面单、发票等打印模板配置';

-- OMS原始订单快照表
CREATE TABLE IF NOT EXISTS oms_original_order (
    snapshot_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    platform VARCHAR(64) NOT NULL,
    marketplace VARCHAR(64),
    store_id VARCHAR(64),
    platform_order_no VARCHAR(128) NOT NULL,
    buyer_name VARCHAR(128),
    country_code VARCHAR(32),
    shipping_address TEXT,
    currency VARCHAR(16),
    import_mode VARCHAR(32) NOT NULL,
    raw_payload TEXT NOT NULL,
    processing_status VARCHAR(32) NOT NULL,
    standard_order_id VARCHAR(64),
    duplicate_order_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_oms_original_order_tenant_platform_no ON oms_original_order (tenant_id, platform, platform_order_no, created_at DESC);
COMMENT ON TABLE oms_original_order IS 'OMS原始订单快照表-保留导入前的平台原始订单载荷与处理结果';

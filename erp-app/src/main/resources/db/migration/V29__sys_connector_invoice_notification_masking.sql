-- V29: SYS域增强 - 连接器配置、发票设置、通知设置/模板、操作日志、数据脱敏规则

-- 连接器配置表
CREATE TABLE IF NOT EXISTS sys_connector_config (
    config_id           VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id           VARCHAR(64)  NOT NULL,
    connector_type      VARCHAR(32)  NOT NULL,
    platform            VARCHAR(64)  NOT NULL,
    connector_name      VARCHAR(128) NOT NULL,
    config              JSONB,
    status              VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    version             VARCHAR(32),
    description         TEXT,
    last_sync_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sys_connector_tenant ON sys_connector_config (tenant_id);
CREATE INDEX IF NOT EXISTS idx_sys_connector_type   ON sys_connector_config (tenant_id, connector_type);
CREATE INDEX IF NOT EXISTS idx_sys_connector_status ON sys_connector_config (tenant_id, status);

-- 发票设置表
CREATE TABLE IF NOT EXISTS sys_invoice_setting (
    setting_id          VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id           VARCHAR(64)  NOT NULL,
    setting_type        VARCHAR(32)  NOT NULL,
    setting_name        VARCHAR(128) NOT NULL,
    config              JSONB,
    enabled             BOOLEAN      NOT NULL DEFAULT TRUE,
    description         TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sys_invoice_tenant ON sys_invoice_setting (tenant_id);
CREATE INDEX IF NOT EXISTS idx_sys_invoice_type   ON sys_invoice_setting (tenant_id, setting_type);

-- 通知设置表
CREATE TABLE IF NOT EXISTS sys_notification_setting (
    setting_id          VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id           VARCHAR(64)  NOT NULL,
    channel             VARCHAR(32)  NOT NULL,
    channel_name        VARCHAR(128),
    rules               JSONB,
    enabled             BOOLEAN      NOT NULL DEFAULT TRUE,
    description         TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sys_notif_setting_tenant ON sys_notification_setting (tenant_id);
CREATE INDEX IF NOT EXISTS idx_sys_notif_setting_chan   ON sys_notification_setting (tenant_id, channel);

-- 通知模板表
CREATE TABLE IF NOT EXISTS sys_notification_template (
    template_id         VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id           VARCHAR(64)  NOT NULL,
    template_code       VARCHAR(64)  NOT NULL,
    template_name       VARCHAR(128) NOT NULL,
    channel             VARCHAR(32)  NOT NULL,
    subject             VARCHAR(256),
    content             TEXT         NOT NULL,
    variables           JSONB,
    enabled             BOOLEAN      NOT NULL DEFAULT TRUE,
    description         TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sys_notif_tmpl_tenant ON sys_notification_template (tenant_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_notif_tmpl_code ON sys_notification_template (tenant_id, template_code);
CREATE INDEX IF NOT EXISTS idx_sys_notif_tmpl_chan ON sys_notification_template (tenant_id, channel);

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    log_id              VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id           VARCHAR(64)  NOT NULL,
    user_id             VARCHAR(64)  NOT NULL,
    username            VARCHAR(128),
    module              VARCHAR(32)  NOT NULL,
    action              VARCHAR(64)  NOT NULL,
    target_object_type  VARCHAR(64),
    target_object_id    VARCHAR(64),
    detail              TEXT,
    ip_address          VARCHAR(64),
    user_agent          VARCHAR(512),
    trace_id            VARCHAR(64),
    operated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sys_oplog_tenant  ON sys_operation_log (tenant_id);
CREATE INDEX IF NOT EXISTS idx_sys_oplog_module  ON sys_operation_log (tenant_id, module);
CREATE INDEX IF NOT EXISTS idx_sys_oplog_user    ON sys_operation_log (tenant_id, user_id);
CREATE INDEX IF NOT EXISTS idx_sys_oplog_trace   ON sys_operation_log (tenant_id, trace_id);
CREATE INDEX IF NOT EXISTS idx_sys_oplog_time    ON sys_operation_log (operated_at DESC);

-- 数据脱敏规则表
CREATE TABLE IF NOT EXISTS sys_data_masking_rule (
    rule_id             VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id           VARCHAR(64)  NOT NULL,
    rule_code           VARCHAR(64)  NOT NULL,
    rule_name           VARCHAR(128) NOT NULL,
    field_type          VARCHAR(32)  NOT NULL,
    mask_pattern        VARCHAR(64)  NOT NULL,
    replace_char        VARCHAR(8)   NOT NULL DEFAULT '*',
    keep_prefix         INT          NOT NULL DEFAULT 0,
    keep_suffix         INT          NOT NULL DEFAULT 0,
    enabled             BOOLEAN      NOT NULL DEFAULT TRUE,
    description         TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sys_masking_tenant ON sys_data_masking_rule (tenant_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_masking_code ON sys_data_masking_rule (tenant_id, rule_code);
CREATE INDEX IF NOT EXISTS idx_sys_masking_type ON sys_data_masking_rule (tenant_id, field_type);

-- V30: Dashboard域增强 - 组件定义、日历事件、AI洞察卡片、快捷入口

-- 组件定义表
CREATE TABLE IF NOT EXISTS dashboard_widget (
    widget_id               VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id               VARCHAR(64)  NOT NULL,
    type                    VARCHAR(32)  NOT NULL,
    name                    VARCHAR(128) NOT NULL,
    data_source             VARCHAR(256),
    refresh_rate_seconds    INT          NOT NULL DEFAULT 300,
    config                  JSONB,
    status                  VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    category                VARCHAR(64),
    description             TEXT,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_dashboard_widget_tenant ON dashboard_widget (tenant_id);
CREATE INDEX IF NOT EXISTS idx_dashboard_widget_category ON dashboard_widget (tenant_id, category);
CREATE INDEX IF NOT EXISTS idx_dashboard_widget_status ON dashboard_widget (tenant_id, status);

-- 日历事件表
CREATE TABLE IF NOT EXISTS dashboard_calendar_event (
    event_id        VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    user_id         VARCHAR(64)  NOT NULL,
    title           VARCHAR(256) NOT NULL,
    description     TEXT,
    event_type      VARCHAR(32)  NOT NULL,
    start_time      TIMESTAMPTZ  NOT NULL,
    end_time        TIMESTAMPTZ,
    business_type   VARCHAR(32),
    business_id     VARCHAR(64),
    color           VARCHAR(16),
    all_day         BOOLEAN      NOT NULL DEFAULT FALSE,
    reminder        VARCHAR(64),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_dashboard_cal_tenant ON dashboard_calendar_event (tenant_id);
CREATE INDEX IF NOT EXISTS idx_dashboard_cal_user ON dashboard_calendar_event (tenant_id, user_id);
CREATE INDEX IF NOT EXISTS idx_dashboard_cal_time ON dashboard_calendar_event (tenant_id, user_id, start_time);

-- AI洞察卡片表
CREATE TABLE IF NOT EXISTS dashboard_ai_insight_card (
    card_id         VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    user_id         VARCHAR(64)  NOT NULL,
    title           VARCHAR(256) NOT NULL,
    summary         TEXT,
    insight_type    VARCHAR(32)  NOT NULL,
    severity        VARCHAR(16)  NOT NULL DEFAULT 'INFO',
    data            JSONB,
    source_domain   VARCHAR(32),
    suggestion      TEXT,
    action_url      VARCHAR(512),
    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
    is_dismissed    BOOLEAN      NOT NULL DEFAULT FALSE,
    valid_until     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_dashboard_insight_tenant ON dashboard_ai_insight_card (tenant_id);
CREATE INDEX IF NOT EXISTS idx_dashboard_insight_user ON dashboard_ai_insight_card (tenant_id, user_id);
CREATE INDEX IF NOT EXISTS idx_dashboard_insight_unread ON dashboard_ai_insight_card (tenant_id, user_id, is_read, is_dismissed);

-- 快捷入口表
CREATE TABLE IF NOT EXISTS dashboard_quick_entry (
    entry_id        VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    user_id         VARCHAR(64)  NOT NULL,
    entry_code      VARCHAR(64)  NOT NULL,
    entry_name      VARCHAR(128) NOT NULL,
    icon            VARCHAR(128),
    url             VARCHAR(512) NOT NULL,
    category        VARCHAR(64),
    sort_order      INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_dashboard_entry_tenant ON dashboard_quick_entry (tenant_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_dashboard_entry_code ON dashboard_quick_entry (tenant_id, user_id, entry_code);

CREATE TABLE IF NOT EXISTS sys_compliance_rule (
    rule_id          VARCHAR(64)  NOT NULL,
    tenant_id        VARCHAR(64)  NOT NULL,
    platform         VARCHAR(32)  NOT NULL,
    rule_type        VARCHAR(64)  NOT NULL,
    rule_name        VARCHAR(256) NOT NULL,
    description      TEXT,
    severity         VARCHAR(16)  NOT NULL DEFAULT 'MEDIUM',
    enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, rule_id)
);

CREATE TABLE IF NOT EXISTS sys_compliance_alert (
    alert_id         VARCHAR(64)  NOT NULL,
    tenant_id        VARCHAR(64)  NOT NULL,
    platform         VARCHAR(32)  NOT NULL,
    rule_id          VARCHAR(64),
    alert_type       VARCHAR(64)  NOT NULL,
    title            VARCHAR(512) NOT NULL,
    description      TEXT,
    severity         VARCHAR(16)  NOT NULL DEFAULT 'MEDIUM',
    status           VARCHAR(32)  NOT NULL DEFAULT 'OPEN',
    reference_type   VARCHAR(64),
    reference_id     VARCHAR(64),
    detected_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at      TIMESTAMP,
    PRIMARY KEY (tenant_id, alert_id)
);

CREATE INDEX idx_compliance_alert_status ON sys_compliance_alert(tenant_id, platform, status);

CREATE TABLE IF NOT EXISTS sys_platform_policy_change (
    change_id        VARCHAR(64)  NOT NULL,
    platform         VARCHAR(32)  NOT NULL,
    policy_area      VARCHAR(64)  NOT NULL,
    change_title     VARCHAR(512) NOT NULL,
    change_summary   TEXT,
    impact_level     VARCHAR(16)  NOT NULL DEFAULT 'MEDIUM',
    source_url       VARCHAR(1024),
    effective_date   TIMESTAMP,
    detected_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (change_id)
);

CREATE INDEX idx_platform_policy_change ON sys_platform_policy_change(platform, policy_area);

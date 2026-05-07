CREATE TABLE IF NOT EXISTS oms_pms_risk_alert_review_log (
    log_id               VARCHAR(64)   NOT NULL,
    tenant_id            VARCHAR(64)   NOT NULL,
    alert_id             VARCHAR(64)   NOT NULL,
    order_id             VARCHAR(64),
    action               VARCHAR(32)   NOT NULL,
    reviewer_note        VARCHAR(512),
    risk_level           VARCHAR(32),
    created_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id)
);

CREATE INDEX IF NOT EXISTS idx_oms_prarl_tenant_alert ON oms_pms_risk_alert_review_log(tenant_id, alert_id);
CREATE INDEX IF NOT EXISTS idx_oms_prarl_tenant_order ON oms_pms_risk_alert_review_log(tenant_id, order_id);

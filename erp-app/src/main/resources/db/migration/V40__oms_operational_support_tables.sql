CREATE TABLE IF NOT EXISTS oms_original_order (
    snapshot_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    platform VARCHAR(64),
    marketplace VARCHAR(64),
    store_id VARCHAR(64),
    platform_order_no VARCHAR(128),
    buyer_name VARCHAR(128),
    country_code VARCHAR(8),
    shipping_address TEXT,
    currency VARCHAR(8),
    import_mode VARCHAR(32),
    raw_payload TEXT,
    processing_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    standard_order_id VARCHAR(64),
    duplicate_order_id VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (snapshot_id)
);

CREATE INDEX IF NOT EXISTS idx_oms_oo_tenant_platform
    ON oms_original_order(tenant_id, platform, processing_status);

CREATE INDEX IF NOT EXISTS idx_oms_oo_platform_order
    ON oms_original_order(tenant_id, platform_order_no);

CREATE TABLE IF NOT EXISTS oms_order_strategy (
    strategy_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    strategy_type VARCHAR(64) NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(512),
    rules TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    priority INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (strategy_id)
);

CREATE INDEX IF NOT EXISTS idx_oms_os_tenant_type
    ON oms_order_strategy(tenant_id, strategy_type, enabled);

CREATE TABLE IF NOT EXISTS oms_order_sync_log (
    sync_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    platform VARCHAR(64) NOT NULL,
    sync_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    synced_count INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    PRIMARY KEY (sync_id)
);

CREATE INDEX IF NOT EXISTS idx_oms_osl_tenant_platform
    ON oms_order_sync_log(tenant_id, platform, status);

CREATE TABLE IF NOT EXISTS oms_pms_risk_alert (
    alert_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    order_id VARCHAR(64) NOT NULL,
    risk_type VARCHAR(64) NOT NULL,
    risk_score DECIMAL(8, 4),
    risk_level VARCHAR(32),
    description VARCHAR(512),
    suggested_action VARCHAR(512),
    trace_id VARCHAR(128),
    idempotency_key VARCHAR(128),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (alert_id)
);

CREATE INDEX IF NOT EXISTS idx_oms_pra_tenant_order
    ON oms_pms_risk_alert(tenant_id, order_id);

CREATE INDEX IF NOT EXISTS idx_oms_pra_tenant_status
    ON oms_pms_risk_alert(tenant_id, status);

CREATE TABLE IF NOT EXISTS oms_promotion (
    promo_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    order_id VARCHAR(64) NOT NULL,
    promo_type VARCHAR(64),
    promo_code VARCHAR(128),
    discount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    description VARCHAR(256),
    applied_at TIMESTAMP,
    PRIMARY KEY (promo_id)
);

CREATE INDEX IF NOT EXISTS idx_oms_promo_tenant_order
    ON oms_promotion(tenant_id, order_id);

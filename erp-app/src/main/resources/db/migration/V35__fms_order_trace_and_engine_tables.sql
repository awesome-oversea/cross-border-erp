CREATE TABLE IF NOT EXISTS fms_cost_aggregation_rule (
    rule_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    cost_source VARCHAR(64),
    cost_category VARCHAR(64),
    allocation_method VARCHAR(32) NOT NULL,
    allocation_basis VARCHAR(64),
    target_dimension VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    priority INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS fms_profit_deviation_alert (
    alert_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    dimension_type VARCHAR(64) NOT NULL,
    dimension_id VARCHAR(64) NOT NULL,
    seller_sku VARCHAR(128),
    expected_margin NUMERIC(18,4) NOT NULL,
    actual_margin NUMERIC(18,4) NOT NULL,
    deviation NUMERIC(18,4) NOT NULL,
    deviation_threshold NUMERIC(18,4) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    detected_at TIMESTAMPTZ NOT NULL,
    resolved_at TIMESTAMPTZ
);

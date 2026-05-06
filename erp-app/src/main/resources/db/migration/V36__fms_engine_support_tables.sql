CREATE TABLE IF NOT EXISTS fms_cost_breakdown (
    breakdown_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    cost_event_id VARCHAR(64),
    cost_type VARCHAR(64),
    cost_category VARCHAR(64),
    amount NUMERIC(18,4),
    currency VARCHAR(16),
    exchange_rate NUMERIC(18,6),
    amount_in_base_currency NUMERIC(18,4),
    remark TEXT,
    created_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS fms_journal_entry (
    entry_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    account_code VARCHAR(64),
    account_name VARCHAR(128),
    type VARCHAR(32),
    amount NUMERIC(18,4),
    currency VARCHAR(16),
    reference_type VARCHAR(64),
    reference_id VARCHAR(64),
    remark TEXT,
    entry_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS fms_tax_rule (
    rule_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    country_code VARCHAR(16),
    tax_type VARCHAR(64),
    tax_rate NUMERIC(18,6),
    tax_category VARCHAR(64),
    enabled BOOLEAN,
    effective_from TIMESTAMPTZ,
    effective_to TIMESTAMPTZ,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS fms_cost_allocation_result (
    result_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    rule_id VARCHAR(64),
    cost_event_id VARCHAR(64),
    target_dimension VARCHAR(64),
    target_id VARCHAR(64),
    allocated_amount NUMERIC(18,4),
    currency VARCHAR(16),
    exchange_rate NUMERIC(18,6),
    amount_in_base_currency NUMERIC(18,4),
    dimensions_json TEXT,
    allocated_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS fms_profit_result (
    result_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    dimension_type VARCHAR(64),
    dimension_id VARCHAR(64),
    seller_sku VARCHAR(128),
    order_id VARCHAR(64),
    store_id VARCHAR(64),
    marketplace_id VARCHAR(64),
    revenue NUMERIC(18,4),
    product_cost NUMERIC(18,4),
    shipping_cost NUMERIC(18,4),
    fba_fee NUMERIC(18,4),
    commission NUMERIC(18,4),
    advertising_cost NUMERIC(18,4),
    return_cost NUMERIC(18,4),
    storage_fee NUMERIC(18,4),
    packaging_cost NUMERIC(18,4),
    custom_duty NUMERIC(18,4),
    other_cost NUMERIC(18,4),
    total_cost NUMERIC(18,4),
    gross_profit NUMERIC(18,4),
    gross_margin NUMERIC(18,4),
    currency VARCHAR(16),
    exchange_rate NUMERIC(18,6),
    amount_in_base_currency NUMERIC(18,4),
    cost_details_json TEXT,
    calculated_at TIMESTAMPTZ
);

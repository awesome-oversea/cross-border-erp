ALTER TABLE fms_cost_event
    ADD COLUMN IF NOT EXISTS marketplace_id VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_fms_cost_marketplace
    ON fms_cost_event(tenant_id, marketplace_id);

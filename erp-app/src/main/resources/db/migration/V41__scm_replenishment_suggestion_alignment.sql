ALTER TABLE scm_replenishment_suggestion
    ADD COLUMN IF NOT EXISTS suggested_quantity INT NOT NULL DEFAULT 0;

ALTER TABLE scm_replenishment_suggestion
    ADD COLUMN IF NOT EXISTS priority VARCHAR(16) NOT NULL DEFAULT 'MEDIUM';

ALTER TABLE scm_replenishment_suggestion
    ALTER COLUMN current_stock SET DEFAULT 0;

ALTER TABLE scm_replenishment_suggestion
    ALTER COLUMN lead_time_days SET DEFAULT 0;

ALTER TABLE scm_replenishment_suggestion
    ALTER COLUMN safety_stock SET DEFAULT 0;

ALTER TABLE scm_replenishment_suggestion
    ALTER COLUMN status SET DEFAULT 'PENDING';

UPDATE scm_replenishment_suggestion
SET suggested_quantity = COALESCE(suggested_qty, 0)
WHERE suggested_quantity = 0
  AND suggested_qty IS NOT NULL;

ALTER TABLE scm_replenishment_suggestion
    ALTER COLUMN avg_daily_sales TYPE INT
    USING COALESCE(ROUND(avg_daily_sales), 0)::INT;

CREATE INDEX IF NOT EXISTS idx_scm_suggestion_tenant
    ON scm_replenishment_suggestion(tenant_id);

CREATE INDEX IF NOT EXISTS idx_scm_suggestion_status
    ON scm_replenishment_suggestion(tenant_id, status);

ALTER TABLE scm_purchase_plan_line
    ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64);

UPDATE scm_purchase_plan_line AS line
SET tenant_id = plan.tenant_id
FROM scm_purchase_plan AS plan
WHERE line.plan_id = plan.plan_id
  AND line.tenant_id IS NULL;

ALTER TABLE scm_purchase_plan_line
    ALTER COLUMN tenant_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_scm_plan_line_tenant_plan
    ON scm_purchase_plan_line(tenant_id, plan_id);

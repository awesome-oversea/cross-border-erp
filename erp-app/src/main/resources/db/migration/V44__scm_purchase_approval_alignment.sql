ALTER TABLE scm_purchase_approval
    ADD COLUMN IF NOT EXISTS po_id VARCHAR(64);

ALTER TABLE scm_purchase_approval
    ADD COLUMN IF NOT EXISTS approval_level INT;

ALTER TABLE scm_purchase_approval
    ADD COLUMN IF NOT EXISTS approver_id VARCHAR(64);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'scm_purchase_approval'
          AND column_name = 'purchase_id'
    ) THEN
        EXECUTE 'UPDATE scm_purchase_approval SET po_id = purchase_id WHERE po_id IS NULL';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'scm_purchase_approval'
          AND column_name = 'approver'
    ) THEN
        EXECUTE 'UPDATE scm_purchase_approval SET approver_id = approver WHERE approver_id IS NULL';
    END IF;
END $$;

WITH ranked AS (
    SELECT approval_id,
           ROW_NUMBER() OVER (PARTITION BY tenant_id, po_id ORDER BY created_at ASC, approval_id ASC) AS level_no
    FROM scm_purchase_approval
)
UPDATE scm_purchase_approval AS target
SET approval_level = ranked.level_no
FROM ranked
WHERE target.approval_id = ranked.approval_id
  AND target.approval_level IS NULL;

ALTER TABLE scm_purchase_approval
    ALTER COLUMN po_id SET NOT NULL;

ALTER TABLE scm_purchase_approval
    ALTER COLUMN approval_level SET NOT NULL;

ALTER TABLE scm_purchase_approval
    ALTER COLUMN approver_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_scm_pa_tenant_purchase
    ON scm_purchase_approval(tenant_id, po_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_scm_pa_tenant_purchase_level
    ON scm_purchase_approval(tenant_id, po_id, approval_level);

CREATE INDEX IF NOT EXISTS idx_scm_pa_tenant_approver_status
    ON scm_purchase_approval(tenant_id, approver_id, status);

CREATE INDEX IF NOT EXISTS idx_fms_payment_request_po
    ON fms_payment_request(tenant_id, po_id);

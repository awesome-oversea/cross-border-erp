ALTER TABLE wms_inventory_balance ADD COLUMN in_transit INT NOT NULL DEFAULT 0;
ALTER TABLE wms_inventory_balance ADD COLUMN frozen INT NOT NULL DEFAULT 0;

ALTER TABLE wms_inventory_balance ALTER COLUMN on_hand SET DEFAULT 0;
ALTER TABLE wms_inventory_balance ALTER COLUMN reserved SET DEFAULT 0;

CREATE INDEX idx_wms_inv_balance_sku ON wms_inventory_balance(tenant_id, seller_sku);

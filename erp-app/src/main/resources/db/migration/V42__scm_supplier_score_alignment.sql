ALTER TABLE scm_supplier_score
    ALTER COLUMN dimension DROP NOT NULL;

ALTER TABLE scm_supplier_score
    ALTER COLUMN score DROP NOT NULL;

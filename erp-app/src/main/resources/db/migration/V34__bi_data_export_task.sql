CREATE TABLE IF NOT EXISTS bi_data_export_task (
    task_id         VARCHAR(64)   NOT NULL,
    tenant_id       VARCHAR(64)   NOT NULL,
    export_name     VARCHAR(256)  NOT NULL,
    export_type     VARCHAR(64)   NOT NULL,
    format          VARCHAR(16)   NOT NULL DEFAULT 'CSV',
    status          VARCHAR(32)   NOT NULL DEFAULT 'PENDING',
    file_url        VARCHAR(1024),
    record_count    BIGINT        NOT NULL DEFAULT 0,
    error           TEXT,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMP,
    PRIMARY KEY (tenant_id, task_id)
);

CREATE INDEX idx_export_task_status ON bi_data_export_task(tenant_id, status);

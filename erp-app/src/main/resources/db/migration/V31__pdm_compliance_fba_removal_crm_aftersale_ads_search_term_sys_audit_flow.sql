CREATE TABLE IF NOT EXISTS pdm_sensitive_word (
    word_id          VARCHAR(64)  NOT NULL,
    tenant_id        VARCHAR(64)  NOT NULL,
    word             VARCHAR(255) NOT NULL,
    category         VARCHAR(32)  NOT NULL,
    language         VARCHAR(10),
    enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (word_id)
);

CREATE INDEX idx_pdm_sw_tenant ON pdm_sensitive_word (tenant_id);
CREATE INDEX idx_pdm_sw_lang   ON pdm_sensitive_word (tenant_id, language);

CREATE TABLE IF NOT EXISTS pdm_upc_pool (
    pool_id          VARCHAR(64)  NOT NULL,
    tenant_id        VARCHAR(64)  NOT NULL,
    upc_code         VARCHAR(32)  NOT NULL,
    status           VARCHAR(16)  NOT NULL DEFAULT 'AVAILABLE',
    assigned_sku_id  VARCHAR(64),
    assigned_at      TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (pool_id)
);

CREATE UNIQUE INDEX idx_pdm_upc_code ON pdm_upc_pool (tenant_id, upc_code);
CREATE INDEX idx_pdm_upc_status ON pdm_upc_pool (tenant_id, status);

CREATE TABLE IF NOT EXISTS fba_removal_order (
    removal_id       VARCHAR(64)  NOT NULL,
    tenant_id        VARCHAR(64)  NOT NULL,
    fba_sku          VARCHAR(128) NOT NULL,
    quantity         INT          NOT NULL,
    removal_type     VARCHAR(16)  NOT NULL,
    status           VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    return_address_id VARCHAR(64),
    reason           VARCHAR(512),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (removal_id)
);

CREATE INDEX idx_fba_ro_tenant ON fba_removal_order (tenant_id);
CREATE INDEX idx_fba_ro_status ON fba_removal_order (tenant_id, status);

CREATE TABLE IF NOT EXISTS crm_return_refund (
    return_id        VARCHAR(64)  NOT NULL,
    tenant_id        VARCHAR(64)  NOT NULL,
    order_id         VARCHAR(64)  NOT NULL,
    seller_sku       VARCHAR(128),
    customer_id      VARCHAR(64),
    quantity         INT          NOT NULL,
    refund_amount    DECIMAL(14,2),
    currency         VARCHAR(3),
    reason           VARCHAR(32)  NOT NULL,
    status           VARCHAR(16)  NOT NULL DEFAULT 'REQUESTED',
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (return_id)
);

CREATE INDEX idx_crm_rr_tenant ON crm_return_refund (tenant_id);
CREATE INDEX idx_crm_rr_order  ON crm_return_refund (tenant_id, order_id);

CREATE TABLE IF NOT EXISTS crm_service_ticket (
    ticket_id        VARCHAR(64)  NOT NULL,
    tenant_id        VARCHAR(64)  NOT NULL,
    customer_id      VARCHAR(64)  NOT NULL,
    subject          VARCHAR(512) NOT NULL,
    description      TEXT,
    assignee         VARCHAR(64),
    status           VARCHAR(16)  NOT NULL DEFAULT 'OPEN',
    resolution       TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ticket_id)
);

CREATE INDEX idx_crm_st_tenant   ON crm_service_ticket (tenant_id);
CREATE INDEX idx_crm_st_assignee ON crm_service_ticket (tenant_id, assignee);

CREATE TABLE IF NOT EXISTS ads_search_term_analysis (
    analysis_id      VARCHAR(64)  NOT NULL,
    tenant_id        VARCHAR(64)  NOT NULL,
    campaign_id      VARCHAR(64)  NOT NULL,
    search_term      VARCHAR(512) NOT NULL,
    impressions      INT          NOT NULL DEFAULT 0,
    clicks           INT          NOT NULL DEFAULT 0,
    ctr              DOUBLE       NOT NULL DEFAULT 0,
    acos             DOUBLE       NOT NULL DEFAULT 0,
    orders           INT          NOT NULL DEFAULT 0,
    performance      VARCHAR(32)  NOT NULL,
    suggested_keywords TEXT,
    analyzed_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (analysis_id)
);

CREATE INDEX idx_ads_sta_tenant   ON ads_search_term_analysis (tenant_id);
CREATE INDEX idx_ads_sta_campaign ON ads_search_term_analysis (tenant_id, campaign_id);

CREATE TABLE IF NOT EXISTS sys_audit_flow_config (
    flow_id              VARCHAR(64)  NOT NULL,
    tenant_id            VARCHAR(64)  NOT NULL,
    flow_code            VARCHAR(64)  NOT NULL,
    flow_name            VARCHAR(128) NOT NULL,
    business_type        VARCHAR(64)  NOT NULL,
    required_approvals   INT          NOT NULL DEFAULT 1,
    enabled              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (flow_id)
);

CREATE UNIQUE INDEX idx_sys_afc_code ON sys_audit_flow_config (tenant_id, flow_code);
CREATE INDEX idx_sys_afc_biz   ON sys_audit_flow_config (tenant_id, business_type);

CREATE TABLE IF NOT EXISTS sys_audit_flow_step (
    step_id          VARCHAR(64)  NOT NULL,
    flow_id          VARCHAR(64)  NOT NULL,
    tenant_id        VARCHAR(64)  NOT NULL,
    step_order       INT          NOT NULL,
    step_name        VARCHAR(128) NOT NULL,
    approver_role    VARCHAR(64),
    auto_approve     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (step_id)
);

CREATE INDEX idx_sys_afs_flow ON sys_audit_flow_step (tenant_id, flow_id);

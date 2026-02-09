-- V2_0_0: Portfolio and Workstream tables
-- Documentation-only migration matching Hibernate-generated schema.
-- NOT auto-executed (Flyway not enabled); kept for future production use.

CREATE TABLE IF NOT EXISTS portfolios (
    id              UUID PRIMARY KEY,
    tenant_id       UUID         NOT NULL,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(300) NOT NULL,
    description     TEXT,
    status          VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    owner_id        UUID,
    tags            TEXT,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP,
    created_by_id   UUID,
    CONSTRAINT uq_portfolios_tenant_slug UNIQUE (tenant_id, slug)
);

CREATE INDEX IF NOT EXISTS idx_portfolios_tenant        ON portfolios (tenant_id);
CREATE INDEX IF NOT EXISTS idx_portfolios_tenant_status ON portfolios (tenant_id, status);

-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS workstreams (
    id              UUID PRIMARY KEY,
    tenant_id       UUID         NOT NULL,
    program_id      UUID         NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    mode            VARCHAR(255) NOT NULL,
    execution_mode  VARCHAR(255) NOT NULL,
    status          VARCHAR(255) NOT NULL DEFAULT 'NOT_STARTED',
    owner_id        UUID,
    sort_order      INTEGER      DEFAULT 0,
    tags            TEXT,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP,
    created_by_id   UUID,
    CONSTRAINT uq_workstreams_tenant_program_name UNIQUE (tenant_id, program_id, name),
    CONSTRAINT fk_workstreams_program FOREIGN KEY (program_id) REFERENCES projects (id)
);

CREATE INDEX IF NOT EXISTS idx_workstreams_tenant_program ON workstreams (tenant_id, program_id);
CREATE INDEX IF NOT EXISTS idx_workstreams_tenant_status  ON workstreams (tenant_id, status);

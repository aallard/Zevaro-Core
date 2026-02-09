-- V2_0_1: Specification, Requirement, and Ticket tables
-- Documentation-only migration matching Hibernate-generated schema.

CREATE TABLE IF NOT EXISTS specifications (
    id              UUID PRIMARY KEY,
    tenant_id       UUID         NOT NULL,
    workstream_id   UUID         NOT NULL,
    program_id      UUID         NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    document_id     UUID,
    status          VARCHAR(255) NOT NULL DEFAULT 'DRAFT',
    version         INTEGER      NOT NULL DEFAULT 1,
    author_id       UUID         NOT NULL,
    reviewer_id     UUID,
    approved_at     TIMESTAMP,
    approved_by_id  UUID,
    estimated_hours NUMERIC(19,2),
    actual_hours    NUMERIC(19,2),
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP,
    created_by_id   UUID,
    CONSTRAINT fk_specifications_workstream FOREIGN KEY (workstream_id) REFERENCES workstreams (id),
    CONSTRAINT fk_specifications_program    FOREIGN KEY (program_id)    REFERENCES projects (id)
);

CREATE INDEX IF NOT EXISTS idx_specifications_tenant_ws      ON specifications (tenant_id, workstream_id);
CREATE INDEX IF NOT EXISTS idx_specifications_tenant_status   ON specifications (tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_specifications_tenant_program  ON specifications (tenant_id, program_id);

-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS requirements (
    id                UUID PRIMARY KEY,
    tenant_id         UUID         NOT NULL,
    specification_id  UUID         NOT NULL,
    workstream_id     UUID         NOT NULL,
    program_id        UUID         NOT NULL,
    identifier        VARCHAR(20)  NOT NULL,
    title             VARCHAR(500) NOT NULL,
    description       TEXT,
    type              VARCHAR(255) NOT NULL,
    priority          VARCHAR(255) NOT NULL,
    status            VARCHAR(255) NOT NULL DEFAULT 'DRAFT',
    acceptance_criteria TEXT,
    estimated_hours   NUMERIC(19,2),
    actual_hours      NUMERIC(19,2),
    sort_order        INTEGER      DEFAULT 0,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP,
    created_by_id     UUID,
    CONSTRAINT fk_requirements_specification FOREIGN KEY (specification_id) REFERENCES specifications (id),
    CONSTRAINT fk_requirements_workstream    FOREIGN KEY (workstream_id)    REFERENCES workstreams (id),
    CONSTRAINT fk_requirements_program       FOREIGN KEY (program_id)       REFERENCES projects (id)
);

CREATE INDEX IF NOT EXISTS idx_requirements_tenant_spec    ON requirements (tenant_id, specification_id);
CREATE INDEX IF NOT EXISTS idx_requirements_tenant_status  ON requirements (tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_requirements_tenant_ws      ON requirements (tenant_id, workstream_id);
CREATE INDEX IF NOT EXISTS idx_requirements_tenant_program ON requirements (tenant_id, program_id);

-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS requirement_dependencies (
    id              UUID PRIMARY KEY,
    requirement_id  UUID         NOT NULL,
    depends_on_id   UUID         NOT NULL,
    type            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    CONSTRAINT uq_req_dep UNIQUE (requirement_id, depends_on_id),
    CONSTRAINT fk_req_dep_requirement FOREIGN KEY (requirement_id) REFERENCES requirements (id),
    CONSTRAINT fk_req_dep_depends_on  FOREIGN KEY (depends_on_id)  REFERENCES requirements (id)
);

CREATE INDEX IF NOT EXISTS idx_req_dep_requirement ON requirement_dependencies (requirement_id);
CREATE INDEX IF NOT EXISTS idx_req_dep_depends_on  ON requirement_dependencies (depends_on_id);

-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS tickets (
    id                  UUID PRIMARY KEY,
    tenant_id           UUID         NOT NULL,
    workstream_id       UUID         NOT NULL,
    program_id          UUID         NOT NULL,
    identifier          VARCHAR(20)  NOT NULL,
    title               VARCHAR(500) NOT NULL,
    description         TEXT,
    type                VARCHAR(255) NOT NULL,
    severity            VARCHAR(255),
    status              VARCHAR(255) NOT NULL DEFAULT 'NEW',
    resolution          VARCHAR(255),
    reported_by_id      UUID         NOT NULL,
    assigned_to_id      UUID,
    environment         VARCHAR(100),
    steps_to_reproduce  TEXT,
    expected_behavior   TEXT,
    actual_behavior     TEXT,
    source              VARCHAR(255) NOT NULL DEFAULT 'MANUAL',
    external_ref        VARCHAR(255),
    estimated_hours     NUMERIC(19,2),
    actual_hours        NUMERIC(19,2),
    resolved_at         TIMESTAMP,
    closed_at           TIMESTAMP,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP,
    created_by_id       UUID,
    CONSTRAINT fk_tickets_workstream FOREIGN KEY (workstream_id) REFERENCES workstreams (id),
    CONSTRAINT fk_tickets_program    FOREIGN KEY (program_id)    REFERENCES projects (id)
);

CREATE INDEX IF NOT EXISTS idx_tickets_tenant_ws       ON tickets (tenant_id, workstream_id);
CREATE INDEX IF NOT EXISTS idx_tickets_tenant_status   ON tickets (tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_tickets_tenant_severity ON tickets (tenant_id, severity);
CREATE INDEX IF NOT EXISTS idx_tickets_tenant_type     ON tickets (tenant_id, type);
CREATE INDEX IF NOT EXISTS idx_tickets_tenant_program  ON tickets (tenant_id, program_id);
CREATE INDEX IF NOT EXISTS idx_tickets_tenant_assigned ON tickets (tenant_id, assigned_to_id);

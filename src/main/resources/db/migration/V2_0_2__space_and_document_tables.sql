-- V2_0_2: Space and Document tables
-- Documentation-only migration matching Hibernate-generated schema.

CREATE TABLE IF NOT EXISTS spaces (
    id              UUID PRIMARY KEY,
    tenant_id       UUID         NOT NULL,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(300) NOT NULL,
    description     TEXT,
    type            VARCHAR(255) NOT NULL,
    status          VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    program_id      UUID,
    owner_id        UUID         NOT NULL,
    icon            VARCHAR(10),
    visibility      VARCHAR(255) NOT NULL DEFAULT 'PUBLIC',
    sort_order      INTEGER      DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP,
    created_by_id   UUID,
    CONSTRAINT uq_spaces_tenant_name UNIQUE (tenant_id, name)
);

CREATE INDEX IF NOT EXISTS idx_spaces_tenant         ON spaces (tenant_id);
CREATE INDEX IF NOT EXISTS idx_spaces_tenant_type    ON spaces (tenant_id, type);
CREATE INDEX IF NOT EXISTS idx_spaces_tenant_program ON spaces (tenant_id, program_id);

-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS documents (
    id                  UUID PRIMARY KEY,
    tenant_id           UUID         NOT NULL,
    space_id            UUID         NOT NULL,
    parent_document_id  UUID,
    title               VARCHAR(500) NOT NULL,
    body                TEXT,
    type                VARCHAR(255) NOT NULL,
    status              VARCHAR(255) NOT NULL DEFAULT 'DRAFT',
    version             INTEGER      NOT NULL DEFAULT 1,
    author_id           UUID         NOT NULL,
    last_edited_by_id   UUID,
    published_at        TIMESTAMP,
    tags                TEXT,
    sort_order          INTEGER      DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP,
    CONSTRAINT fk_documents_space          FOREIGN KEY (space_id)           REFERENCES spaces (id),
    CONSTRAINT fk_documents_parent_doc     FOREIGN KEY (parent_document_id) REFERENCES documents (id)
);

CREATE INDEX IF NOT EXISTS idx_documents_tenant_space      ON documents (tenant_id, space_id);
CREATE INDEX IF NOT EXISTS idx_documents_tenant_parent_doc ON documents (tenant_id, parent_document_id);
CREATE INDEX IF NOT EXISTS idx_documents_tenant_type       ON documents (tenant_id, type);

-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS document_versions (
    id            UUID PRIMARY KEY,
    document_id   UUID         NOT NULL,
    version       INTEGER      NOT NULL,
    title         VARCHAR(500) NOT NULL,
    body          TEXT,
    edited_by_id  UUID         NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    CONSTRAINT fk_doc_versions_document FOREIGN KEY (document_id) REFERENCES documents (id)
);

CREATE INDEX IF NOT EXISTS idx_doc_versions_document ON document_versions (document_id);
CREATE INDEX IF NOT EXISTS idx_doc_versions_doc_ver  ON document_versions (document_id, version);

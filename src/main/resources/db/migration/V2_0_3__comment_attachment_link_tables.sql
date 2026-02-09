-- V2_0_3: Comment, Attachment, EntityLink, and ProgramTemplate tables
-- Documentation-only migration matching Hibernate-generated schema.

CREATE TABLE IF NOT EXISTS comments (
    id                UUID PRIMARY KEY,
    tenant_id         UUID         NOT NULL,
    parent_type       VARCHAR(255) NOT NULL,
    parent_id         UUID         NOT NULL,
    author_id         UUID         NOT NULL,
    body              TEXT         NOT NULL,
    parent_comment_id UUID,
    edited            BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP,
    CONSTRAINT fk_comments_parent_comment FOREIGN KEY (parent_comment_id) REFERENCES comments (id)
);

CREATE INDEX IF NOT EXISTS idx_comments_tenant_parent  ON comments (tenant_id, parent_type, parent_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent_comment ON comments (parent_comment_id);

-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS attachments (
    id              UUID PRIMARY KEY,
    tenant_id       UUID           NOT NULL,
    parent_type     VARCHAR(255)   NOT NULL,
    parent_id       UUID           NOT NULL,
    file_name       VARCHAR(255)   NOT NULL,
    file_type       VARCHAR(100),
    file_size       BIGINT,
    storage_url     VARCHAR(1000)  NOT NULL,
    uploaded_by_id  UUID           NOT NULL,
    created_at      TIMESTAMP      NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_attachments_tenant_parent ON attachments (tenant_id, parent_type, parent_id);

-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS entity_links (
    id              UUID PRIMARY KEY,
    tenant_id       UUID         NOT NULL,
    source_type     VARCHAR(255) NOT NULL,
    source_id       UUID         NOT NULL,
    target_type     VARCHAR(255) NOT NULL,
    target_id       UUID         NOT NULL,
    link_type       VARCHAR(255) NOT NULL,
    created_by_id   UUID         NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    CONSTRAINT uq_entity_links UNIQUE (source_type, source_id, target_type, target_id, link_type)
);

CREATE INDEX IF NOT EXISTS idx_entity_links_source ON entity_links (tenant_id, source_type, source_id);
CREATE INDEX IF NOT EXISTS idx_entity_links_target ON entity_links (tenant_id, target_type, target_id);

-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS program_templates (
    id              UUID PRIMARY KEY,
    tenant_id       UUID,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    structure       TEXT         NOT NULL,
    is_system       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by_id   UUID,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_templates_tenant ON program_templates (tenant_id);
CREATE INDEX IF NOT EXISTS idx_templates_system ON program_templates (is_system);

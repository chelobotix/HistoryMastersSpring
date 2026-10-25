CREATE TABLE biography_translations (
    id              BIGSERIAL PRIMARY KEY,
    biography_id    BIGINT NOT NULL,
    locale          VARCHAR(35) NOT NULL,
    text            TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_biography_translations UNIQUE (biography_id, locale),
    CONSTRAINT fk_biography_translations_biography
        FOREIGN KEY (biography_id) REFERENCES biographies (id) ON DELETE CASCADE
);

CREATE INDEX idx_biography_translations_locale ON biography_translations (locale);

CREATE TRIGGER trg_biography_translations_updated_at
    BEFORE UPDATE ON biography_translations
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

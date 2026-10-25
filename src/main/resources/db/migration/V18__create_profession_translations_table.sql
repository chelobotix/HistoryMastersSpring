CREATE TABLE profession_translations (
    id              BIGSERIAL PRIMARY KEY,
    profession_id   BIGINT NOT NULL,
    locale          VARCHAR(35) NOT NULL,
    text            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_profession_translations UNIQUE (profession_id, locale),
    CONSTRAINT fk_profession_translations_profession
        FOREIGN KEY (profession_id) REFERENCES professions (id) ON DELETE CASCADE
);

CREATE INDEX idx_profession_translations_locale ON profession_translations (locale);

CREATE TRIGGER trg_profession_translations_updated_at
    BEFORE UPDATE ON profession_translations
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

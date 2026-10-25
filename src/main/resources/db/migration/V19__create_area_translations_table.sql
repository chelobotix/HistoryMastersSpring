CREATE TABLE area_translations (
    id          BIGSERIAL PRIMARY KEY,
    area_id     BIGINT NOT NULL,
    locale      VARCHAR(35) NOT NULL,
    text        VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_area_translations UNIQUE (area_id, locale),
    CONSTRAINT fk_area_translations_area
        FOREIGN KEY (area_id) REFERENCES areas (id) ON DELETE CASCADE
);

CREATE INDEX idx_area_translations_locale ON area_translations (locale);

CREATE TRIGGER trg_area_translations_updated_at
    BEFORE UPDATE ON area_translations
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

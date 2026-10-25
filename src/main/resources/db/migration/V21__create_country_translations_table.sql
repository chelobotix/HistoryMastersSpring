CREATE TABLE country_translations (
    id          BIGSERIAL PRIMARY KEY,
    country_id  BIGINT NOT NULL,
    locale      VARCHAR(35) NOT NULL,
    text        VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_country_translations UNIQUE (country_id, locale),
    CONSTRAINT fk_country_translations_country
        FOREIGN KEY (country_id) REFERENCES countries (id) ON DELETE CASCADE
);

CREATE INDEX idx_country_translations_locale ON country_translations (locale);

CREATE TRIGGER trg_country_translations_updated_at
    BEFORE UPDATE ON country_translations
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

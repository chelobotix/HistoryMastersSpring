CREATE TABLE historical_era_translations (
    id                  BIGSERIAL PRIMARY KEY,
    historical_era_id   BIGINT NOT NULL,
    locale              VARCHAR(35) NOT NULL,
    text                VARCHAR(255) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_historical_era_translations UNIQUE (historical_era_id, locale),
    CONSTRAINT fk_historical_era_translations_era
        FOREIGN KEY (historical_era_id) REFERENCES historical_eras (id) ON DELETE CASCADE
);

CREATE INDEX idx_historical_era_translations_locale ON historical_era_translations (locale);

CREATE TRIGGER trg_historical_era_translations_updated_at
    BEFORE UPDATE ON historical_era_translations
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

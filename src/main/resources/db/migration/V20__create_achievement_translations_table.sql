CREATE TABLE achievement_translations (
    id                  BIGSERIAL PRIMARY KEY,
    achievement_id      BIGINT NOT NULL,
    locale              VARCHAR(35) NOT NULL,
    text                VARCHAR(255) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_achievement_translations UNIQUE (achievement_id, locale),
    CONSTRAINT fk_achievement_translations_achievement
        FOREIGN KEY (achievement_id) REFERENCES achievements (id) ON DELETE CASCADE
);

CREATE INDEX idx_achievement_translations_locale ON achievement_translations (locale);

CREATE TRIGGER trg_achievement_translations_updated_at
    BEFORE UPDATE ON achievement_translations
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

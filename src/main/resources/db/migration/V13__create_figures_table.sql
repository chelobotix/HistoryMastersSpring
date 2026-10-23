CREATE TABLE figures (
    id                  BIGSERIAL PRIMARY KEY,
    first_name          VARCHAR(255) NOT NULL,
    last_name           VARCHAR(255) NOT NULL,
    year_of_birth       INTEGER NOT NULL,
    year_of_birth_bc    BOOLEAN NOT NULL DEFAULT FALSE,
    year_of_death       INTEGER,
    year_of_death_bc    BOOLEAN NOT NULL DEFAULT TRUE,
    country_id          BIGINT,
    historical_era_id   BIGINT,
    profession_id       BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_figures_country FOREIGN KEY (country_id) REFERENCES countries (id),
    CONSTRAINT fk_figures_historical_era FOREIGN KEY (historical_era_id) REFERENCES historical_eras (id),
    CONSTRAINT fk_figures_profession FOREIGN KEY (profession_id) REFERENCES professions (id)
);

CREATE INDEX idx_figures_country_id ON figures (country_id);
CREATE INDEX idx_figures_historical_era_id ON figures (historical_era_id);
CREATE INDEX idx_figures_profession_id ON figures (profession_id);

CREATE TRIGGER trg_figures_updated_at
    BEFORE UPDATE ON figures
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

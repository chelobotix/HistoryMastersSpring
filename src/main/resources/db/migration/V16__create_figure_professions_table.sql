CREATE TABLE figure_professions (
    figure_id       BIGINT NOT NULL,
    profession_id   BIGINT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_figure_professions PRIMARY KEY (figure_id, profession_id),
    CONSTRAINT fk_figure_professions_figure FOREIGN KEY (figure_id) REFERENCES figures (id) ON DELETE CASCADE,
    CONSTRAINT fk_figure_professions_profession FOREIGN KEY (profession_id) REFERENCES professions (id) ON DELETE CASCADE
);

CREATE INDEX idx_figure_professions_profession_id ON figure_professions (profession_id);

CREATE TRIGGER trg_figure_professions_updated_at
    BEFORE UPDATE ON figure_professions
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

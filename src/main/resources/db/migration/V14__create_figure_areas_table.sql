CREATE TABLE figure_areas (
    figure_id   BIGINT NOT NULL,
    area_id     BIGINT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_figure_areas PRIMARY KEY (figure_id, area_id),
    CONSTRAINT fk_figure_areas_figure FOREIGN KEY (figure_id) REFERENCES figures (id) ON DELETE CASCADE,
    CONSTRAINT fk_figure_areas_area FOREIGN KEY (area_id) REFERENCES areas (id) ON DELETE CASCADE
);

CREATE INDEX idx_figure_areas_area_id ON figure_areas (area_id);

CREATE TRIGGER trg_figure_areas_updated_at
    BEFORE UPDATE ON figure_areas
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

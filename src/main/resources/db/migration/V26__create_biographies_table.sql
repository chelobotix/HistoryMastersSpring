CREATE TABLE biographies (
    id          BIGSERIAL PRIMARY KEY,
    figure_id   BIGINT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_biographies_figure UNIQUE (figure_id),
    CONSTRAINT fk_biographies_figure
        FOREIGN KEY (figure_id) REFERENCES figures (id) ON DELETE CASCADE
);

CREATE TRIGGER trg_biographies_updated_at
    BEFORE UPDATE ON biographies
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

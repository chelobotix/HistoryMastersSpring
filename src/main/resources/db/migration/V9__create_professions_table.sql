CREATE TABLE professions (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_professions_code UNIQUE (code)
);

CREATE TRIGGER trg_professions_updated_at
    BEFORE UPDATE ON professions
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

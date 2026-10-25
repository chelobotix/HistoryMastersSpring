CREATE TABLE historical_eras (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(255) NOT NULL,
    sort_order  INTEGER NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_historical_eras_code UNIQUE (code)
);

CREATE TRIGGER trg_historical_eras_updated_at
    BEFORE UPDATE ON historical_eras
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

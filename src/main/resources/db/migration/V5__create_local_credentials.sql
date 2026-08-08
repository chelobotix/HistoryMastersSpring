CREATE TABLE local_credentials (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    password_digest     VARCHAR(255) NOT NULL,
    password_changed_at TIMESTAMPTZ,
    picture             VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_local_credentials_user_id UNIQUE (user_id)
);

CREATE TRIGGER trg_local_credentials_updated_at
    BEFORE UPDATE ON local_credentials
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TABLE auth_identities (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider    VARCHAR(255) NOT NULL,
    uid         VARCHAR(255) NOT NULL,
    picture     VARCHAR(2048),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_auth_identities_provider_uid UNIQUE (provider, uid),
    CONSTRAINT uq_auth_identities_user_provider UNIQUE (user_id, provider)
);

CREATE INDEX idx_auth_identities_user_id ON auth_identities (user_id);

CREATE TRIGGER trg_auth_identities_updated_at
    BEFORE UPDATE ON auth_identities
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

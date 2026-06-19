CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                user_id UUID NOT NULL,
                                token_hash VARCHAR(255) NOT NULL UNIQUE,
                                status VARCHAR(30) NOT NULL,
                                expires_at TIMESTAMP NOT NULL,
                                revoked_at TIMESTAMP NULL,
                                created_at TIMESTAMP NOT NULL,
                                updated_at TIMESTAMP NOT NULL,

                                CONSTRAINT fk_refresh_tokens_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES app_users(id)
                                        ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens(user_id);

CREATE INDEX idx_refresh_tokens_token_hash
    ON refresh_tokens(token_hash);

CREATE INDEX idx_refresh_tokens_status
    ON refresh_tokens(status);

CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens(expires_at);
CREATE TABLE accounts (
                          id UUID PRIMARY KEY,
                          user_id UUID NOT NULL,
                          account_number VARCHAR(50) NOT NULL UNIQUE,
                          account_type VARCHAR(30) NOT NULL,
                          status VARCHAR(30) NOT NULL,
                          balance NUMERIC(19, 2) NOT NULL,
                          currency VARCHAR(3) NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,

                          CONSTRAINT fk_accounts_user
                              FOREIGN KEY (user_id)
                                  REFERENCES app_users(id)
                                  ON DELETE CASCADE
);

CREATE INDEX idx_accounts_user_id
    ON accounts(user_id);

CREATE INDEX idx_accounts_account_number
    ON accounts(account_number);

CREATE INDEX idx_accounts_status
    ON accounts(status);


CREATE TABLE transactions (
                              id UUID PRIMARY KEY,
                              source_account_id UUID NOT NULL,
                              destination_account_id UUID NOT NULL,
                              transaction_type VARCHAR(30) NOT NULL,
                              status VARCHAR(30) NOT NULL,
                              amount NUMERIC(19, 2) NOT NULL,
                              currency VARCHAR(3) NOT NULL,
                              description VARCHAR(255),
                              idempotency_key VARCHAR(100) NOT NULL UNIQUE,
                              created_by_user_id UUID NOT NULL,
                              created_at TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP NOT NULL,

                              CONSTRAINT fk_transactions_source_account
                                  FOREIGN KEY (source_account_id)
                                      REFERENCES accounts(id),

                              CONSTRAINT fk_transactions_destination_account
                                  FOREIGN KEY (destination_account_id)
                                      REFERENCES accounts(id),

                              CONSTRAINT fk_transactions_created_by_user
                                  FOREIGN KEY (created_by_user_id)
                                      REFERENCES app_users(id),

                              CONSTRAINT chk_transactions_amount_positive
                                  CHECK (amount > 0)
);

CREATE INDEX idx_transactions_source_account_id
    ON transactions(source_account_id);

CREATE INDEX idx_transactions_destination_account_id
    ON transactions(destination_account_id);

CREATE INDEX idx_transactions_created_by_user_id
    ON transactions(created_by_user_id);

CREATE INDEX idx_transactions_status
    ON transactions(status);

CREATE INDEX idx_transactions_created_at
    ON transactions(created_at);

CREATE INDEX idx_transactions_idempotency_key
    ON transactions(idempotency_key);
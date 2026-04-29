CREATE TABLE IF NOT EXISTS orders (
                                      id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      symbol      VARCHAR(20)    NOT NULL,
                                      side        VARCHAR(10)    NOT NULL CHECK (side IN ('BUY', 'SELL')),
                                      quantity    DECIMAL(18, 8) NOT NULL,
                                      price       DECIMAL(18, 8) NOT NULL,
                                      status      VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
                                      created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
                                      updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);
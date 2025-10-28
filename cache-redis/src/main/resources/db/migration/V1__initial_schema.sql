CREATE TABLE IF NOT EXISTS category (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          TEXT        NOT NULL,
    slug          TEXT        NOT NULL UNIQUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS product (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku           TEXT        NOT NULL UNIQUE,
    name          TEXT        NOT NULL,
    description   TEXT,
    category_id   UUID        NOT NULL REFERENCES category(id) ON DELETE RESTRICT,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_product_category_id ON product(category_id);

CREATE TABLE IF NOT EXISTS product_price (
    product_id    UUID        NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    currency      VARCHAR(3)  NOT NULL,
    amount        NUMERIC(19,2) NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (product_id, currency)
);

CREATE INDEX IF NOT EXISTS idx_product_price_product_id ON product_price(product_id);

CREATE TABLE IF NOT EXISTS inventory (
    product_id    UUID        NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    warehouse_id  BIGINT      NOT NULL,
    quantity      INTEGER     NOT NULL CHECK (quantity >= 0),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (product_id, warehouse_id)
);

CREATE INDEX IF NOT EXISTS idx_inventory_product_id ON inventory(product_id);

CREATE TABLE IF NOT EXISTS review (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id    UUID        NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    rating        SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    text          TEXT,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_review_product_id ON review(product_id);
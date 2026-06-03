-- liquibase formatted sql

-- changeset system:create-schema
CREATE TABLE products (
    sku         VARCHAR(50)  PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE inventory (
    sku             VARCHAR(50) PRIMARY KEY REFERENCES products(sku),
    total_stock     INTEGER     NOT NULL CHECK (total_stock >= 0),
    available_stock INTEGER     NOT NULL CHECK (available_stock >= 0),
    reserved_stock  INTEGER     NOT NULL DEFAULT 0 CHECK (reserved_stock >= 0)
);

CREATE TABLE reservations (
    id       UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id VARCHAR(100) NOT NULL UNIQUE,
    status   VARCHAR(20)  NOT NULL,
    -- tracks when the reservation was requested (required by spec)
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- tracks the last state transition (PENDING→CONFIRMED/CANCELLED) for audit purposes
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE reservation_items (
    reservation_id UUID        NOT NULL REFERENCES reservations(id),
    sku            VARCHAR(50) NOT NULL REFERENCES products(sku),
    quantity       INTEGER     NOT NULL CHECK (quantity > 0),
    -- composite PK enforces that a single SKU cannot appear twice in the same reservation
    PRIMARY KEY (reservation_id, sku)
);

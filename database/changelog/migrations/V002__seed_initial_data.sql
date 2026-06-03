-- liquibase formatted sql

-- changeset system:seed-initial-data
INSERT INTO products (sku, name, description) VALUES
    ('A100', 'Widget Alpha',      'Standard aluminum widget, grade A'),
    ('B200', 'Gadget Beta',       'Precision electronic gadget, series B'),
    ('C300', 'Component Gamma',   'Industrial component, type Gamma');

INSERT INTO inventory (sku, total_stock, available_stock, reserved_stock) VALUES
    ('A100', 100, 100, 0),
    ('B200',  50,  50, 0),
    ('C300', 200, 200, 0);

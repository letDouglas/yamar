ALTER TABLE orders
    ADD COLUMN customer_email    VARCHAR(255),
    ADD COLUMN shipping_street   VARCHAR(255),
    ADD COLUMN shipping_city     VARCHAR(100),
    ADD COLUMN shipping_zip_code VARCHAR(20),
    ADD COLUMN shipping_country  VARCHAR(100),
    ADD COLUMN billing_street    VARCHAR(255),
    ADD COLUMN billing_city      VARCHAR(100),
    ADD COLUMN billing_zip_code  VARCHAR(20),
    ADD COLUMN billing_country   VARCHAR(100);
CREATE TABLE orders
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id        VARCHAR(255),
    order_number       VARCHAR(255) NOT NULL UNIQUE,
    total_amount       DECIMAL(19, 4),
    payment_method     VARCHAR(50),
    order_status       VARCHAR(50)  NOT NULL,
    created_date       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP    NULL     DEFAULT NULL
);

CREATE TABLE order_line
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT,
    product_id     VARCHAR(255),
    quantity       INT NOT NULL,
    price_per_unit DECIMAL(19, 4),
    sub_total      DECIMAL(19, 4),
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE TABLE users
(
    id           VARCHAR(255) NOT NULL PRIMARY KEY,
    email        VARCHAR(255),
    first_name   VARCHAR(255),
    last_name    VARCHAR(255),
    phone_number VARCHAR(50),
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)
);

CREATE TABLE addresses
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    street   VARCHAR(255),
    city     VARCHAR(100),
    zip_code VARCHAR(20),
    country  VARCHAR(100),
    type     VARCHAR(20), -- SHIPPING, BILLING
    user_id  VARCHAR(255),
    CONSTRAINT fk_user_address FOREIGN KEY (user_id) REFERENCES users (id)
);
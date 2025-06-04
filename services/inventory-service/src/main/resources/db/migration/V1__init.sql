CREATE TABLE `inventory`
(
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
    `product_id`         VARCHAR(255) NOT NULL,
    `quantity`           INT          NOT NULL DEFAULT 0,
    `threshold`          INT          NOT NULL DEFAULT 0,
    `status`             VARCHAR(50)  NOT NULL,
    `last_modified_date` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

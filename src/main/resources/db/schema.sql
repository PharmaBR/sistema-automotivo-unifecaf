-- =====================================================================
-- Sistema Automotivo - Modelo físico (MySQL 8)
-- Executado automaticamente na inicialização (spring.sql.init.mode=always).
-- "IF NOT EXISTS" torna o script idempotente: pode rodar a cada start.
-- =====================================================================

CREATE TABLE IF NOT EXISTS brands (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(80)  NOT NULL,
    country     VARCHAR(60)  NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_brands_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS vehicle_models (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    brand_id    BIGINT       NOT NULL,
    name        VARCHAR(80)  NOT NULL,
    category    VARCHAR(20)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_models_brand_name UNIQUE (brand_id, name),
    CONSTRAINT fk_models_brand FOREIGN KEY (brand_id) REFERENCES brands (id)
);

CREATE TABLE IF NOT EXISTS vehicles (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    model_id          BIGINT         NOT NULL,
    manufacture_year  INT            NOT NULL,
    color             VARCHAR(40)    NOT NULL,
    price             DECIMAL(12, 2) NOT NULL,
    mileage           INT            NOT NULL,
    status            VARCHAR(20)    NOT NULL,
    created_at        DATETIME(6)    NOT NULL,
    updated_at        DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_vehicles_model FOREIGN KEY (model_id) REFERENCES vehicle_models (id),
    CONSTRAINT ck_vehicles_price CHECK (price > 0),
    CONSTRAINT ck_vehicles_mileage CHECK (mileage >= 0),
    -- Índices para os filtros mais usados (requisito não funcional de performance)
    INDEX idx_vehicles_status (status),
    INDEX idx_vehicles_price (price),
    INDEX idx_vehicles_year (manufacture_year)
);

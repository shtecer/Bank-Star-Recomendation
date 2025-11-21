-- Таблица продуктов
CREATE TABLE products (
id UUID PRIMARY KEY,
product_name VARCHAR(255) NOT NULL,
description TEXT,
type VARCHAR(50),
active BOOLEAN DEFAULT true,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица правил
CREATE TABLE product_rules (
id UUID PRIMARY KEY,
product_id UUID NOT NULL REFERENCES products(id),
rule_name VARCHAR(255) NOT NULL,
rule_description TEXT,
condition_type VARCHAR(50) NOT NULL,
condition_json TEXT NOT NULL,
active BOOLEAN DEFAULT true,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Пример заполнения продуктов
INSERT INTO products (id, product_name, description, type) VALUES
('123e4567-e89b-12d3-a456-426614174000', 'Продукт А', 'Инвестиционный портфель', 'INVESTMENT'),
('123e4567-e89b-12d3-a456-426614174001', 'Продукт Б', 'Премиальная карта', 'CARD'),
('123e4567-e89b-12d3-a456-426614174002', 'Продукт В', 'Ипотечный кредит', 'CREDIT');
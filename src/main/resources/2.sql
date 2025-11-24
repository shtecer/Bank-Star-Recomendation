-- Создание таблицы products
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы product_rules
CREATE TABLE IF NOT EXISTS product_rules (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    rule_description TEXT,
    condition_type VARCHAR(50) NOT NULL,
    condition_json TEXT NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы transactions
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы users
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Создание индексов для улучшения производительности
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_product_id ON transactions(product_id);
CREATE INDEX IF NOT EXISTS idx_product_rules_product_id ON product_rules(product_id);
CREATE INDEX IF NOT EXISTS idx_product_rules_active ON product_rules(active);

CREATE DATABASE rulesdb;
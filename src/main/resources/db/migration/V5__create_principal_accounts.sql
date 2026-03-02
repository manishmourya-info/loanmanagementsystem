-- Flyway Migration V5: Create Principal Accounts Table
-- Description: Create principal_accounts table for consumer's primary banking accounts
-- Deployed: 2026-02-25

DROP TABLE IF EXISTS principal_accounts;

CREATE TABLE principal_accounts (
  principal_account_id BINARY(16) NOT NULL PRIMARY KEY COMMENT 'Unique principal account identifier (UUID)',
  consumer_id BINARY(16) NOT NULL UNIQUE COMMENT 'Foreign Key to consumers.consumer_id',
  account_number VARCHAR(34) NOT NULL UNIQUE COMMENT 'Account number (IBAN compliant)',
  account_holder_name VARCHAR(100) NOT NULL COMMENT 'Account holder name',
  bank_code VARCHAR(20) NOT NULL COMMENT 'Bank code',
  verification_status ENUM(
    'PENDING', 'VERIFIED', 'FAILED', 'REJECTED'
  ) NOT NULL DEFAULT 'PENDING',
  linked_date TIMESTAMP NULL COMMENT 'Date when account was linked',
  verified_date TIMESTAMP NULL COMMENT 'Date when account was verified',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version BIGINT DEFAULT 0,
  CONSTRAINT fk_principal_account_consumer FOREIGN KEY (consumer_id) REFERENCES consumers(consumer_id),
  CONSTRAINT uk_account_number UNIQUE (account_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_consumer_id ON principal_accounts(consumer_id);
CREATE INDEX idx_verification_status ON principal_accounts(verification_status);

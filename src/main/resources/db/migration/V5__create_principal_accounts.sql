-- Flyway Migration V5: Initialize Principal Account Table
-- Description: Create principal_accounts table for linking consumer bank accounts used in loan disbursement and repayment
-- Deployed: 2026-02-26
-- Validated: MySQL 8.0 schema with FK constraints and verification status tracking

CREATE TABLE IF NOT EXISTS principal_accounts (
  principal_account_id CHAR(36) NOT NULL COMMENT 'Unique principal account identifier (UUID)',
  consumer_id CHAR(36) NOT NULL COMMENT 'Foreign Key to consumers.consumer_id',
  account_number VARCHAR(34) NOT NULL UNIQUE COMMENT 'Bank account number (IBAN compliant, max 34 chars)',
  account_holder_name VARCHAR(100) NOT NULL COMMENT 'Name of account holder',
  bank_code VARCHAR(20) NOT NULL COMMENT 'Bank identification code',
  verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Account verification status: PENDING, VERIFIED, REJECTED',
  linked_date TIMESTAMP NULL COMMENT 'Date when account was linked to consumer',
  verified_date TIMESTAMP NULL COMMENT 'Date when account was verified',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  version BIGINT DEFAULT 0 COMMENT 'Optimistic locking version',

  -- Constraints: Data validity checks
  CONSTRAINT chk_account_number_length CHECK (CHAR_LENGTH(account_number) >= 8 AND CHAR_LENGTH(account_number) <= 34),
  CONSTRAINT chk_account_holder_name_length CHECK (CHAR_LENGTH(account_holder_name) >= 2 AND CHAR_LENGTH(account_holder_name) <= 100),
  CONSTRAINT chk_bank_code_length CHECK (CHAR_LENGTH(bank_code) >= 2 AND CHAR_LENGTH(bank_code) <= 20),
  CONSTRAINT chk_verification_status_valid CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')),

  -- Foreign Key Constraint
  CONSTRAINT fk_principal_account_consumer FOREIGN KEY (consumer_id)
    REFERENCES consumers(consumer_id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
    COMMENT 'Ensure consumer exists',

  -- Unique Constraint: One principal account per consumer
  CONSTRAINT uk_consumer_principal_account UNIQUE KEY (consumer_id)
    COMMENT 'Each consumer can have only one principal account',

  -- Indices for query performance
  INDEX idx_consumer_id (consumer_id) COMMENT 'Lookup by consumer',
  INDEX idx_verification_status (verification_status) COMMENT 'Filter by verification status',
  INDEX idx_created_at (created_at) COMMENT 'Sort by creation date'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Principal Account Table - Consumer bank account for loan transactions'
;

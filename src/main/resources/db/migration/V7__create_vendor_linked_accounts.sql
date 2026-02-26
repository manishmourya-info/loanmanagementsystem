-- Flyway Migration V7: Initialize Vendor Linked Account Table
-- Description: Create vendor_linked_accounts table for vendor settlement and transaction accounts
-- Deployed: 2026-02-26
-- Validated: MySQL 8.0 schema with FK constraints and account status tracking

CREATE TABLE IF NOT EXISTS vendor_linked_accounts (
  vendor_account_id CHAR(36) NOT NULL COMMENT 'Unique vendor account identifier (UUID)',
  vendor_id CHAR(36) NOT NULL COMMENT 'Foreign Key to vendors.vendor_id',
  account_number VARCHAR(34) NOT NULL UNIQUE COMMENT 'Vendor bank account number (IBAN compliant)',
  account_type VARCHAR(50) NOT NULL COMMENT 'Account type: SETTLEMENT, ESCROW, OPERATING, etc.',
  account_details JSON NOT NULL COMMENT 'Account metadata: {bankCode, ifscCode, accountName, branchCode, etc.}',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Account status: PENDING, ACTIVE, INACTIVE, DISABLED',
  activation_date TIMESTAMP NULL COMMENT 'Date when account was activated',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  version BIGINT DEFAULT 0 COMMENT 'Optimistic locking version',

  -- Constraints: Data validity checks
  CONSTRAINT chk_account_number_length CHECK (CHAR_LENGTH(account_number) >= 8 AND CHAR_LENGTH(account_number) <= 34),
  CONSTRAINT chk_account_type_length CHECK (CHAR_LENGTH(account_type) >= 2 AND CHAR_LENGTH(account_type) <= 50),
  CONSTRAINT chk_status_valid CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE', 'DISABLED')),

  -- Foreign Key Constraint
  CONSTRAINT fk_vendor_linked_account FOREIGN KEY (vendor_id)
    REFERENCES vendors(vendor_id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
    COMMENT 'Ensure vendor exists',

  -- Indices for query performance
  INDEX idx_vendor_id (vendor_id) COMMENT 'Lookup accounts by vendor',
  INDEX idx_status (status) COMMENT 'Filter by account status',
  INDEX idx_account_number (account_number) COMMENT 'Account number lookup',
  INDEX idx_created_at (created_at) COMMENT 'Sort by creation date',

  PRIMARY KEY (vendor_account_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Vendor Linked Account Table - Settlement and transaction accounts for vendors'
;

-- Flyway Migration V7: Create Vendor Linked Accounts Table
-- Description: Create vendor_linked_accounts table (UPDATED schema without status/activationDate)
-- Deployed: 2026-02-28
-- Updated: Removed account_details, account_number, account_type, status, activation_date
--          Added principal_account_id for mapping with PrincipalAccount

DROP TABLE IF EXISTS vendor_linked_accounts;

CREATE TABLE vendor_linked_accounts (
  vendor_account_id CHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique vendor account identifier (UUID)',
  vendor_id CHAR(36) NOT NULL COMMENT 'Foreign Key to vendors.vendor_id',
  principal_account_id CHAR(36) NOT NULL COMMENT 'Foreign Key to principal_accounts.principal_account_id',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  version BIGINT DEFAULT 0 COMMENT 'Optimistic locking version',

  -- Foreign Key Constraints
  CONSTRAINT fk_vendor_linked_account FOREIGN KEY (vendor_id)
    REFERENCES vendors(vendor_id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,

  CONSTRAINT fk_vendor_linked_principal_account FOREIGN KEY (principal_account_id)
    REFERENCES principal_accounts(principal_account_id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,

  -- Indices for query performance
  INDEX idx_vendor_id (vendor_id) COMMENT 'Lookup accounts by vendor',
  INDEX idx_principal_account_id (principal_account_id) COMMENT 'Lookup by principal account',
  INDEX idx_created_at (created_at) COMMENT 'Sort by creation date'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Vendor Linked Account Table - Maps vendor accounts to principal accounts for transactions';

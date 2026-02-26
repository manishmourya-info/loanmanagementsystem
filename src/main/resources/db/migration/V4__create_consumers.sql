-- Flyway Migration V4: Initialize Consumer Table
-- Description: Create the consumers table for customer registration and profile management
-- Deployed: 2026-02-26
-- Validated: MySQL 8.0 schema with UUID primary key and comprehensive constraints

CREATE TABLE IF NOT EXISTS consumers (
  consumer_id CHAR(36) NOT NULL COMMENT 'Unique consumer identifier (UUID)',
  name VARCHAR(100) NOT NULL COMMENT 'Consumer full name',
  email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Consumer email (unique)',
  phone VARCHAR(15) NOT NULL COMMENT 'Consumer phone number (E.164 format)',
  identity_type VARCHAR(50) NOT NULL COMMENT 'Identity document type: AADHAR, PAN, DL, PASSPORT, etc.',
  identity_number VARCHAR(50) NOT NULL COMMENT 'Identity document number',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Consumer status: ACTIVE, INACTIVE, SUSPENDED, CLOSED',
  kyc_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'KYC verification status: PENDING, VERIFIED, REJECTED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  version BIGINT DEFAULT 0 COMMENT 'Optimistic locking version',

  -- Constraints: Data validity checks
  CONSTRAINT chk_name_length CHECK (CHAR_LENGTH(name) >= 2 AND CHAR_LENGTH(name) <= 100),
  CONSTRAINT chk_phone_format CHECK (CHAR_LENGTH(phone) >= 8 AND CHAR_LENGTH(phone) <= 15),
  CONSTRAINT chk_identity_type CHECK (identity_type IN ('AADHAR', 'PAN', 'DL', 'PASSPORT', 'VOTER_ID', 'OTHER')),
  CONSTRAINT chk_status_valid CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED')),
  CONSTRAINT chk_kyc_status_valid CHECK (kyc_status IN ('PENDING', 'VERIFIED', 'REJECTED')),

  -- Indices for query performance
  INDEX idx_email (email) COMMENT 'Unique email lookup',
  INDEX idx_phone (phone) COMMENT 'Phone-based search',
  INDEX idx_status (status) COMMENT 'Filter by consumer status',
  INDEX idx_created_at (created_at) COMMENT 'Sort by creation date',

  PRIMARY KEY (consumer_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Consumer/Customer Master Table - Individual customer profiles'
;

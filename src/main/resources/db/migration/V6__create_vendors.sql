-- Flyway Migration V6: Initialize Vendor Table
-- Description: Create vendors table for merchant/service provider registration and management
-- Deployed: 2026-02-26
-- Validated: MySQL 8.0 schema with business registration and compliance tracking

CREATE TABLE IF NOT EXISTS vendors (
  vendor_id CHAR(36) NOT NULL COMMENT 'Unique vendor identifier (UUID)',
  vendor_name VARCHAR(100) NOT NULL COMMENT 'Business name of the vendor',
  business_type VARCHAR(50) NOT NULL COMMENT 'Type of business: MERCHANT, DISTRIBUTOR, SERVICE_PROVIDER, etc.',
  registration_number VARCHAR(50) NOT NULL UNIQUE COMMENT 'Business registration number',
  gst_number VARCHAR(20) UNIQUE COMMENT 'GST registration number (optional, unique if provided)',
  contact_email VARCHAR(100) NOT NULL COMMENT 'Primary contact email',
  contact_phone VARCHAR(15) NOT NULL COMMENT 'Primary contact phone (E.164 format)',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Vendor status: ACTIVE, INACTIVE, SUSPENDED',
  registration_date TIMESTAMP NOT NULL COMMENT 'Date of vendor business registration',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
  version BIGINT DEFAULT 0 COMMENT 'Optimistic locking version',

  -- Constraints: Data validity checks
  CONSTRAINT chk_vendor_name_length CHECK (CHAR_LENGTH(vendor_name) >= 2 AND CHAR_LENGTH(vendor_name) <= 100),
  CONSTRAINT chk_contact_email_length CHECK (CHAR_LENGTH(contact_email) >= 5 AND CHAR_LENGTH(contact_email) <= 100),
  CONSTRAINT chk_contact_phone_length CHECK (CHAR_LENGTH(contact_phone) >= 8 AND CHAR_LENGTH(contact_phone) <= 15),
  CONSTRAINT chk_status_valid CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
  CONSTRAINT chk_gst_number_length CHECK (gst_number IS NULL OR CHAR_LENGTH(gst_number) = 15),

  -- Indices for query performance
  INDEX idx_registration_number (registration_number) COMMENT 'Business registration lookup',
  INDEX idx_gst_number (gst_number) COMMENT 'GST number lookup',
  INDEX idx_status (status) COMMENT 'Filter by vendor status',
  INDEX idx_registration_date (registration_date) COMMENT 'Sort by registration date',
  INDEX idx_created_at (created_at) COMMENT 'Sort by creation date'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Vendor Master Table - Merchant/service provider profiles'
;

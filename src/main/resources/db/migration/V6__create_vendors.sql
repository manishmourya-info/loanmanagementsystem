-- Flyway Migration V6: Create Vendors Table
-- Description: Create vendors table for vendor information
-- Deployed: 2026-02-25

DROP TABLE IF EXISTS vendors;

CREATE TABLE vendors (
  vendor_id CHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique vendor identifier (UUID)',
  vendor_name VARCHAR(100) NOT NULL COMMENT 'Vendor name',
  business_type VARCHAR(50) NOT NULL COMMENT 'Business type',
  registration_number VARCHAR(50) NOT NULL UNIQUE COMMENT 'Registration number',
  gst_number VARCHAR(15) NOT NULL UNIQUE COMMENT 'GST number',
  contact_email VARCHAR(100) NOT NULL COMMENT 'Contact email',
  contact_phone VARCHAR(20) NOT NULL COMMENT 'Contact phone',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Vendor status',
  registration_date TIMESTAMP NOT NULL COMMENT 'Registration date',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_registration_number ON vendors(registration_number);
CREATE INDEX idx_status ON vendors(status);

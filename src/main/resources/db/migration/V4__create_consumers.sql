-- Flyway Migration V4: Create Consumers Table
-- Description: Create consumers table for consumer information
-- Deployed: 2026-02-25

CREATE TABLE IF NOT EXISTS consumers (
  consumer_id CHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique consumer identifier (UUID)',
  email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Consumer email address',
  first_name VARCHAR(50) NOT NULL COMMENT 'First name',
  last_name VARCHAR(50) NOT NULL COMMENT 'Last name',
  phone VARCHAR(20) NOT NULL COMMENT 'Phone number',
  date_of_birth DATE NOT NULL COMMENT 'Date of birth',
  pan_number VARCHAR(20) NOT NULL UNIQUE COMMENT 'PAN number',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Consumer status',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_email ON consumers(email);
CREATE INDEX idx_status ON consumers(status);

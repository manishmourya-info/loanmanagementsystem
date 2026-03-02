-- Flyway Migration V4: Create Consumers Table
-- Description: Create consumers table for consumer information
-- Deployed: 2026-02-25

DROP TABLE IF EXISTS consumers;

DROP TABLE IF EXISTS consumers;

CREATE TABLE consumers (
  consumer_id BINARY(16) NOT NULL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  phone VARCHAR(15) NOT NULL,
  identity_type VARCHAR(50) NOT NULL,
  identity_number VARCHAR(50) NOT NULL,
  status ENUM('ACTIVE','INACTIVE','SUSPENDED','CLOSED') NOT NULL,
  kyc_status ENUM('PENDING','VERIFIED','REJECTED','EXPIRED') NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  version BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_email ON consumers(email);
CREATE INDEX idx_status ON consumers(status);
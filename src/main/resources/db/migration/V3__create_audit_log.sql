-- Flyway Migration V3: Create Audit Log Table
-- Description: Create audit_log table for tracking entity changes
-- Deployed: 2026-02-20

CREATE TABLE IF NOT EXISTS audit_log (
  audit_id CHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique audit identifier (UUID)',
  entity_type VARCHAR(50) NOT NULL COMMENT 'Entity type',
  entity_id CHAR(36) NOT NULL COMMENT 'Entity identifier',
  action VARCHAR(20) NOT NULL COMMENT 'Action: CREATE, UPDATE, DELETE',
  changes JSON COMMENT 'Changes made',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_entity_id ON audit_log(entity_id);
CREATE INDEX idx_entity_type ON audit_log(entity_type);

-- Flyway Migration V3: Initialize Audit Log Table (Optional - for compliance)
-- Description: Create audit_log table for comprehensive operation tracking and compliance
-- Deployed: 2026-02-25
-- Note: Optional for MVP, required for production compliance

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique audit record identifier',
  entity_type VARCHAR(50) NOT NULL COMMENT 'Entity type: PersonalLoan, LoanRepayment, etc.',
  entity_id BIGINT NOT NULL COMMENT 'Reference ID to the modified entity',
  action VARCHAR(20) NOT NULL COMMENT 'Action: CREATE, UPDATE, DELETE, APPROVE, REJECT, PROCESS_PAYMENT',
  old_values JSON NULL COMMENT 'Previous values before change (NULL for CREATE)',
  new_values JSON NULL COMMENT 'New values after change',
  user_id VARCHAR(50) NULL COMMENT 'User who performed action (NULL for system)',
  ip_address VARCHAR(45) NULL COMMENT 'Client IP address (IPv4 or IPv6)',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Audit entry timestamp',

  -- Constraints
  CONSTRAINT chk_entity_type CHECK (entity_type IN ('PersonalLoan', 'LoanRepayment', 'Customer')),
  CONSTRAINT chk_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT', 'PROCESS_PAYMENT')),

  -- Indices for audit queries
  INDEX idx_entity (entity_type, entity_id) COMMENT 'Find audit trail for specific entity',
  INDEX idx_action (action) COMMENT 'Filter by action type',
  INDEX idx_created_at (created_at) COMMENT 'Query by timestamp range',
  INDEX idx_user_id (user_id) COMMENT 'Find actions by user'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Audit Log - Comprehensive operation tracking for compliance and troubleshooting'
;

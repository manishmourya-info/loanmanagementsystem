-- Flyway Migration V3: Create Audit Log Table
-- Description: Create audit_logs table for tracking entity changes
-- Deployed: 2026-02-20

DROP TABLE IF EXISTS audit_logs;

CREATE TABLE audit_logs (
  audit_id BINARY(16) NOT NULL PRIMARY KEY,
  action VARCHAR(100) NOT NULL,
  loan_id VARCHAR(255),
  user_id VARCHAR(100) NOT NULL,
  amount DECIMAL(19,2),
  details JSON,
  status ENUM('SUCCESS','FAILURE','PARTIAL') NOT NULL,
  timestamp DATETIME NOT NULL,
  ip_address VARCHAR(45)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_loan_id ON audit_logs(loan_id);
CREATE INDEX idx_user_id ON audit_logs(user_id);
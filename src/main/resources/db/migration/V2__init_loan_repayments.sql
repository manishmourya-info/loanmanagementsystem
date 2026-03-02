-- Flyway Migration V2: Initialize Loan Repayments Table
-- Description: Create loan_repayments table for tracking loan repayments
-- Deployed: 2026-02-20

DROP TABLE IF EXISTS loan_repayments;

CREATE TABLE loan_repayments (
  repayment_id CHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique repayment identifier (UUID)',
  loan_id CHAR(36) NOT NULL COMMENT 'Foreign Key to personal_loans.loan_id',
  repayment_amount DECIMAL(15, 2) NOT NULL COMMENT 'Repayment amount',
  repayment_date TIMESTAMP NOT NULL COMMENT 'Repayment date',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Repayment status',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version BIGINT DEFAULT 0,
  CONSTRAINT fk_loan_repayments FOREIGN KEY (loan_id) REFERENCES personal_loans(loan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_loan_id ON loan_repayments(loan_id);
CREATE INDEX idx_status ON loan_repayments(status);

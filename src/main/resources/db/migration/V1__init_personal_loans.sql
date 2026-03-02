-- Flyway Migration V1: Initialize Personal Loans Table
-- Description: Create personal_loans table for loan management
-- Deployed: 2026-02-20

DROP TABLE IF EXISTS personal_loans;

CREATE TABLE personal_loans (
  loan_id CHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique loan identifier (UUID)',
  consumer_id CHAR(36) NOT NULL COMMENT 'Foreign Key to consumers.consumer_id',
  principal_amount DECIMAL(15, 2) NOT NULL COMMENT 'Loan principal amount',
  interest_rate DECIMAL(5, 2) NOT NULL COMMENT 'Annual interest rate (percentage)',
  tenure_months INT NOT NULL COMMENT 'Loan tenure in months',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Loan status',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_consumer_id ON personal_loans(consumer_id);
CREATE INDEX idx_status ON personal_loans(status);

-- Foreign key constraint added in V9 after consumers table is created

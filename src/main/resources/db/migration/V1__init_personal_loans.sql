-- Flyway Migration V1: Initialize Personal Loans Table
-- Description: Create personal_loans table for loan management
-- Deployed: 2026-02-20

DROP TABLE IF EXISTS personal_loans;

CREATE TABLE personal_loans (
  loan_id BINARY(16) NOT NULL PRIMARY KEY COMMENT 'Unique loan identifier (UUID)',

  consumer_id BINARY(16) NOT NULL 
    COMMENT 'Foreign Key to consumers.consumer_id',

  principal DECIMAL(19, 2) NOT NULL 
    COMMENT 'Loan principal amount',

  annual_interest_rate DECIMAL(5, 2) NOT NULL 
    COMMENT 'Annual interest rate (percentage)',

  tenure_months INT NOT NULL 
    COMMENT 'Loan tenure in months',

  monthly_emi DECIMAL(19, 2) NOT NULL 
    COMMENT 'Calculated monthly EMI amount',

  total_interest_payable DECIMAL(19, 2) DEFAULT 0.00 
    COMMENT 'Total interest payable over loan tenure',

  outstanding_balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00 
    COMMENT 'Remaining outstanding balance',

  remaining_tenure INT NOT NULL DEFAULT 0 
    COMMENT 'Remaining tenure in months',

  status ENUM(
    'PENDING',
    'APPROVED',
    'ACTIVE',
    'CLOSED',
    'REJECTED',
    'DEFAULTED'
  ) NOT NULL DEFAULT 'PENDING',

  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP 
    ON UPDATE CURRENT_TIMESTAMP,

  approved_at TIMESTAMP NULL DEFAULT NULL,

  rejected_at TIMESTAMP NULL DEFAULT NULL,

  closed_at TIMESTAMP NULL DEFAULT NULL,

  approval_remarks VARCHAR(500) NULL,

  rejection_reason VARCHAR(500) NULL,

  version BIGINT NOT NULL DEFAULT 0 
    COMMENT 'Optimistic locking version'

) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci;

-- Indexes
CREATE INDEX idx_consumer_id ON personal_loans(consumer_id);
CREATE INDEX idx_status ON personal_loans(status);
CREATE INDEX idx_created_at ON personal_loans(created_at);

-- Foreign key constraint added in V9 after consumers table is created
-- Flyway Migration V2: Initialize Loan Repayments Schedule Table
-- Description: Create loan_repayments table for EMI schedule tracking and payment history
-- Deployed: 2026-02-25
-- Validated: MySQL 8.0 schema with FK constraints, unique constraints, and atomic payment recording

CREATE TABLE IF NOT EXISTS loan_repayments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique repayment record identifier',
  loan_id BIGINT NOT NULL COMMENT 'Foreign Key to personal_loans.id',
  installment_number INT NOT NULL COMMENT 'Sequence number of this EMI (1, 2, 3...)',
  principal_amount DECIMAL(15,2) NOT NULL COMMENT 'Principal portion of this EMI (amortized)',
  interest_amount DECIMAL(15,2) NOT NULL COMMENT 'Interest portion of this EMI',
  total_amount DECIMAL(15,2) NOT NULL COMMENT 'Total EMI = principal + interest (2 decimal places)',
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Payment status: PENDING, PAID, PARTIALLY_PAID, OVERDUE, WAIVED',
  due_date TIMESTAMP NOT NULL COMMENT 'Due date for this EMI payment',
  paid_date TIMESTAMP NULL COMMENT 'Actual payment date (null if not paid)',
  paid_amount DECIMAL(15,2) NULL COMMENT 'Actual amount paid (null if not paid)',
  payment_mode VARCHAR(50) NULL COMMENT 'Payment method: ONLINE, CHEQUE, CASH, BANK_TRANSFER, etc.',
  transaction_reference VARCHAR(100) NULL COMMENT 'Payment reference/receipt/transaction ID',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',

  -- Foreign Key Constraint
  CONSTRAINT fk_loan_id FOREIGN KEY (loan_id)
    REFERENCES personal_loans(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
    COMMENT 'Prevent orphaned repayment records',

  -- Unique constraint: One installment per loan
  CONSTRAINT uk_loan_installment UNIQUE KEY (loan_id, installment_number)
    COMMENT 'Ensure no duplicate installments for same loan',

  -- Constraints: Financial validity checks
  CONSTRAINT chk_total_amount CHECK (total_amount > 0),
  CONSTRAINT chk_principal_amount CHECK (principal_amount >= 0),
  CONSTRAINT chk_interest_amount CHECK (interest_amount >= 0),
  CONSTRAINT chk_paid_amount CHECK (paid_amount IS NULL OR paid_amount >= 0),

  -- Indices for query performance
  INDEX idx_loan_id (loan_id) COMMENT 'Fetch all EMIs for a loan',
  INDEX idx_status (status) COMMENT 'Find pending/overdue/paid EMIs',
  INDEX idx_due_date (due_date) COMMENT 'Identify upcoming/overdue payments',
  INDEX idx_paid_date (paid_date) COMMENT 'Query payment history',
  INDEX idx_created_at (created_at) COMMENT 'Audit trail sorting',

  PRIMARY KEY (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='EMI Schedule & Payment History - Transaction audit trail with status tracking'
;

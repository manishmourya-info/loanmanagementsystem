-- Flyway Migration V1: Initialize Personal Loans Table
-- Description: Create the core personal_loans table with all required fields, constraints, and indices
-- Deployed: 2026-02-25
-- Validated: MySQL 8.0 schema with ACID compliance and financial precision

CREATE TABLE IF NOT EXISTS personal_loans (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique loan identifier',
  customer_id VARCHAR(50) NOT NULL COMMENT 'Customer reference (PK to future Customer table)',
  principal_amount DECIMAL(15,2) NOT NULL COMMENT 'Loan amount in currency units (2 decimal precision)',
  annual_interest_rate DECIMAL(5,2) NOT NULL COMMENT 'Annual interest rate as percentage (5.2 precision)',
  loan_tenure_months INT NOT NULL COMMENT 'Loan duration in months',
  monthly_emi DECIMAL(15,2) NOT NULL COMMENT 'Calculated monthly EMI (Equated Monthly Installment)',
  total_interest_payable DECIMAL(15,2) NOT NULL COMMENT 'Total interest over loan term',
  outstanding_balance DECIMAL(15,2) NOT NULL COMMENT 'Remaining principal + interest due',
  remaining_tenure INT NOT NULL COMMENT 'Months remaining until full repayment',
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Loan status: PENDING, APPROVED, ACTIVE, CLOSED, REJECTED, DEFAULTED',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
  approved_at TIMESTAMP NULL COMMENT 'Loan approval timestamp',
  rejected_at TIMESTAMP NULL COMMENT 'Loan rejection timestamp',
  closed_at TIMESTAMP NULL COMMENT 'Loan closure (fully repaid) timestamp',
  approval_remarks VARCHAR(500) NULL COMMENT 'Approval decision comments',
  rejection_reason VARCHAR(500) NULL COMMENT 'Rejection reason from credit decision',

  -- Constraints: Financial validity checks
  CONSTRAINT chk_principal_amount CHECK (principal_amount > 0 AND principal_amount <= 10000000),
  CONSTRAINT chk_annual_interest_rate CHECK (annual_interest_rate >= 0 AND annual_interest_rate <= 25),
  CONSTRAINT chk_loan_tenure_months CHECK (loan_tenure_months >= 6 AND loan_tenure_months <= 360),
  CONSTRAINT chk_monthly_emi CHECK (monthly_emi > 0),
  CONSTRAINT chk_outstanding_balance CHECK (outstanding_balance >= 0),
  CONSTRAINT chk_remaining_tenure CHECK (remaining_tenure >= 0),

  -- Indices for query performance
  INDEX idx_customer_id (customer_id) COMMENT 'Filter loans by customer',
  INDEX idx_status (status) COMMENT 'Filter loans by approval status',
  INDEX idx_created_at (created_at) COMMENT 'Sort loans by creation date',
  INDEX idx_outstanding_balance (outstanding_balance) COMMENT 'Find loans with outstanding balance'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Personal Loan Master Table - Core financial entity'
;

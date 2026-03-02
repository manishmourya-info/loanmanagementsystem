-- Flyway Migration V2: Initialize Loan Repayments Table
-- Description: Create loan_repayments table for tracking loan repayments
-- Deployed: 2026-02-20

DROP TABLE IF EXISTS loan_repayments;

CREATE TABLE loan_repayments (
  repayment_id BINARY(16) NOT NULL PRIMARY KEY,
  loan_id BINARY(16) NOT NULL,
  installment_number INT NOT NULL,
  principal_amount DECIMAL(15,2) NOT NULL,
  interest_amount DECIMAL(15,2) NOT NULL,
  total_amount DECIMAL(15,2) NOT NULL,
  status ENUM('PENDING','PAID','PARTIALLY_PAID','OVERDUE','WAIVED') NOT NULL,
  due_date DATETIME NOT NULL,
  paid_date DATETIME NULL,
  paid_amount DECIMAL(15,2),
  payment_mode VARCHAR(50),
  transaction_reference VARCHAR(100),
  created_at DATETIME NOT NULL,
  CONSTRAINT uk_loan_installment UNIQUE (loan_id, installment_number),
  CONSTRAINT fk_loan_repayments 
      FOREIGN KEY (loan_id) REFERENCES personal_loans(loan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_loan_id ON loan_repayments(loan_id);
CREATE INDEX idx_status ON loan_repayments(status);

-- Flyway Migration V9: Add Foreign Key Constraints
-- Description: Add deferred foreign key constraints after all tables are created
-- Deployed: 2026-03-02

-- Add foreign key constraint to personal_loans
ALTER TABLE personal_loans 
ADD CONSTRAINT fk_personal_loans_consumer 
FOREIGN KEY (consumer_id) REFERENCES consumers(consumer_id);

-- Add foreign key constraint to loan_repayments if not already present
ALTER TABLE loan_repayments
ADD CONSTRAINT fk_loan_repayments_loan 
FOREIGN KEY (loan_id) REFERENCES personal_loans(loan_id);

-- Flyway Migration V8: Insert Sample Data for Testing
-- Description: Populate sample data for all entities with proper relationships
-- Deployed: 2026-02-26
-- Note: Sample data for development and testing purposes

-- ==================== SAMPLE CONSUMERS ====================
INSERT INTO consumers (consumer_id, name, email, phone, identity_type, identity_number, status, kyc_status, created_at, updated_at, version) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Rajesh Kumar', 'rajesh.kumar@email.com', '+919876543210', 'AADHAR', '123456789012', 'ACTIVE', 'VERIFIED', NOW(), NOW(), 0),
('550e8400-e29b-41d4-a716-446655440002', 'Priya Sharma', 'priya.sharma@email.com', '+919123456789', 'PAN', 'ABCDE1234F', 'ACTIVE', 'VERIFIED', NOW(), NOW(), 0),
('550e8400-e29b-41d4-a716-446655440003', 'Amit Patel', 'amit.patel@email.com', '+918765432109', 'AADHAR', '987654321098', 'ACTIVE', 'PENDING', NOW(), NOW(), 0),
('550e8400-e29b-41d4-a716-446655440004', 'Neha Gupta', 'neha.gupta@email.com', '+917654321098', 'DL', 'DL1234567890', 'ACTIVE', 'VERIFIED', NOW(), NOW(), 0),
('550e8400-e29b-41d4-a716-446655440005', 'Vikram Singh', 'vikram.singh@email.com', '+916543210987', 'PASSPORT', 'N1234567', 'ACTIVE', 'VERIFIED', NOW(), NOW(), 0);

-- ==================== SAMPLE PRINCIPAL ACCOUNTS ====================
-- Each consumer has exactly one principal account (1:1 relationship)
INSERT INTO principal_accounts (principal_account_id, consumer_id, account_number, account_holder_name, bank_code, verification_status, linked_date, verified_date, created_at, updated_at, version) VALUES
('650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'IN89SBIN0001234567', 'Rajesh Kumar', 'SBIN0001234', 'VERIFIED', NOW(), NOW(), NOW(), NOW(), 0),
('650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'IN89HDFC0009876543', 'Priya Sharma', 'HDFC0009876', 'VERIFIED', NOW(), NOW(), NOW(), NOW(), 0),
('650e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', 'IN89ICIC0005555555', 'Amit Patel', 'ICIC0005555', 'PENDING', NOW(), NULL, NOW(), NOW(), 0),
('650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440004', 'IN89AXAB0002468135', 'Neha Gupta', 'AXAB0002468', 'VERIFIED', NOW(), NOW(), NOW(), NOW(), 0),
('650e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440005', 'IN89IDIB0001357924', 'Vikram Singh', 'IDIB0001357', 'VERIFIED', NOW(), NOW(), NOW(), NOW(), 0);

-- ==================== SAMPLE VENDORS ====================
INSERT INTO vendors (vendor_id, vendor_name, business_type, registration_number, gst_number, contact_email, contact_phone, status, registration_date, created_at, updated_at, version) VALUES
('750e8400-e29b-41d4-a716-446655440001', 'TechSolutions India Pvt Ltd', 'MERCHANT', 'REG20240001', '18AABCS5055K1ZO', 'contact@techsolutions.com', '+919988776655', 'ACTIVE', '2024-01-15', NOW(), NOW(), 0),
('750e8400-e29b-41d4-a716-446655440002', 'GlobalTrade Partners', 'DISTRIBUTOR', 'REG20240002', '27AABCT7123H2F', 'admin@globaltrade.com', '+918877665544', 'ACTIVE', '2024-02-20', NOW(), NOW(), 0),
('750e8400-e29b-41d4-a716-446655440003', 'QuickService Solutions', 'SERVICE_PROVIDER', 'REG20240003', '07AABCR1234M1Z5', 'support@quickservice.in', '+917766554433', 'ACTIVE', '2024-03-10', NOW(), NOW(), 0);

-- ==================== SAMPLE VENDOR LINKED ACCOUNTS ====================
-- Each vendor can have multiple linked accounts (1:N relationship)
INSERT INTO vendor_linked_accounts (vendor_account_id, vendor_id, account_number, account_type, account_details, status, activation_date, created_at, updated_at, version) VALUES
('850e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440001', 'IN89SBIN0010000001', 'SETTLEMENT', '{"bankCode":"SBIN0010000","ifscCode":"SBIN0010000","accountName":"TechSolutions Settlement","branchCode":"DELHI001"}', 'ACTIVE', NOW(), NOW(), NOW(), 0),
('850e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440001', 'IN89SBIN0010000002', 'ESCROW', '{"bankCode":"SBIN0010000","ifscCode":"SBIN0010000","accountName":"TechSolutions Escrow","branchCode":"DELHI001"}', 'ACTIVE', NOW(), NOW(), NOW(), 0),
('850e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440002', 'IN89HDFC0020000001', 'SETTLEMENT', '{"bankCode":"HDFC0020000","ifscCode":"HDFC0020000","accountName":"GlobalTrade Settlement","branchCode":"MUMBAI001"}', 'ACTIVE', NOW(), NOW(), NOW(), 0),
('850e8400-e29b-41d4-a716-446655440004', '750e8400-e29b-41d4-a716-446655440003', 'IN89ICIC0030000001', 'OPERATING', '{"bankCode":"ICIC0030000","ifscCode":"ICIC0030000","accountName":"QuickService Operating","branchCode":"BANGALORE001"}', 'ACTIVE', NOW(), NOW(), NOW(), 0);

-- ==================== SAMPLE PERSONAL LOANS ====================
-- Each loan belongs to one consumer (M:1 relationship)
INSERT INTO personal_loans (customer_id, principal_amount, annual_interest_rate, loan_tenure_months, monthly_emi, total_interest_payable, outstanding_balance, remaining_tenure, status, created_at, approved_at, rejected_at, closed_at, approval_remarks, rejection_reason) VALUES
-- Loan for Rajesh Kumar (consumer_id: 550e8400-e29b-41d4-a716-446655440001)
('550e8400-e29b-41d4-a716-446655440001', 500000.00, 9.50, 60, 9638.22, 78293.20, 478636.78, 58, 'ACTIVE', '2024-01-10 10:00:00', '2024-01-15 14:30:00', NULL, NULL, 'Approved - CIBIL Score: 750', NULL),

-- Loan for Priya Sharma (consumer_id: 550e8400-e29b-41d4-a716-446655440002)
('550e8400-e29b-41d4-a716-446655440002', 300000.00, 8.75, 48, 6848.56, 28890.88, 298000.00, 48, 'APPROVED', '2024-01-20 11:15:00', '2024-01-25 09:45:00', NULL, NULL, 'Approved - Salary verified', NULL),

-- Loan for Amit Patel (consumer_id: 550e8400-e29b-41d4-a716-446655440003)
('550e8400-e29b-41d4-a716-446655440003', 750000.00, 10.00, 84, 10717.45, 138906.80, 750000.00, 84, 'PENDING', '2024-02-01 09:30:00', NULL, NULL, NULL, NULL, NULL),

-- Loan for Neha Gupta (consumer_id: 550e8400-e29b-41d4-a716-446655440004)
('550e8400-e29b-41d4-a716-446655440004', 200000.00, 12.00, 36, 6282.88, 26263.68, 0.00, 0, 'CLOSED', '2023-12-15 14:20:00', '2023-12-20 11:00:00', NULL, '2026-01-15 16:45:00', 'Approved - Employee loan', NULL),

-- Loan for Vikram Singh (consumer_id: 550e8400-e29b-41d4-a716-446655440005)
('550e8400-e29b-41d4-a716-446655440005', 1000000.00, 9.25, 96, 11285.67, 83504.32, 998000.00, 95, 'ACTIVE', '2024-01-05 13:45:00', '2024-01-08 10:20:00', NULL, NULL, 'Approved - High CIBIL Score', NULL);

-- ==================== SAMPLE LOAN REPAYMENTS ====================
-- Each repayment belongs to one loan (M:1 relationship)
-- Repayments for Rajesh Kumar's loan (id: 1, 60-month tenure)
INSERT INTO loan_repayments (loan_id, installment_number, principal_amount, interest_amount, total_amount, status, due_date, paid_date, paid_amount, payment_mode, transaction_reference, created_at) VALUES
(1, 1, 8305.88, 1332.34, 9638.22, 'PAID', '2024-02-10', '2024-02-08', 9638.22, 'ONLINE', 'TXN20240208001', '2024-01-10 10:00:00'),
(1, 2, 8356.75, 1281.47, 9638.22, 'PAID', '2024-03-10', '2024-03-09', 9638.22, 'ONLINE', 'TXN20240309002', '2024-02-10 10:00:00'),
(1, 3, 8407.80, 1230.42, 9638.22, 'PAID', '2024-04-10', '2024-04-10', 9638.22, 'BANK_TRANSFER', 'TXN20240410003', '2024-03-10 10:00:00'),
(1, 4, 8459.02, 1179.20, 9638.22, 'PENDING', '2024-05-10', NULL, NULL, NULL, NULL, '2024-04-10 10:00:00'),
(1, 5, 8510.40, 1127.82, 9638.22, 'PENDING', '2024-06-10', NULL, NULL, NULL, NULL, '2024-05-10 10:00:00');

-- Repayments for Priya Sharma's loan (id: 2, 48-month tenure)
INSERT INTO loan_repayments (loan_id, installment_number, principal_amount, interest_amount, total_amount, status, due_date, paid_date, paid_amount, payment_mode, transaction_reference, created_at) VALUES
(2, 1, 6073.40, 775.16, 6848.56, 'PENDING', '2024-02-25', NULL, NULL, NULL, NULL, '2024-01-20 11:15:00'),
(2, 2, 6126.30, 722.26, 6848.56, 'PENDING', '2024-03-25', NULL, NULL, NULL, NULL, '2024-02-20 11:15:00'),
(2, 3, 6179.44, 669.12, 6848.56, 'PENDING', '2024-04-25', NULL, NULL, NULL, NULL, '2024-03-20 11:15:00');

-- Repayments for Amit Patel's loan (id: 3, 84-month tenure) - New loan, no payments yet
INSERT INTO loan_repayments (loan_id, installment_number, principal_amount, interest_amount, total_amount, status, due_date, paid_date, paid_amount, payment_mode, transaction_reference, created_at) VALUES
(3, 1, 8930.31, 1787.14, 10717.45, 'PENDING', '2024-03-05', NULL, NULL, NULL, NULL, '2024-02-01 09:30:00'),
(3, 2, 9004.06, 1713.39, 10717.45, 'PENDING', '2024-04-05', NULL, NULL, NULL, NULL, '2024-03-05 09:30:00');

-- Repayments for Neha Gupta's loan (id: 4, 36-month tenure) - Fully paid
INSERT INTO loan_repayments (loan_id, installment_number, principal_amount, interest_amount, total_amount, status, due_date, paid_date, paid_amount, payment_mode, transaction_reference, created_at) VALUES
(4, 1, 5416.88, 866.00, 6282.88, 'PAID', '2024-01-15', '2024-01-14', 6282.88, 'ONLINE', 'TXN20240114001', '2023-12-15 14:20:00'),
(4, 2, 5460.90, 821.98, 6282.88, 'PAID', '2024-02-15', '2024-02-14', 6282.88, 'ONLINE', 'TXN20240214002', '2024-01-15 14:20:00'),
(4, 3, 5505.37, 777.51, 6282.88, 'PAID', '2024-03-15', '2024-03-14', 6282.88, 'ONLINE', 'TXN20240314003', '2024-02-15 14:20:00'),
(4, 4, 5550.29, 732.59, 6282.88, 'PAID', '2024-04-15', '2024-04-14', 6282.88, 'ONLINE', 'TXN20240414004', '2024-03-15 14:20:00'),
(4, 5, 5595.65, 687.23, 6282.88, 'PAID', '2024-05-15', '2024-05-14', 6282.88, 'ONLINE', 'TXN20240514005', '2024-04-15 14:20:00');

-- Repayments for Vikram Singh's loan (id: 5, 96-month tenure)
INSERT INTO loan_repayments (loan_id, installment_number, principal_amount, interest_amount, total_amount, status, due_date, paid_date, paid_amount, payment_mode, transaction_reference, created_at) VALUES
(5, 1, 9369.36, 1916.31, 11285.67, 'PAID', '2024-02-05', '2024-02-04', 11285.67, 'ONLINE', 'TXN20240204001', '2024-01-05 13:45:00'),
(5, 2, 9434.23, 1851.44, 11285.67, 'PENDING', '2024-03-05', NULL, NULL, NULL, NULL, '2024-02-05 13:45:00');

-- ==================== SAMPLE AUDIT LOG ====================
-- Track sample operations across entities
INSERT INTO audit_log (entity_type, entity_id, action, old_values, new_values, user_id, ip_address, created_at) VALUES
('PersonalLoan', 1, 'CREATE', NULL, '{"customer_id":"550e8400-e29b-41d4-a716-446655440001","principal_amount":500000,"status":"PENDING"}', 'ADMIN001', '192.168.1.100', '2024-01-10 10:00:00'),
('PersonalLoan', 1, 'APPROVE', '{"status":"PENDING"}', '{"status":"APPROVED"}', 'LOAN_OFFICER_001', '192.168.1.101', '2024-01-15 14:30:00'),
('LoanRepayment', 1, 'PROCESS_PAYMENT', '{"status":"PENDING"}', '{"status":"PAID","paid_amount":9638.22,"paid_date":"2024-02-08"}', 'SYSTEM', '192.168.1.102', '2024-02-08 10:30:00'),
('Consumer', 1, 'CREATE', NULL, '{"name":"Rajesh Kumar","email":"rajesh.kumar@email.com","status":"ACTIVE"}', 'ADMIN001', '192.168.1.100', '2024-01-01 09:00:00'),
('PersonalLoan', 4, 'CLOSE', '{"status":"ACTIVE","outstanding_balance":500}', '{"status":"CLOSED","outstanding_balance":0}', 'SYSTEM', '192.168.1.102', '2026-01-15 16:45:00');

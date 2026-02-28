-- Flyway Migration V8: Insert Sample Data
-- Description: Insert sample data for testing
-- Deployed: 2026-02-25

-- Sample Consumer
INSERT INTO consumers (consumer_id, email, first_name, last_name, phone, date_of_birth, pan_number, status)
VALUES (
  '550e8400-e29b-41d4-a716-446655440000',
  'john.doe@example.com',
  'John',
  'Doe',
  '+1234567890',
  '1990-01-15',
  'ABCDE1234F',
  'ACTIVE'
) ON DUPLICATE KEY UPDATE email=email;

-- Sample Principal Account
INSERT INTO principal_accounts (principal_account_id, consumer_id, account_number, account_holder_name, bank_code, verification_status, linked_date, verified_date)
VALUES (
  '660e8400-e29b-41d4-a716-446655440000',
  '550e8400-e29b-41d4-a716-446655440000',
  'DE75512108001234567890',
  'John Doe',
  'DEUTDE',
  'VERIFIED',
  NOW(),
  NOW()
) ON DUPLICATE KEY UPDATE account_number=account_number;

-- Sample Vendor
INSERT INTO vendors (vendor_id, vendor_name, business_type, registration_number, gst_number, contact_email, contact_phone, status, registration_date)
VALUES (
  '770e8400-e29b-41d4-a716-446655440000',
  'ABC Electronics',
  'RETAIL',
  'REG123456',
  'GST123456789',
  'vendor@example.com',
  '+1234567890',
  'ACTIVE',
  NOW()
) ON DUPLICATE KEY UPDATE vendor_name=vendor_name;

-- Sample Personal Loan
INSERT INTO personal_loans (loan_id, consumer_id, principal_amount, interest_rate, tenure_months, status)
VALUES (
  '880e8400-e29b-41d4-a716-446655440000',
  '550e8400-e29b-41d4-a716-446655440000',
  100000.00,
  8.50,
  12,
  'ACTIVE'
) ON DUPLICATE KEY UPDATE principal_amount=principal_amount;

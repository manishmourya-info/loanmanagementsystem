# Sample Data Documentation - Loan Management System

## Overview
This document describes the sample/seed data provided for the Loan Management System. The sample data includes realistic test data for all entities with proper foreign key relationships maintained.

## Data Files
- **SQL Migration**: `src/main/resources/db/migration/V8__insert_sample_data.sql` - Flyway migration script
- **JSON Reference**: `SAMPLE_DATA.json` - JSON format for reference and API testing
- **This Document**: Complete relationship and query documentation

---

## Entity Relationships Diagram

```
CONSUMERS (1) ------ (1) PRINCIPAL_ACCOUNTS
    |
    +---- (1:N) PERSONAL_LOANS
               |
               +---- (1:N) LOAN_REPAYMENTS

VENDORS (1) ------ (1:N) VENDOR_LINKED_ACCOUNTS
```

---

## Detailed Entity Descriptions

### 1. CONSUMERS (5 Sample Records)

| Field | Example | Details |
|-------|---------|---------|
| consumer_id | UUID | Unique identifier |
| name | Rajesh Kumar | Full name (2-100 chars) |
| email | rajesh.kumar@email.com | Unique email |
| phone | +919876543210 | E.164 format |
| identity_type | AADHAR | AADHAR, PAN, DL, PASSPORT |
| identity_number | 123456789012 | Document number |
| status | ACTIVE | ACTIVE, INACTIVE, SUSPENDED, CLOSED |
| kyc_status | VERIFIED | PENDING, VERIFIED, REJECTED |

**Sample Records**:
1. **Rajesh Kumar** - AADHAR verified, has active loan
2. **Priya Sharma** - PAN verified, has approved loan
3. **Amit Patel** - AADHAR pending KYC, has pending loan
4. **Neha Gupta** - DL verified, has closed loan (fully repaid)
5. **Vikram Singh** - PASSPORT verified, has large active loan

---

### 2. PRINCIPAL_ACCOUNTS (1:1 with CONSUMERS)

Each consumer has exactly one principal account. Account is verified after linking.

| Field | Example | Details |
|-------|---------|---------|
| principal_account_id | UUID | Unique identifier |
| consumer_id | UUID | FK to consumers |
| account_number | IN89SBIN0001234567 | IBAN-compliant |
| account_holder_name | Rajesh Kumar | Must match consumer |
| bank_code | SBIN0001234 | Bank identifier |
| verification_status | VERIFIED | PENDING, VERIFIED, REJECTED |

**Sample Records**:
- All 5 consumers have linked accounts
- 4 accounts VERIFIED, 1 PENDING (Amit Patel)
- Different banks: SBI, HDFC, ICICI, Axis, IDBI

---

### 3. VENDORS (3 Sample Records)

Vendors are business partners with settlement accounts.

| Field | Example | Details |
|-------|---------|---------|
| vendor_id | UUID | Unique identifier |
| vendor_name | TechSolutions India Pvt Ltd | 2-100 chars |
| business_type | MERCHANT | MERCHANT, DISTRIBUTOR, SERVICE_PROVIDER |
| registration_number | REG20240001 | Unique reg number |
| gst_number | 18AABCS5055K1ZO | 15-char GST (optional) |
| contact_email | contact@techsolutions.com | Unique email |
| contact_phone | +919988776655 | E.164 format |
| status | ACTIVE | ACTIVE, INACTIVE, SUSPENDED |

**Sample Records**:
1. **TechSolutions** - Tech merchant with 2 accounts
2. **GlobalTrade** - Distributor with 1 account
3. **QuickService** - Service provider with 1 account

---

### 4. VENDOR_LINKED_ACCOUNTS (1:N with VENDORS)

Each vendor can have up to 5 linked accounts for different purposes.

| Field | Example | Details |
|-------|---------|---------|
| vendor_account_id | UUID | Unique identifier |
| vendor_id | UUID | FK to vendors |
| account_number | IN89SBIN0010000001 | IBAN-compliant, unique |
| account_type | SETTLEMENT | SETTLEMENT, ESCROW, OPERATING |
| account_details | JSON | Bank details metadata |
| status | ACTIVE | PENDING, ACTIVE, INACTIVE, DISABLED |

**Sample Records**:
- **TechSolutions**: 2 accounts (SETTLEMENT + ESCROW)
- **GlobalTrade**: 1 account (SETTLEMENT)
- **QuickService**: 1 account (OPERATING)

---

### 5. PERSONAL_LOANS (1:N with CONSUMERS)

Each consumer can have multiple loans. Tracks loan lifecycle.

| Field | Example | Details |
|-------|---------|---------|
| id | 1 | Auto-increment identifier |
| customer_id | UUID | FK to consumers |
| principal_amount | 500000 | Loan amount in currency |
| annual_interest_rate | 9.50 | Interest rate as percentage |
| loan_tenure_months | 60 | Duration in months |
| monthly_emi | 9638.22 | Calculated EMI |
| status | ACTIVE | PENDING, APPROVED, ACTIVE, CLOSED, REJECTED |
| outstanding_balance | 478636.78 | Remaining amount |
| remaining_tenure | 58 | Months left |

**Sample Records**:
1. **Loan 1** - Rajesh: ₹500K @ 9.5% for 60 months | **ACTIVE** (58 months remaining)
2. **Loan 2** - Priya: ₹300K @ 8.75% for 48 months | **APPROVED** (not yet active)
3. **Loan 3** - Amit: ₹750K @ 10% for 84 months | **PENDING** (awaiting approval)
4. **Loan 4** - Neha: ₹200K @ 12% for 36 months | **CLOSED** (fully repaid)
5. **Loan 5** - Vikram: ₹1M @ 9.25% for 96 months | **ACTIVE** (95 months remaining)

**Loan Status Lifecycle**:
```
PENDING → APPROVED → ACTIVE → CLOSED
                  ↓
                REJECTED
```

---

### 6. LOAN_REPAYMENTS (1:N with PERSONAL_LOANS)

EMI schedule and payment history for each loan.

| Field | Example | Details |
|-------|---------|---------|
| id | 1 | Auto-increment identifier |
| loan_id | 1 | FK to personal_loans |
| installment_number | 1 | EMI sequence (1, 2, 3...) |
| principal_amount | 8305.88 | Principal portion of EMI |
| interest_amount | 1332.34 | Interest portion of EMI |
| total_amount | 9638.22 | Total EMI |
| status | PAID | PENDING, PAID, PARTIALLY_PAID, OVERDUE, WAIVED |
| due_date | 2024-02-10 | EMI due date |
| paid_date | 2024-02-08 | Actual payment date |
| paid_amount | 9638.22 | Actual amount paid |
| payment_mode | ONLINE | ONLINE, CHEQUE, CASH, BANK_TRANSFER |
| transaction_reference | TXN20240208001 | Payment receipt/ref ID |

**Sample Records by Loan**:
- **Loan 1 (Rajesh)**: 5 EMI records (3 PAID, 2 PENDING)
- **Loan 2 (Priya)**: 3 EMI records (all PENDING)
- **Loan 3 (Amit)**: 2 EMI records (all PENDING)
- **Loan 4 (Neha)**: 5 EMI records (all PAID - fully repaid)
- **Loan 5 (Vikram)**: 2 EMI records (1 PAID, 1 PENDING)

**Total**: 17 repayment records

---

### 7. AUDIT_LOG (Tracking Sample Operations)

Comprehensive audit trail for compliance and debugging.

| Field | Example | Details |
|-------|---------|---------|
| id | 1 | Auto-increment identifier |
| entity_type | PersonalLoan | Entity being tracked |
| entity_id | 1 | ID of the entity |
| action | CREATE | CREATE, UPDATE, DELETE, APPROVE, REJECT, PROCESS_PAYMENT |
| old_values | JSON | Previous values (null for CREATE) |
| new_values | JSON | New values after change |
| user_id | LOAN_OFFICER_001 | User who made change |
| ip_address | 192.168.1.101 | Source IP |
| created_at | 2024-01-15 14:30:00 | Audit timestamp |

**Sample Records**:
- Loan creation, approval, and payment processing
- Consumer creation
- Loan closure

---

## Key Relationships & Constraints

### 1. Consumer → Principal Account (1:1)
```sql
CONSTRAINT uk_consumer_principal_account UNIQUE KEY (consumer_id)
CONSTRAINT fk_principal_account_consumer FOREIGN KEY (consumer_id)
  REFERENCES consumers(consumer_id) ON DELETE CASCADE
```
- Each consumer has exactly ONE principal account
- Deleting consumer cascades to principal account

### 2. Consumer → Personal Loan (1:N)
```sql
CONSTRAINT fk_personal_loan_consumer FOREIGN KEY (customer_id)
  REFERENCES consumers(consumer_id) ON DELETE CASCADE
```
- Consumer can have multiple loans
- Deleting consumer cascades to all loans

### 3. Personal Loan → Loan Repayment (1:N)
```sql
CONSTRAINT fk_loan_repayment FOREIGN KEY (loan_id)
  REFERENCES personal_loans(id) ON DELETE RESTRICT
```
- Each loan has multiple repayment records
- Deleting loan is RESTRICTED if repayments exist (audit protection)

### 4. Vendor → Vendor Linked Account (1:N)
```sql
CONSTRAINT fk_vendor_linked_account FOREIGN KEY (vendor_id)
  REFERENCES vendors(vendor_id) ON DELETE CASCADE
```
- Vendor can have multiple accounts (max 5)
- Deleting vendor cascades to all accounts

---

## Sample Data Highlights

### Data Consistency
✓ All foreign keys are valid  
✓ All relationships properly maintained  
✓ All constraints satisfied  
✓ Realistic financial data (EMI calculations accurate)  
✓ Date sequences correct (created_at before approved_at, etc.)  

### Test Scenarios Covered
1. **Consumer with Active Loan** (Rajesh Kumar)
2. **Consumer with Approved but Inactive Loan** (Priya Sharma)
3. **Consumer with Pending Loan** (Amit Patel)
4. **Consumer with Fully Repaid Closed Loan** (Neha Gupta)
5. **Consumer with Large Active Loan** (Vikram Singh)

### Payment History Examples
- **Fully Paid**: Neha Gupta's 36-month loan (all 5 EMIs paid)
- **Partially Paid**: Rajesh Kumar's 60-month loan (3 of 5 EMIs paid)
- **Not Started**: Priya Sharma's 48-month loan (0 of 48 EMIs paid)

---

## Useful Queries

### Consumer Information
```sql
-- Get all consumers with their principal accounts
SELECT c.consumer_id, c.name, c.email, pa.account_number, pa.verification_status
FROM consumers c
LEFT JOIN principal_accounts pa ON c.consumer_id = pa.consumer_id;

-- Get consumer's KYC status
SELECT consumer_id, name, kyc_status, status FROM consumers WHERE kyc_status = 'VERIFIED';
```

### Loan Information
```sql
-- Get all active loans
SELECT * FROM personal_loans WHERE status = 'ACTIVE';

-- Get loan details with consumer name
SELECT l.id, c.name, l.principal_amount, l.monthly_emi, l.outstanding_balance, l.status
FROM personal_loans l
JOIN consumers c ON l.customer_id = c.consumer_id;

-- Get loans by status
SELECT status, COUNT(*) as count FROM personal_loans GROUP BY status;
```

### Repayment Information
```sql
-- Get payment history for a loan
SELECT * FROM loan_repayments WHERE loan_id = 1 ORDER BY installment_number;

-- Get pending EMI payments
SELECT lr.*, l.monthly_emi, c.name
FROM loan_repayments lr
JOIN personal_loans l ON lr.loan_id = l.id
JOIN consumers c ON l.customer_id = c.consumer_id
WHERE lr.status = 'PENDING';

-- Get total paid vs due
SELECT 
  COUNT(*) as total_emis,
  SUM(CASE WHEN status = 'PAID' THEN 1 ELSE 0 END) as paid_count,
  SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending_count
FROM loan_repayments WHERE loan_id = 1;
```

### Vendor Information
```sql
-- Get all vendors with their accounts
SELECT v.vendor_id, v.vendor_name, COUNT(vla.vendor_account_id) as account_count
FROM vendors v
LEFT JOIN vendor_linked_accounts vla ON v.vendor_id = vla.vendor_id
GROUP BY v.vendor_id;

-- Get active vendor accounts
SELECT v.vendor_name, vla.account_type, vla.status
FROM vendors v
JOIN vendor_linked_accounts vla ON v.vendor_id = vla.vendor_id
WHERE vla.status = 'ACTIVE';
```

### Audit Trail
```sql
-- Get all actions on a specific loan
SELECT * FROM audit_log WHERE entity_type = 'PersonalLoan' AND entity_id = 1;

-- Get user activity
SELECT user_id, action, COUNT(*) as count FROM audit_log GROUP BY user_id, action;
```

---

## Integration with APIs

The sample data can be tested with the following API endpoints:

### Consumer APIs
```
GET    /api/v1/consumers              - List all consumers
GET    /api/v1/consumers/{consumerId} - Get specific consumer
POST   /api/v1/consumers              - Create new consumer
PUT    /api/v1/consumers/{consumerId} - Update consumer
```

### Principal Account APIs
```
GET    /api/v1/consumers/{consumerId}/principal-account           - Get account
POST   /api/v1/consumers/{consumerId}/principal-account           - Link account
PUT    /api/v1/consumers/{consumerId}/principal-account           - Update account
PUT    /api/v1/consumers/{consumerId}/principal-account/verify/{id} - Verify account
```

### Loan APIs
```
GET    /api/v1/loans                           - List all loans
GET    /api/v1/loans/{loanId}                  - Get specific loan
POST   /api/v1/loans                           - Create new loan
GET    /api/v1/loans/customer/{customerId}     - Get customer loans
PUT    /api/v1/loans/{loanId}/approve          - Approve loan
PUT    /api/v1/loans/{loanId}/disburse         - Disburse loan
PUT    /api/v1/loans/{loanId}/close            - Close loan
```

### Repayment APIs
```
GET    /api/v1/repayments/{loanId}/installment/{number}  - Get EMI details
POST   /api/v1/repayments/{loanId}/installment/{number}/pay - Pay EMI
GET    /api/v1/repayments/{loanId}                        - Get all EMIs
```

### Vendor APIs
```
GET    /api/v1/vendors                                    - List vendors
POST   /api/v1/vendors/register                           - Register vendor
GET    /api/v1/vendors/{vendorId}/linked-accounts         - Get accounts
POST   /api/v1/vendors/{vendorId}/linked-accounts         - Add account
PUT    /api/v1/vendors/linked-accounts/{accountId}/activate - Activate
```

---

## Notes

- **UUID Format**: All consumer, vendor, and account IDs use UUID v4 format
- **Currency**: All amounts in default currency (e.g., INR)
- **Dates**: Use MySQL TIMESTAMP format with automatic NOW()
- **Decimal Precision**: Financial values use DECIMAL(15,2) for accuracy
- **Timezone**: All timestamps are server timezone
- **Optimistic Locking**: All entities have version column for concurrent updates

---

## Migration Execution Order

```
1. V1__init_personal_loans.sql            - Core loan table
2. V2__init_loan_repayments.sql           - EMI schedule
3. V3__create_audit_log.sql               - Audit trail
4. V4__create_consumers.sql               - Consumer master
5. V5__create_principal_accounts.sql      - Bank accounts
6. V6__create_vendors.sql                 - Vendor master
7. V7__create_vendor_linked_accounts.sql  - Vendor accounts
8. V8__insert_sample_data.sql             - Sample/seed data ← AUTO-INSERTED
```

Flyway will execute all migrations in order on application startup.

---

## Last Updated
- Date: 2026-02-26
- Version: 1.0
- Status: Ready for Development & Testing

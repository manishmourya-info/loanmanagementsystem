# Data Model: Consumer Finance Multi-API Platform

**Feature**: 001-finance-apis  
**Date**: February 25, 2026  
**Status**: Phase 1 Design

---

## Entity Relationship Diagram

```
┌─────────────────────┐         ┌──────────────────────┐
│      Consumer       │         │   Principal Account  │
├─────────────────────┤         ├──────────────────────┤
│ consumerId (PK)     │────1:1──│ principalAccountId   │
│ name                │         │ consumerId (FK)      │
│ email               │         │ accountNumber        │
│ phone               │         │ accountHolderName    │
│ identityType        │         │ bankCode             │
│ identityNumber      │         │ verificationStatus   │
│ status              │         │ linkedDate           │
│ createdAt           │         │ createdAt            │
└─────────────────────┘         └──────────────────────┘

┌─────────────────────┐
│      Consumer       │
├─────────────────────┤
│ consumerId (PK)     │────1:N────┐
│ ...                 │           │
└─────────────────────┘           │
                                  ▼
                        ┌──────────────────────┐
                        │    Personal Loan     │
                        ├──────────────────────┤
                        │ loanId (PK)          │
                        │ consumerId (FK)      │
                        │ principal            │
                        │ annualInterestRate   │
                        │ tenureMonths         │
                        │ monthlyEMI           │
                        │ status               │
                        │ version (opt.lock)   │
                        └──────────────────────┘
                                  │
                                  │ 1:N
                                  ▼
                        ┌──────────────────────┐
                        │   Loan Repayment     │
                        ├──────────────────────┤
                        │ repaymentId (PK)     │
                        │ loanId (FK)          │
                        │ emiAmount            │
                        │ paidAmount           │
                        │ status               │
                        │ paidDate             │
                        │ version (opt.lock)   │
                        └──────────────────────┘

┌─────────────────────┐         ┌──────────────────────┐
│      Vendor         │         │ Vendor Linked Account│
├─────────────────────┤         ├──────────────────────┤
│ vendorId (PK)       │────1:N──│ vendorAccountId (PK) │
│ vendorName          │         │ vendorId (FK)        │
│ businessType        │         │ accountType          │
│ contactEmail        │         │ accountDetails       │
│ contactPhone        │         │ status               │
│ registrationDate    │         │ activationDate       │
│ status              │         │ createdAt            │
└─────────────────────┘         └──────────────────────┘

┌──────────────────────┐
│     Audit Log        │
├──────────────────────┤
│ auditId (PK,seq)     │
│ action               │
│ loanId (FK, nullable)│
│ userId               │
│ amount               │
│ details (JSON)       │
│ status               │
│ timestamp            │
└──────────────────────┘
```

---

## Entity Specifications

### 1. Consumer Entity

Represents individual customers in the loan platform. Contains personal identification, KYC (Know Your Customer) information, and account status.

#### Field Definitions

| Field | Type | Constraints | Description | Validation |
|-------|------|-----------|------------|-----------|
| **consumerId** | UUID | PK, NOT NULL | Unique consumer identifier | Generated on creation |
| **name** | VARCHAR(100) | NOT NULL | Full name of consumer | Required, 2-100 chars, alphanumeric + spaces |
| **email** | VARCHAR(100) | UNIQUE, NOT NULL | Email address | Valid email format, unique |
| **phone** | VARCHAR(15) | NOT NULL | Phone number | International format, 10-15 digits |
| **identityType** | ENUM | NOT NULL | Type of identity proof | Values: PASSPORT, DRIVING_LICENSE, AADHAR, PAN |
| **identityNumber** | VARCHAR(50) | UNIQUE, NOT NULL | Identity document number | Format specific to identityType |
| **kycStatus** | ENUM | NOT NULL | KYC verification status | PENDING, VERIFIED, REJECTED |
| **status** | ENUM | NOT NULL, DEFAULT='ACTIVE' | Consumer account status | ACTIVE, INACTIVE, SUSPENDED, CLOSED |
| **createdAt** | DATETIME(6) | NOT NULL, AUTO | Record creation timestamp | Generated automatically |
| **updatedAt** | DATETIME(6) | NOT NULL, AUTO_UPDATE | Last modification timestamp | Updated automatically |
| **version** | BIGINT | DEFAULT=0 | Optimistic lock version | Incremented on each update |

#### Indexes
- `idx_email` (UNIQUE)
- `idx_phone`
- `idx_identity_number` (UNIQUE)
- `idx_status`
- `idx_created_at`

#### Relationships
- **One-to-Many**: Consumer → PersonalLoans (one consumer has many loans)
- **One-to-One**: Consumer → PrincipalAccount (each consumer has one primary account)

#### Business Validations
```
✓ Email must be unique and valid format (RFC 5322)
✓ Phone must follow international format (E.164)
✓ Identity number must match type (e.g., AADHAR = 12 digits)
✓ KYC status cannot be VERIFIED unless all identity fields present
✓ Cannot create more than 5 inactive consumers from same phone/email
✓ Identity number must be encrypted at rest
```

---

### 2. Principal Account Entity

Represents the consumer's primary banking account used for loan disbursement and repayment collection. Only one principal account per consumer.

#### Field Definitions

| Field | Type | Constraints | Description | Validation |
|-------|------|-----------|------------|-----------|
| **principalAccountId** | UUID | PK, NOT NULL | Unique account identifier | Generated on creation |
| **consumerId** | UUID | FK, NOT NULL, UNIQUE | Foreign key to Consumer | Must exist in Consumer table |
| **accountNumber** | VARCHAR(34) | NOT NULL | Bank account number | IBAN or national format |
| **accountHolderName** | VARCHAR(100) | NOT NULL | Name on account | Must match consumer name (fuzzy match 80%+) |
| **bankCode** | VARCHAR(20) | NOT NULL | Bank identifier code | ISO 4217 or national code |
| **bankName** | VARCHAR(100) | NOT NULL | Full bank name | For audit trail |
| **accountType** | ENUM | NOT NULL | Account classification | SAVINGS, CHECKING, CURRENT |
| **verificationStatus** | ENUM | NOT NULL, DEFAULT='PENDING' | Verification state | PENDING, VERIFIED, FAILED, REJECTED |
| **linkedDate** | DATETIME(6) | | Date account was linked | Populated on verification |
| **createdAt** | DATETIME(6) | NOT NULL, AUTO | Record creation timestamp | Generated automatically |
| **updatedAt** | DATETIME(6) | NOT NULL, AUTO_UPDATE | Last modification timestamp | Updated automatically |
| **version** | BIGINT | DEFAULT=0 | Optimistic lock version | Incremented on each update |

#### Indexes
- `idx_account_number` (UNIQUE)
- `idx_consumer_id` (UNIQUE)
- `idx_verification_status`

#### Relationships
- **Many-to-One**: PrincipalAccount → Consumer
- **Referenced by**: PersonalLoan (for disbursal information)

#### Business Validations
```
✓ Only one principal account per consumer (UNIQUE on consumerId)
✓ Account holder name must match consumer name within 80% fuzzy match
✓ Account number must be valid for specified bank code
✓ Verification status must progress: PENDING → VERIFIED or FAILED
✓ Linked date only populated when verification succeeds
✓ Account number must be encrypted at rest (sensitive data)
✓ Cannot be marked VERIFIED without third-party bank verification
```

#### State Transitions
```
PENDING → VERIFIED → (final state, only updated on account change)
       ↘ FAILED   → PENDING (can be retried)
       ↘ REJECTED → (terminal, requires manual intervention)
```

---

### 3. Personal Loan Entity

Core entity representing loan contracts. Tracks loan details, status, and financial calculations.

#### Field Definitions

| Field | Type | Constraints | Description | Validation |
|-------|------|-----------|------------|-----------|
| **loanId** | UUID | PK, NOT NULL | Unique loan identifier | Generated on creation |
| **consumerId** | UUID | FK, NOT NULL | Foreign key to Consumer | Must exist; consumer must have linked account |
| **principal** | DECIMAL(19,2) | NOT NULL | Loan amount in base currency | Range: 10,000.00 - 50,000,000.00 |
| **annualInterestRate** | DECIMAL(5,2) | NOT NULL | Interest rate (%) | Range: 0.01 - 36.00 (percent per annum) |
| **tenureMonths** | INT | NOT NULL | Loan tenure | Range: 12 - 360 months |
| **monthlyEMI** | DECIMAL(19,2) | NOT NULL | Calculated EMI amount | Computed via: EMI = P×r×(1+r)^n / ((1+r)^n - 1) |
| **disbursedAmount** | DECIMAL(19,2) | NOT NULL, DEFAULT=0.00 | Amount disbursed | Cannot exceed principal |
| **status** | ENUM | NOT NULL, DEFAULT='PENDING' | Loan lifecycle status | PENDING, APPROVED, REJECTED, ACTIVE, CLOSED, DEFAULTED |
| **approvalDate** | DATETIME(6) | | Date loan was approved | Populated when approved |
| **disbursementDate** | DATETIME(6) | | Date funds disbursed | Populated when disbursal complete |
| **maturityDate** | DATETIME(6) | | Loan maturity/closure date | Calculated: disbursementDate + tenureMonths |
| **createdAt** | DATETIME(6) | NOT NULL, AUTO | Record creation timestamp | Generated automatically |
| **updatedAt** | DATETIME(6) | NOT NULL, AUTO_UPDATE | Last modification timestamp | Updated automatically |
| **version** | BIGINT | DEFAULT=0 | Optimistic lock version | For concurrent payment processing |

#### Indexes
- `idx_consumer_id`
- `idx_status`
- `idx_created_at`
- `idx_disbursement_date`
- `composite_idx (consumer_id, status, created_at)`

#### Relationships
- **Many-to-One**: PersonalLoan → Consumer
- **One-to-Many**: PersonalLoan → LoanRepayment (one loan has many repayments)
- **Referenced by**: AuditLog

#### Business Validations
```
✓ Consumer must have VERIFIED principal account before loan approval
✓ Principal must be within configured range (e.g., 10k - 50M)
✓ Annual interest rate 0.01% - 36.00%
✓ Tenure 12 - 360 months (1 - 30 years)
✓ Monthly EMI calculated with BigDecimal, HALF_EVEN rounding, 2 decimals
✓ EMI = P × r × (1+r)^n / ((1+r)^n - 1) where r = annual_rate/12/100
✓ Status transitions: PENDING → APPROVED/REJECTED → ACTIVE → CLOSED
✓ Cannot reject or approve loan twice
✓ Cannot disburse more than principal amount
✓ Disbursement only allowed when status = APPROVED
✓ Maturity date = disbursement date + tenure months
✓ Consumer can have max 5 ACTIVE loans simultaneously (configurable)
```

#### State Machine
```
PENDING ─[approved]→ APPROVED ─[disburse]→ ACTIVE ─[fully_repaid]→ CLOSED
     ↓
   REJECTED (terminal)
     
ACTIVE ─[default]→ DEFAULTED (terminal)
```

---

### 4. Loan Repayment Entity

Tracks individual EMI payments and repayment schedules. One repayment record per month/payment cycle.

#### Field Definitions

| Field | Type | Constraints | Description | Validation |
|-------|------|-----------|------------|-----------|
| **repaymentId** | UUID | PK, NOT NULL | Unique repayment identifier | Generated on creation |
| **loanId** | UUID | FK, NOT NULL | Foreign key to PersonalLoan | Must exist |
| **installmentNumber** | INT | NOT NULL | EMI installment sequence | 1 to tenure_months |
| **dueDate** | DATE | NOT NULL | Payment due date | Calculated from disbursement + months |
| **emiAmount** | DECIMAL(19,2) | NOT NULL | EMI due amount | From loan record |
| **paidAmount** | DECIMAL(19,2) | DEFAULT=0.00 | Amount actually paid | Null or >= 0 |
| **status** | ENUM | NOT NULL, DEFAULT='PENDING' | Repayment status | PENDING, PAID, DEFAULTED, WAIVED, ADJUSTED |
| **paidDate** | DATETIME(6) | | Date payment received | Populated when paid |
| **transactionId** | VARCHAR(50) | UNIQUE | Payment reference | External payment gateway ref |
| **paymentMethod** | ENUM | | How payment was made | ONLINE, CHEQUE, TRANSFER, CASH |
| **notes** | TEXT | | Additional repayment notes | For admin use |
| **createdAt** | DATETIME(6) | NOT NULL, AUTO | Record creation timestamp | Generated automatically |
| **updatedAt** | DATETIME(6) | NOT NULL, AUTO_UPDATE | Last modification timestamp | Updated automatically |
| **version** | BIGINT | DEFAULT=0 | Optimistic lock version | For concurrent payment processing |

#### Indexes
- `idx_loan_id`
- `idx_due_date`
- `idx_status`
- `idx_transaction_id` (UNIQUE)
- `composite_idx (loan_id, status, due_date)`

#### Relationships
- **Many-to-One**: LoanRepayment → PersonalLoan
- **Referenced by**: AuditLog

#### Business Validations
```
✓ Loan must exist and be in ACTIVE status
✓ Due date must be >= loan disbursement date
✓ EMI amount matches loan's monthly EMI
✓ Paid amount cannot exceed emi_amount (unless special adjustment)
✓ Status transitions: PENDING → PAID/DEFAULTED/WAIVED
✓ Paid date only populated when status = PAID
✓ Transaction ID must be unique (prevents duplicate payments)
✓ Paid date cannot be in the future
✓ Cannot mark as PAID without amount >= EMI amount
✓ Payment creates AuditLog record for compliance
✓ All payments within same transaction must succeed or all fail (atomicity)
```

#### Payment Processing Rules
```
Rule 1: Payment Eligibility
- Only PENDING repayments can be paid
- Loan must be ACTIVE
- Payment date cannot be > 60 days after due date (becomes defaulted)

Rule 2: Partial Payment Handling
- If paid_amount < emi_amount: remains PENDING (partial payment accepted per config)
- If paid_amount >= emi_amount: status = PAID

Rule 3: EMI Schedule Update
- When repayment PAID: automatically create next month's repayment (if not exists)
- When all repayments PAID: parent loan status → CLOSED

Rule 4: Concurrent Payment Protection
- Use @Version (optimistic lock) for high-concurrency scenarios
- Use PESSIMISTIC_WRITE lock for payment processing (critical section)
```

---

### 5. Vendor Entity

Represents merchants, partners, and service providers in the ecosystem.

#### Field Definitions

| Field | Type | Constraints | Description | Validation |
|-------|------|-----------|------------|-----------|
| **vendorId** | UUID | PK, NOT NULL | Unique vendor identifier | Generated on creation |
| **vendorName** | VARCHAR(150) | NOT NULL | Legal vendor name | 3-150 chars |
| **businessType** | VARCHAR(50) | NOT NULL | Type of business | MERCHANT, PROCESSOR, PARTNER, SERVICE_PROVIDER |
| **registrationNumber** | VARCHAR(50) | UNIQUE | Business registration ID | Country-specific format |
| **gstNumber** | VARCHAR(15) | UNIQUE | GST/Tax identification | Format per country |
| **contactEmail** | VARCHAR(100) | NOT NULL | Primary contact email | Valid email format |
| **contactPhone** | VARCHAR(15) | NOT NULL | Primary contact phone | International format |
| **registrationDate** | DATETIME(6) | NOT NULL, AUTO | Date vendor registered | Generated on creation |
| **status** | ENUM | NOT NULL, DEFAULT='ACTIVE' | Vendor status | ACTIVE, INACTIVE, SUSPENDED, BLACKLISTED |
| **createdAt** | DATETIME(6) | NOT NULL, AUTO | Record creation timestamp | Generated automatically |
| **updatedAt** | DATETIME(6) | NOT NULL, AUTO_UPDATE | Last modification timestamp | Updated automatically |

#### Indexes
- `idx_vendor_name`
- `idx_registration_number` (UNIQUE)
- `idx_gst_number` (UNIQUE)
- `idx_status`

#### Relationships
- **One-to-Many**: Vendor → VendorLinkedAccount

#### Business Validations
```
✓ Vendor name unique per business type
✓ Registration number must be unique and valid format
✓ Email must be unique and valid
✓ Status can only be ACTIVE if all required documents verified
✓ Vendors marked BLACKLISTED cannot have new transactions
✓ Cannot delete vendor; mark as INACTIVE instead (audit trail)
```

---

### 6. Vendor Linked Account Entity

Represents specific banking/payment accounts linked to vendors for disbursal and settlement.

#### Field Definitions

| Field | Type | Constraints | Description | Validation |
|-------|------|-----------|------------|-----------|
| **vendorAccountId** | UUID | PK, NOT NULL | Unique account identifier | Generated on creation |
| **vendorId** | UUID | FK, NOT NULL | Foreign key to Vendor | Must exist |
| **accountNumber** | VARCHAR(34) | NOT NULL | Bank account number | IBAN or national format |
| **accountHolderName** | VARCHAR(100) | NOT NULL | Name on account | Must match vendor details |
| **bankCode** | VARCHAR(20) | NOT NULL | Bank identifier | ISO code |
| **accountType** | ENUM | NOT NULL | Account classification | SAVINGS, CHECKING, CURRENT |
| **status** | ENUM | NOT NULL, DEFAULT='PENDING' | Verification status | PENDING, ACTIVE, SUSPENDED, CLOSED |
| **activationDate** | DATETIME(6) | | Date account activated | Populated when ACTIVE |
| **createdAt** | DATETIME(6) | NOT NULL, AUTO | Record creation timestamp | Generated automatically |
| **updatedAt** | DATETIME(6) | NOT NULL, AUTO_UPDATE | Last modification timestamp | Updated automatically |

#### Indexes
- `idx_vendor_id`
- `idx_account_number`
- `idx_status`

#### Relationships
- **Many-to-One**: VendorLinkedAccount → Vendor

#### Business Validations
```
✓ Vendor must exist and be ACTIVE
✓ Account number must be unique
✓ Account holder name must match vendor name (80%+ match)
✓ Status progression: PENDING → ACTIVE or SUSPENDED
✓ Activation date only populated when ACTIVE
✓ Cannot process transactions if status != ACTIVE
✓ At most 5 accounts per vendor
```

---

### 7. Audit Log Entity

Immutable audit trail for compliance and regulatory requirements.

#### Field Definitions

| Field | Type | Constraints | Description | Validation |
|-------|------|-----------|------------|-----------|
| **auditId** | BIGINT | PK, AUTO_INCREMENT | Sequential audit log ID | Generated automatically |
| **action** | VARCHAR(50) | NOT NULL | Action performed | LOAN_CREATED, PAYMENT_PROCESSED, LOAN_APPROVED, etc. |
| **loanId** | UUID | | Related loan (if applicable) | FK to PersonalLoan (nullable) |
| **userId** | VARCHAR(50) | | User who performed action | From JWT token |
| **amount** | DECIMAL(19,2) | | Transaction amount (if applicable) | For payment audits |
| **details** | JSON | | Additional audit details | Structured data (request/response) |
| **status** | VARCHAR(20) | | Action result | SUCCESS, FAILURE, PARTIAL |
| **ipAddress** | VARCHAR(45) | | Request origin IP | For security audit |
| **timestamp** | DATETIME(6) | NOT NULL, AUTO | When action occurred | Generated automatically |

#### Indexes
- `idx_loan_id`
- `idx_user_id`
- `idx_timestamp`
- `idx_action`
- `composite_idx (loan_id, action, timestamp)`

#### Characteristics
- **Immutable**: No UPDATE/DELETE; only INSERT
- **Comprehensive**: Every significant action logged
- **Compliant**: Supports SOX, GDPR, PCI-DSS audit requirements
- **Async**: Logged asynchronously to avoid blocking business operations

#### Logged Actions
```
CONSUMER_CREATED        - New consumer registration
CONSUMER_UPDATED        - Consumer profile modified
KYC_VERIFIED            - KYC approval
ACCOUNT_LINKED          - Principal account connected
ACCOUNT_VERIFIED        - Account verification completed
LOAN_CREATED            - Loan application submitted
LOAN_APPROVED           - Loan approval decision
LOAN_REJECTED           - Loan rejection decision
LOAN_DISBURSED          - Funds disbursed
PAYMENT_PROCESSED       - EMI payment received
PAYMENT_FAILED          - Payment processing failed
LOAN_CLOSED             - Loan fully repaid
LOAN_DEFAULTED          - Loan default status
VENDOR_CREATED          - Vendor registered
VENDOR_ACCOUNT_LINKED   - Vendor account connected
```

---

## Physical Schema Constraints

### Foreign Key Constraints
```sql
ALTER TABLE principal_accounts 
ADD CONSTRAINT fk_pa_consumer 
FOREIGN KEY (consumer_id) REFERENCES consumers(consumer_id) 
ON DELETE CASCADE;

ALTER TABLE personal_loans 
ADD CONSTRAINT fk_pl_consumer 
FOREIGN KEY (consumer_id) REFERENCES consumers(consumer_id) 
ON DELETE CASCADE;

ALTER TABLE loan_repayments 
ADD CONSTRAINT fk_lr_loan 
FOREIGN KEY (loan_id) REFERENCES personal_loans(loan_id) 
ON DELETE CASCADE;

ALTER TABLE vendor_linked_accounts 
ADD CONSTRAINT fk_vla_vendor 
FOREIGN KEY (vendor_id) REFERENCES vendors(vendor_id) 
ON DELETE CASCADE;
```

### Unique Constraints
```sql
UNIQUE(email) on consumers
UNIQUE(phone) on consumers
UNIQUE(identity_number) on consumers
UNIQUE(consumer_id) on principal_accounts
UNIQUE(account_number) on principal_accounts
UNIQUE(vendor_name, business_type) on vendors
UNIQUE(registration_number) on vendors
UNIQUE(gst_number) on vendors
UNIQUE(transaction_id) on loan_repayments
```

### Check Constraints
```sql
CHECK (principal >= 10000.00 AND principal <= 50000000.00) on personal_loans
CHECK (annual_interest_rate >= 0.01 AND annual_interest_rate <= 36.00) on personal_loans
CHECK (tenure_months >= 12 AND tenure_months <= 360) on personal_loans
CHECK (monthly_emi > 0) on personal_loans
CHECK (disbursed_amount <= principal) on personal_loans
CHECK (paid_amount <= emi_amount OR paid_amount IS NULL) on loan_repayments
```

---

## Data Type Specifications

### Financial Fields
- All monetary amounts: `DECIMAL(19, 2)` 
  - Range: ±9,223,372,036.85 (supports global currencies)
  - Scale: 2 decimal places (cents/paise)
  - Rationale: Exact arithmetic; no floating-point errors

- Interest rates: `DECIMAL(5, 2)`
  - Range: ±999.99%
  - Scale: 2 decimal places (0.01% precision)
  - Rationale: Industry standard for interest rate precision

### Temporal Fields
- All timestamps: `DATETIME(6)` 
  - Precision: 6 decimal places (microsecond resolution)
  - Rationale: High-precision audit trail for rapid payments

- Dates (no time): `DATE`
  - Rationale: Payment due dates, etc. (no time component needed)

### Identifiers
- Primary IDs: `CHAR(36)` UUID or `BIGINT AUTO_INCREMENT`
- Foreign Keys: Match parent type
- Business identifiers: `VARCHAR(50-100)` (account numbers, etc.)

---

## Validation Rules Summary

| Entity | Critical Validations | Audit Level |
|--------|----------------------|-------------|
| **Consumer** | KYC status, identity uniqueness, email format | HIGH |
| **PrincipalAccount** | Account ownership verification, account number format | HIGH |
| **PersonalLoan** | Principal range, interest rate bounds, tenure limits, EMI calculation | CRITICAL |
| **LoanRepayment** | Payment amount validation, loan active status, due date rules | CRITICAL |
| **Vendor** | Registration verification, tax ID validity | MEDIUM |
| **VendorLinkedAccount** | Bank account verification, vendor status | MEDIUM |
| **AuditLog** | Immutability, timestamp accuracy, user identification | CRITICAL |

---

## Migration Path

Entities are created via Flyway migrations in this sequence:

1. **V1__create_personal_loans_table.sql** - Core loan infrastructure
2. **V2__create_loan_repayments_table.sql** - Payment tracking
3. **V3__create_audit_log_table.sql** - Compliance audit trail
4. **V4__create_consumers_table.sql** - Customer management
5. **V5__create_principal_accounts_table.sql** - Account linking
6. **V6__create_vendors_table.sql** - Merchant onboarding
7. **V7__create_vendor_linked_accounts_table.sql** - Vendor account management

**Note**: Order ensures foreign key dependencies resolve correctly.

---

**Status**: ✅ Ready for API Contract Definition (Phase 1 Design)

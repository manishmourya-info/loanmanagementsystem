# Data Model: Consumer Finance Loan Management System

**Feature**: Consumer Finance - Loan Management System  
**Phase**: 1 (Design & Data Modeling)  
**Created**: 2026-02-25  
**Status**: Complete (Ready for Implementation)

---

## Entity Relationship Diagram

```
┌─────────────────────────┐
│   PersonalLoan          │
├─────────────────────────┤
│ id (PK)                 │
│ customerId              │
│ principalAmount         │────┐
│ annualInterestRate      │    │ One-to-Many
│ loanTenureMonths        │    │
│ monthlyEMI              │    │
│ totalInterestPayable    │    │
│ outstandingBalance      │    │
│ remainingTenure         │    │
│ status                  │    │
│ createdAt               │    │
│ approvedAt              │    │
│ closedAt                │    │
└─────────────────────────┘    │
                              │
                              │
┌─────────────────────────┐    │
│   LoanRepayment         │◄───┘
├─────────────────────────┤
│ id (PK)                 │
│ loanId (FK)             │
│ installmentNumber       │
│ principalAmount         │
│ interestAmount          │
│ totalAmount             │
│ status                  │
│ dueDate                 │
│ paidDate                │
│ paidAmount              │
│ createdAt               │
└─────────────────────────┘
```

---

## Core Entities

### 1. PersonalLoan Entity

**Purpose**: Represents a personal loan issued to a customer with complete lifecycle tracking.

**JPA Mapping**:
```java
@Entity
@Table(name = "personal_loans")
public class PersonalLoan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                           // Unique loan identifier
    
    @Column(nullable = false)
    private String customerId;                 // FK to Customer (denormalized for MVP)
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;        // Loan amount borrowed
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal annualInterestRate;     // Interest rate per annum (%)
    
    @Column(nullable = false)
    private Integer loanTenureMonths;          // Total loan duration (months)
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyEMI;             // Calculated EMI (fixed)
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInterestPayable;   // Total interest over loan life
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal outstandingBalance;     // Amount still owed (updates with repayments)
    
    @Column(nullable = false)
    private Integer remainingTenure;           // Months left to repay
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;                 // ACTIVE, CLOSED, SUSPENDED, DEFAULTED
    
    @Column(nullable = false)
    private LocalDateTime createdAt;           // Application timestamp
    
    @Column(nullable = false)
    private LocalDateTime approvedAt;          // Approval timestamp
    
    @Column
    private LocalDateTime closedAt;            // Closure timestamp (if closed)
    
    @OneToMany(mappedBy = "loan", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<LoanRepayment> repayments;
}
```

**Enum: LoanStatus**
```java
public enum LoanStatus {
    ACTIVE,        // Loan is active and accepting repayments
    CLOSED,        // Loan fully repaid or explicitly closed
    SUSPENDED,     // Loan suspended (default risk, review needed)
    DEFAULTED      // Loan defaulted (critical delinquency)
}
```

**Key Constraints**:
- `principalAmount > 0`: Loan amount must be positive
- `annualInterestRate` ∈ [0, 25]: Typical range for personal loans
- `loanTenureMonths` ∈ [6, 360]: 6 months to 30 years
- `monthlyEMI = principalAmount * r * (1+r)^n / ((1+r)^n - 1)` where r = monthly rate
- `outstandingBalance` initially = `principalAmount`, decreases with repayments
- `remainingTenure` initially = `loanTenureMonths`, decreases with repayments
- `status` transitions: ACTIVE → CLOSED (when balance = 0) or SUSPENDED/DEFAULTED
- `createdAt` = current timestamp (application)
- `approvedAt` = current timestamp (approval)
- `closedAt` = NULL until explicitly closed

**Validation Rules**:
| Field | Min | Max | Rule |
|-------|-----|-----|------|
| principalAmount | 1,000 | 10,000,000 | Must be positive |
| annualInterestRate | 0 | 25 | % per annum |
| loanTenureMonths | 6 | 360 | Months (0.5 to 30 years) |
| outstandingBalance | 0 | principalAmount | Monotonically decreases |
| remainingTenure | 0 | loanTenureMonths | Monotonically decreases |

---

### 2. LoanRepayment Entity

**Purpose**: Represents a single installment obligation and its payment history, enabling transaction-level tracking.

**JPA Mapping**:
```java
@Entity
@Table(name = "loan_repayments")
public class LoanRepayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                           // Unique repayment transaction ID
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private PersonalLoan loan;                 // Associated loan (required)
    
    @Column(nullable = false)
    private Integer installmentNumber;         // 1, 2, 3, ... n (sequential)
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;        // Principal portion of this installment
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestAmount;         // Interest portion of this installment
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;            // Principal + Interest (= EMI usually)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepaymentStatus status;            // PENDING, PAID, PARTIALLY_PAID, OVERDUE, WAIVED
    
    @Column(nullable = false)
    private LocalDateTime dueDate;             // Due date for this installment
    
    @Column
    private LocalDateTime paidDate;            // Actual payment date (NULL if not paid)
    
    @Column(precision = 15, scale = 2)
    private BigDecimal paidAmount;             // Amount actually paid (NULL if not paid)
    
    @Column
    private String remarks;                    // Optional notes (e.g., partial, waived reason)
    
    @Column(nullable = false)
    private LocalDateTime createdAt;           // Record creation timestamp
}
```

**Enum: RepaymentStatus**
```java
public enum RepaymentStatus {
    PENDING,           // Due but not yet paid
    PAID,              // Fully paid (paidAmount = totalAmount)
    PARTIALLY_PAID,    // Partially paid (paidAmount < totalAmount)
    OVERDUE,           // Due date passed and not paid
    WAIVED             // Forgiven (due to hardship, etc.)
}
```

**Key Constraints**:
- `loanId` (FK): Must reference existing PersonalLoan (required)
- `installmentNumber`: Sequential 1 to N (where N = loanTenureMonths)
- `principalAmount + interestAmount = totalAmount`
- `totalAmount` ≈ `monthlyEMI` (may differ for last installment)
- `paidAmount ≤ totalAmount`: Cannot overpay (or reject with error)
- `status` transitions: PENDING → (PAID, PARTIALLY_PAID, OVERDUE) → PAID
- `dueDate` = approvalDate + (installmentNumber months)
- `paidDate` = NULL until payment, then timestamp of payment
- `createdAt` = timestamp when schedule generated (at loan creation)

**Validation Rules**:
| Field | Rule |
|-------|------|
| installmentNumber | Sequential 1..N, unique per loan |
| principalAmount | > 0 |
| interestAmount | ≥ 0 |
| totalAmount | = principalAmount + interestAmount |
| paidAmount | 0 ≤ paidAmount ≤ totalAmount |
| dueDate | >= loan.approvedAt |
| paidDate | NULL or >= dueDate (allows early payment) |

---

## Database Schema (MySQL 8.0)

### Table: personal_loans

```sql
CREATE TABLE personal_loans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id VARCHAR(50) NOT NULL,
    principal_amount DECIMAL(15, 2) NOT NULL CHECK (principal_amount > 0),
    annual_interest_rate DECIMAL(5, 2) NOT NULL CHECK (annual_interest_rate >= 0 AND annual_interest_rate <= 25),
    loan_tenure_months INT NOT NULL CHECK (loan_tenure_months >= 6 AND loan_tenure_months <= 360),
    monthly_emi DECIMAL(15, 2) NOT NULL,
    total_interest_payable DECIMAL(15, 2) NOT NULL,
    outstanding_balance DECIMAL(15, 2) NOT NULL,
    remaining_tenure INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CLOSED', 'SUSPENDED', 'DEFAULTED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP NULL,
    
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Table: loan_repayments

```sql
CREATE TABLE loan_repayments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    installment_number INT NOT NULL,
    principal_amount DECIMAL(15, 2) NOT NULL,
    interest_amount DECIMAL(15, 2) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'PARTIALLY_PAID', 'OVERDUE', 'WAIVED')),
    due_date TIMESTAMP NOT NULL,
    paid_date TIMESTAMP NULL,
    paid_amount DECIMAL(15, 2) NULL,
    remarks TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (loan_id) REFERENCES personal_loans(id) ON DELETE RESTRICT,
    UNIQUE KEY unique_loan_installment (loan_id, installment_number),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date),
    INDEX idx_paid_date (paid_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## State Transitions

### Loan Status Lifecycle

```
┌─────────────┐
│  ACTIVE     │◄──── Loan application approved
├─────────────┤
│   (Accepts  │
│ repayments) │
└──────┬──────┘
       │
       ├──────► CLOSED (when outstandingBalance = 0)
       │
       ├──────► SUSPENDED (manual admin action for risk)
       │
       └──────► DEFAULTED (critical delinquency)
```

### Repayment Status Lifecycle (per installment)

```
PENDING
   │
   ├──► PAID (if paidAmount = totalAmount)
   │
   ├──► PARTIALLY_PAID (if 0 < paidAmount < totalAmount)
   │
   ├──► OVERDUE (if dueDate < today AND status = PENDING)
   │
   └──► WAIVED (admin action for hardship/forgiveness)
```

---

## Relationships & Constraints

### One-to-Many: PersonalLoan → LoanRepayment

**Definition**:
- One `PersonalLoan` has many `LoanRepayment` records
- Each `LoanRepayment` belongs to exactly one `PersonalLoan`
- When a loan is created, N `LoanRepayment` records are pre-generated (one per month)
- Cascade PERSIST: Creating a loan persists its repayments
- Cascade REMOVE: **NOT used** (deleting loan should not delete payment history)

**Referential Integrity**:
- FK constraint: `loan_repayments.loan_id` → `personal_loans.id`
- Delete rule: ON DELETE RESTRICT (cannot delete loan with repayments)
- Update rule: ON UPDATE CASCADE (if loan.id updated, cascade to repayments)

**Example**:
```sql
-- Create loan with 3 auto-generated repayments
INSERT INTO personal_loans (...) VALUES (...);  -- id = 1

-- Auto-insert during service.createLoan():
INSERT INTO loan_repayments (loan_id, installment_number, ...)
VALUES (1, 1, ...), (1, 2, ...), (1, 3, ...);
```

---

## Data Integrity Guarantees

### ACID Properties (MySQL ACID via InnoDB)

**Atomicity**: Payment processing is @Transactional
```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public void processRepayment(Long loanId, BigDecimal amount) {
    // Update repayment status + update loan balance TOGETHER
    // Either both succeed or both rollback
}
```

**Consistency**: Schema constraints enforce invariants
- `principal_amount > 0` (always valid)
- `annual_interest_rate IN [0, 25]` (range validation)
- `outstanding_balance` never > `principal_amount`
- Foreign keys ensure orphan repayments impossible

**Isolation**: Concurrent payments isolated
- Multiple customers can pay simultaneously without interference
- Isolation level: READ_COMMITTED (MySQL default for InnoDB)

**Durability**: MySQL persists to disk
- All committed transactions survive system failures
- Configured with `innodb_flush_log_at_trx_commit = 1` (production)

---

## Future Enhancements (Deferred to Phase 2)

### Customer Entity (when needed)

```java
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    private String id;                        // CUST001, CUST002, etc.
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column
    private String phone;
    
    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus;               // PENDING, APPROVED, REJECTED
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "customer")
    private List<PersonalLoan> loans;
}
```

### Audit Log Entity (for compliance)

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String entityType;                 // "PersonalLoan", "LoanRepayment"
    
    @Column(nullable = false)
    private Long entityId;                     // ID of loan or repayment
    
    @Column(nullable = false)
    private String action;                     // "CREATE", "UPDATE", "REPAYMENT"
    
    @Column(nullable = false)
    private String actor;                      // User/system that triggered action
    
    @Column(columnDefinition = "LONGTEXT")
    private String changes;                    // JSON of what changed
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
```

---

## Performance Considerations

### Indexing Strategy

| Table | Index | Purpose |
|-------|-------|---------|
| personal_loans | customer_id | Fast lookup of customer's loans |
| personal_loans | status | Quick filtering (ACTIVE loans) |
| personal_loans | created_at | Sort by creation date |
| loan_repayments | status | Find PENDING/OVERDUE installments |
| loan_repayments | due_date | Identify upcoming/overdue payments |
| loan_repayments | loan_id + installment_number | Unique constraint |

### Query Optimization

```java
// ✅ GOOD: Use derived column (outstandingBalance stored in PersonalLoan)
SELECT outstanding_balance FROM personal_loans WHERE id = 1;

// ❌ BAD: Don't calculate from repayments each time
SELECT SUM(total_amount - paid_amount) FROM loan_repayments WHERE ...;

// ✅ GOOD: Pre-generate schedule, update status on payment
SELECT * FROM loan_repayments WHERE loan_id = 1 AND status = 'PENDING';

// ❌ BAD: Don't calculate due dates dynamically
SELECT * FROM loan_repayments WHERE due_date BETWEEN ? AND ?;
```

---

**Version**: 1.0.0 | **Status**: Complete | **Last Updated**: 2026-02-25

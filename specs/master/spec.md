# Feature Specification: Consumer Finance Loan Management System

**Feature Branch**: `master`  
**Created**: 2026-02-24  
**Status**: Draft / Pending Clarification  
**Input**: User description: "Consumer Finance app with three apis - Personal loan API, Calculate EMI API, Loan repayment API, Validate user input, Log actions, Low response time"

## User Scenarios & Testing

### User Story 1 - Customer Applies for Personal Loan (Priority: P1)

A customer needs to apply for a personal loan by providing loan amount, duration, and agreeing to terms. The system should calculate and display the EMI, total interest, and total repayment amount before approval.

**Why this priority**: Personal loan creation is the core entry point; without this, no other functionality is possible. This is the primary revenue driver.

**Independent Test**: Can be fully tested by creating a loan application, verifying EMI calculation is displayed, and confirming loan record is persisted.

**Acceptance Scenarios**:

1. **Given** a customer accesses the loan application, **When** they submit valid loan details (amount, tenure, income verification), **Then** the system calculates and displays monthly EMI, total interest, and total amount payable
2. **Given** calculated EMI is displayed, **When** customer confirms, **Then** the loan is created with ACTIVE status and repayment schedule is generated
3. **Given** a customer submits invalid data (negative amount, invalid tenure), **When** validation occurs, **Then** clear error messages are returned and loan is not created

---

### User Story 2 - Calculate EMI for Loan Options (Priority: P1)

A customer wants to compare different loan options by calculating EMI for various amounts and tenures before making a decision.

**Why this priority**: EMI calculation is core functionality referenced in requirements; enables informed decision-making before commitment.

**Independent Test**: Can be fully tested by calling EMI calculation API with various inputs and verifying mathematical accuracy independent of loan creation.

**Acceptance Scenarios**:

1. **Given** loan parameters (principal amount, annual interest rate, tenure in months), **When** EMI calculation request is submitted, **Then** the system returns monthly EMI, total interest, and total amount payable
2. **Given** an EMI calculation request with zero or negative values, **When** validation occurs, **Then** error response with clear message is returned
3. **Given** multiple EMI calculation requests in sequence, **When** submitted rapidly, **Then** responses are returned within acceptable time (response time requirement)

---

### User Story 3 - Customer Makes Loan Repayment (Priority: P1)

A customer needs to make a payment toward their loan for a specific installment. The system should record the payment, update outstanding balance, and provide receipt/confirmation.

**Why this priority**: Repayment processing is critical for loan lifecycle management and revenue collection.

**Independent Test**: Can be fully tested by creating a loan, retrieving pending repayment, processing payment, and verifying balance update and status change.

**Acceptance Scenarios**:

1. **Given** a customer has an active loan with pending repayments, **When** they submit a payment for a specific installment, **Then** the repayment is marked PAID, outstanding balance is updated, and confirmation is returned
2. **Given** a customer attempts to pay an already-paid installment, **When** repayment submission occurs, **Then** system rejects with appropriate error message
3. **Given** a customer submits a payment amount, **When** amount is less than due EMI, **Then** repayment is marked PARTIALLY_PAID and customer is notified

---

### User Story 4 - View Loan Details and Repayment Schedule (Priority: P2)

A customer needs to view their active loans, outstanding balance, remaining tenure, and upcoming repayment schedule.

**Why this priority**: Essential for customer self-service; reduces support inquiries. Depends on P1 stories for data availability.

**Independent Test**: Can be tested by retrieving loan details and repayment schedule after loan creation.

**Acceptance Scenarios**:

1. **Given** a customer has an active loan, **When** they request loan details, **Then** system returns loan amount, EMI, interest rate, outstanding balance, remaining tenure, and next payment due date
2. **Given** a customer requests repayment schedule, **When** request is submitted, **Then** system returns all installments with due dates, amounts, and current status

---

### User Story 5 - Admin Views Overdue Repayments (Priority: P3)

An admin needs to identify customers with overdue loan repayments for follow-up actions.

**Why this priority**: Support function for collection management; can be deferred if needed. Less critical than customer-facing functionality.

**Independent Test**: Can be tested by creating loans with past-due dates and verifying overdue report retrieval.

**Acceptance Scenarios**:

1. **Given** there are overdue repayments in the system, **When** admin requests overdue list, **Then** system returns all overdue installments with customer details and overdue amount
2. **Given** an installment due date passes without payment, **When** system checks status, **Then** repayment status is marked OVERDUE

---

### Edge Cases

- What happens when a customer submits a payment exceeding the total EMI?
- How does the system handle loan requests with tenure exceeding 360 months?
- What is the minimum loan amount acceptable?
- How does the system behave when interest rates are 0%?
- What happens during concurrent payment submissions for the same installment?
- How is loan data handled if a customer requests account closure with outstanding balance?

## Requirements

### Functional Requirements

- **FR-001**: System MUST provide a Personal Loan API endpoint to create new loans with principal amount, annual interest rate, and tenure
- **FR-002**: System MUST calculate monthly EMI using standard amortization formula for all loans
- **FR-003**: System MUST provide an EMI Calculation API endpoint accepting principal amount, annual interest rate, and tenure (without creating a loan)
- **FR-004**: System MUST validate all user input (principal amount, interest rate, tenure, payment amounts) and return descriptive error messages
- **FR-005**: System MUST generate and persist a complete repayment schedule upon loan creation with all installment details
- **FR-006**: System MUST provide a Loan Repayment API endpoint to record customer payments and update loan status
- **FR-007**: System MUST maintain accurate outstanding balance and remaining tenure after each repayment
- **FR-008**: System MUST log all significant actions (loan creation, repayment processing, status changes) with timestamps and actor information
- **FR-009**: System MUST retrieve loan details including outstanding balance, EMI, remaining tenure, and current status
- **FR-010**: System MUST retrieve repayment schedule with status (PENDING, PAID, PARTIALLY_PAID, OVERDUE) for each installment
- **FR-011**: System MUST [NEEDS CLARIFICATION: Define authentication/authorization model - are all APIs public or do they require authentication?]
- **FR-012**: System MUST [NEEDS CLARIFICATION: Define user roles - are there customer vs. admin vs. staff roles with different permissions?]

### Key Entities

- **PersonalLoan**: Represents a loan issued to a customer
  - Attributes: LoanID, CustomerID, PrincipalAmount, AnnualInterestRate, LoanTenureMonths, MonthlyEMI, TotalInterestPayable, OutstandingBalance, RemainingTenure, Status (ACTIVE, CLOSED, SUSPENDED, DEFAULTED), CreatedAt, ApprovedAt, ClosedAt
  - Relationships: Has many LoanRepayments

- **LoanRepayment**: Represents a single installment payment obligation or transaction
  - Attributes: RepaymentID, LoanID, InstallmentNumber, PrincipalAmount, InterestAmount, TotalAmount, Status (PENDING, PAID, PARTIALLY_PAID, OVERDUE, WAIVED), DueDate, PaidDate, PaidAmount, CreatedAt
  - Relationships: Belongs to PersonalLoan

- **Customer**: [NEEDS CLARIFICATION: Should we model Customer entity separately or reference by ID?]
  - Minimal attributes needed: CustomerID, Name, Email, Phone, KYC Status?

### Non-Functional Requirements

- **NFR-001**: API response time MUST be under [NEEDS CLARIFICATION: specific target in milliseconds? e.g., 500ms, 1000ms?] for standard operations
- **NFR-002**: System MUST support [NEEDS CLARIFICATION: expected concurrent users? e.g., 100, 1000, 10000?]
- **NFR-003**: EMI calculation accuracy MUST be to 2 decimal places
- **NFR-004**: System MUST use ACID-compliant database transactions for payment processing
- **NFR-005**: All sensitive data (loan amounts, personal data) MUST be encrypted at rest and in transit
- **NFR-006**: System MUST maintain audit logs for all financial transactions

## Success Criteria

### Measurable Outcomes

- **SC-001**: All three core APIs (Personal Loan, EMI Calculation, Repayment) are implemented and deployed to production
- **SC-002**: [NEEDS CLARIFICATION: EMI calculation accuracy test - should match standard financial calculators within acceptable variance]
- **SC-003**: API response times consistently meet the performance target for 95th percentile
- **SC-004**: Loan repayment schedule is accurately generated with correct principal/interest split per installment
- **SC-005**: All user input validation errors are caught before database write operations
- **SC-006**: Comprehensive audit logs capture all loan lifecycle events
- **SC-007**: [NEEDS CLARIFICATION: Define user acceptance criteria - e.g., "Customer can complete loan application in under 3 minutes"]
- **SC-008**: [NEEDS CLARIFICATION: Define availability requirement - e.g., "99.5% uptime SLA"]

## Clarifications

*(Clarifications will be recorded here as questions are answered)*

---

**Version**: 0.1.0 (Draft) | **Last Updated**: 2026-02-24

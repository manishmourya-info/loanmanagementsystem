# Feature Specification: Consumer Finance Multi-API Platform

**Feature Branch**: `001-finance-apis`  
**Created**: February 25, 2026  
**Status**: Draft  
**Input**: User description: "Consumer Finance app with these apis: Consumer API, Principal account API, Vendor API, Vendor Linked Account API, Loan API, Calculate EMI API, Loan repayment API, Application Health API, Validate user input, Log proper actions, Low response time"

## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
  Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - Consumer Registration and Account Creation (Priority: P1)

A new customer registers in the system and creates their consumer profile, linking their principal account (savings/checking account) for loan disbursement and repayments.

**Why this priority**: Core foundation - customers cannot apply for loans without first creating their profile and linking their banking account. This is the entry point for all other features.

**Independent Test**: Can be fully tested by registering a consumer, validating input constraints, and confirming the consumer and principal account records are created.

**Acceptance Scenarios**:

1. **Given** a prospective customer accesses the registration endpoint, **When** they submit valid consumer information (name, email, phone, identity verification), **Then** a consumer profile is created with a unique ID
2. **Given** a registered consumer, **When** they link their principal account with valid account details, **Then** the account is verified and associated with their profile
3. **Given** invalid input data (missing required fields, invalid email format), **When** submitted, **Then** validation errors are returned with clear messages indicating which fields are problematic

---

### User Story 2 - Loan Application and Approval (Priority: P1)

A consumer applies for a personal loan through the Loan API, providing loan details (amount, tenure). The system processes the application and notifies the consumer of approval/rejection status.

**Why this priority**: Core business functionality - this is how the company generates revenue and serves customers' financial needs. Must work reliably as the primary use case.

**Independent Test**: Can be tested independently by submitting a loan application and verifying the loan record is created with proper status tracking.

**Acceptance Scenarios**:

1. **Given** a registered consumer, **When** they submit a loan application via the Loan API with amount and tenure, **Then** the loan record is created with "PENDING" status
2. **Given** a loan application, **When** business approval occurs, **Then** the status changes to "APPROVED" and the consumer is notified
3. **Given** invalid loan parameters (negative amount, tenure outside acceptable range), **When** submitted, **Then** validation errors are returned

---

### User Story 3 - EMI Calculation and Display (Priority: P1)

A consumer wants to know their monthly EMI (Equated Monthly Installment) for a potential loan. The Calculate EMI API computes the EMI based on principal amount, interest rate, and tenure, helping customers make informed decisions.

**Why this priority**: Essential for user decision-making before loan commitment. Customers need this to evaluate loan affordability. Creates value in the pre-application phase.

**Independent Test**: Can be fully tested by calling the Calculate EMI API with various parameters and validating mathematical accuracy of returned EMI.

**Acceptance Scenarios**:

1. **Given** loan parameters (principal amount, interest rate %, tenure in months), **When** the Calculate EMI API is called, **Then** the exact EMI amount is returned with calculation breakdown
2. **Given** valid parameters, **When** EMI is calculated, **Then** response time is under 500ms
3. **Given** invalid parameters (negative principal, invalid tenure), **When** submitted, **Then** validation error is returned with helpful guidance

---

### User Story 4 - Loan Repayment Processing (Priority: P2)

A consumer makes a repayment towards their active loan. The system records the payment, updates the outstanding balance, and provides payment confirmation.

**Why this priority**: Critical for loan lifecycle management and revenue collection. Cannot operate without this, but can defer implementation slightly after initial loan approval.

**Independent Test**: Can be tested by recording a repayment against an active loan and verifying balance updates and transaction records.

**Acceptance Scenarios**:

1. **Given** an active loan with outstanding balance, **When** a repayment is submitted via the Loan Repayment API, **Then** the payment is recorded and balance is updated correctly
2. **Given** a successful repayment, **When** processed, **Then** a confirmation is returned with transaction ID and new outstanding balance
3. **Given** invalid repayment (amount exceeds outstanding balance, wrong loan ID), **When** submitted, **Then** appropriate validation error is returned

---

### User Story 5 - Vendor Account Management (Priority: P2)

Vendors (merchants, service providers) register in the system and create linked accounts. The platform tracks vendor transactions for partner integrations and merchant services.

**Why this priority**: Enables B2B partnerships and merchant ecosystem. Important for business expansion but secondary to core consumer loan operations.

**Independent Test**: Can be tested independently by creating vendor profiles and linked accounts, validating data persistence.

**Acceptance Scenarios**:

1. **Given** vendor registration credentials, **When** submitted via Vendor API, **Then** a vendor account is created with unique vendor ID
2. **Given** a registered vendor, **When** they link a vendor account, **Then** the linked account is associated and ready for transactions
3. **Given** vendor account operations, **When** data is submitted, **Then** all input validation rules apply with appropriate error messages

---

### User Story 6 - Application Health and Monitoring (Priority: P3)

Operations teams need to monitor the health of the Consumer Finance application. The Application Health API provides real-time status of all critical services and dependencies.

**Why this priority**: Supporting feature for operations and maintenance. Necessary for production reliability but not needed for initial MVP launch.

**Independent Test**: Can be tested by calling the Health API and verifying it returns accurate status of all components.

**Acceptance Scenarios**:

1. **Given** the Application Health API is called, **When** all services are operational, **Then** a healthy status (HTTP 200) is returned with component statuses
2. **Given** a service dependency failure, **When** the health check runs, **Then** the status reflects the degraded state with affected components identified
3. **Given** the health endpoint is called, **When** response is returned, **Then** response time is under 100ms even during high system load

### Edge Cases

- What happens when a consumer attempts to apply for a loan while already having an active loan in "PENDING" approval status?
- How does the system handle repayment attempts when the loan has already been fully repaid?
- What occurs if a vendor linked account becomes inactive or is deleted while transactions are in progress?
- How does EMI calculation handle edge cases like very small principal amounts (< $100) or very long tenures (> 30 years)?
- What is the system behavior when input validation encounters concurrent requests from the same user (race conditions)?
- How should the system respond if the principal account linked to a consumer becomes invalid or is closed?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a Consumer API that allows registration of new consumers with mandatory fields (name, email, phone, identity verification type)
- **FR-002**: System MUST validate all user input for format, length, and required field presence before processing
- **FR-003**: System MUST provide a Principal Account API that allows consumers to link their banking account with account number, account holder name, and bank details
- **FR-004**: System MUST provide a Vendor API that allows vendor registration with business details and unique vendor identification
- **FR-005**: System MUST provide a Vendor Linked Account API that allows vendors to create and manage linked vendor accounts
- **FR-006**: System MUST provide a Loan API that allows consumers to create loan applications with loan amount and tenure parameters
- **FR-007**: System MUST track loan status through lifecycle (PENDING, APPROVED, REJECTED, ACTIVE, COMPLETED)
- **FR-008**: System MUST provide a Calculate EMI API that computes monthly installment using standard loan calculation formula: EMI = (P × r × (1 + r)^n) / ((1 + r)^n - 1), where P is principal, r is monthly interest rate, n is number of months
- **FR-009**: System MUST return EMI calculation results within 500ms
- **FR-010**: System MUST provide a Loan Repayment API that records payments against active loans and updates outstanding balance
- **FR-011**: System MUST prevent repayment amounts exceeding outstanding balance for a loan
- **FR-012**: System MUST provide an Application Health API that returns status of all critical services (database, message queue, external APIs)
- **FR-013**: System MUST log all API actions with timestamp, user context, request parameters, and response status
- **FR-014**: System MUST maintain detailed audit logs for security-sensitive operations (account linking, loan approval, repayments)
- **FR-015**: System MUST implement request validation at API layer before business logic execution
- **FR-016**: System MUST return appropriate HTTP status codes (200 success, 400 validation error, 401 unauthorized, 404 not found, 500 server error)
- **FR-017**: System MUST return error responses in consistent JSON format with error code, message, and affected fields
- **FR-018**: System MUST enforce low response times: standard API endpoints under 1000ms, Calculate EMI under 500ms, Health checks under 100ms

### Key Entities

- **Consumer**: Represents an individual customer with personal information, identity verification, and linked banking account
  - Attributes: ConsumerId, Name, Email, PhoneNumber, IdentityType, IdentityNumber, CreatedDate, Status (ACTIVE/INACTIVE)
  - Relationships: One Consumer can have multiple Loans, one Consumer links to one Principal Account

- **Principal Account**: Represents the consumer's primary banking account used for loan disbursement and repayments
  - Attributes: PrincipalAccountId, ConsumerId, AccountNumber, AccountHolderName, BankCode, VerificationStatus, LinkedDate
  - Relationships: One Principal Account belongs to one Consumer

- **Loan**: Represents a personal loan application and contract
  - Attributes: LoanId, ConsumerId, PrincipalAmount, InterestRate, TenureMonths, Status, ApprovalDate, DisbursementDate, MaturityDate, CreatedDate
  - Relationships: One Loan belongs to one Consumer, one Loan has multiple Loan Repayments, one Loan has an EMI record

- **Loan Repayment**: Represents a payment transaction against a loan
  - Attributes: RepaymentId, LoanId, RepaymentAmount, RepaymentDate, TransactionId, Status (PENDING/COMPLETED/FAILED)
  - Relationships: Many Repayments belong to one Loan

- **Vendor**: Represents a merchant or service provider in the ecosystem
  - Attributes: VendorId, VendorName, BusinessType, ContactEmail, ContactPhone, RegistrationDate, Status
  - Relationships: One Vendor can have multiple Vendor Linked Accounts

- **Vendor Linked Account**: Represents vendor's integrated account for transactions
  - Attributes: VendorAccountId, VendorId, AccountType, AccountDetails, ActivationDate, Status
  - Relationships: Many Vendor Linked Accounts belong to one Vendor

- **Audit Log**: Represents system audit trail for compliance and security
  - Attributes: AuditLogId, Timestamp, ActionType, UserId, EntityType, EntityId, OldValue, NewValue, IPAddress
  - Relationships: Records all sensitive operations across entities

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All standard API endpoints respond within 1 second (1000ms) for 95th percentile of requests under normal load
- **SC-002**: Calculate EMI API responds within 500ms for 99th percentile of requests
- **SC-003**: Application Health API responds within 100ms
- **SC-004**: System validates and rejects invalid input with appropriate error messages in 100% of test cases
- **SC-005**: All API requests are logged with required audit trail information (95%+ coverage of endpoint calls)
- **SC-006**: Sensitive operations (account linking, loan approval, repayments) are logged in audit trail for 100% of transactions
- **SC-007**: Consumer can complete full loan application flow (registration, account linking, loan application, EMI calculation) in under 5 minutes
- **SC-008**: System handles 1000 concurrent API requests without errors or significant performance degradation (< 10% latency increase)
- **SC-009**: All data validation rules are consistently enforced across all six API endpoints with consistent error message format
- **SC-010**: Zero critical security issues identified in input validation implementation

## Assumptions *(mandatory)*

This specification makes the following reasonable assumptions based on standard industry practices for consumer finance applications:

### Technical Assumptions
- System uses standard REST API architecture with JSON request/response format
- Database transactions are ACID-compliant for financial operations
- Authentication/Authorization is already handled upstream (specification focuses on data validation and business logic)
- Interest rates are configured in system (not calculated; provided as input parameter)
- System operates in a single currency (specified during implementation)

### Business Assumptions
- Loan approval follows standard underwriting criteria (system doesn't specify approval rules; those are business decisions)
- Repayment schedule is calculated based on standard EMI formula (standard in consumer finance)
- Principal account verification happens synchronously at the time of linking (no external async processes assumed)
- Vendors are pre-approved entities; vendor registration is administrative (no customer vendor onboarding)
- Audit logs are kept indefinitely (no retention policy specified; implementation to follow compliance requirements)

### Behavioral Assumptions
- Consumers can have only ONE active principal account at a time (standard for primary account model)
- Loan amounts are positive and tenure is in whole months (standard parameters)
- Health check includes only critical internal services (not third-party SLA dependencies unless integrated)
- Validation errors are recoverable (invalid input doesn't block user, just requires correction)
- All timestamps use UTC for consistency across systems

## Dependencies & Scope Boundaries

### In Scope
- Core API design and implementation for eight endpoints
- Input validation at API layer
- Basic request/response logging
- Audit logging for sensitive operations
- EMI calculation logic
- Loan lifecycle state management

### Out of Scope (For Future Features)
- Customer notification/messaging system (mentioned in user story but delegated as separate feature)
- Loan approval automation/workflow (specification assumes manual or external approval)
- Advanced security (2FA, encryption - assumed handled by platform)
- Integration with external banking APIs (assumed abstracted layer exists)
- UI/Frontend implementation
- Payment gateway integration (assumed for repayment processing)
- Risk assessment and underwriting algorithms
- Vendor transaction processing details (high-level structure only)

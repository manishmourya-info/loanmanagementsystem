# Implementation Tasks: Consumer Finance Multi-API Platform

**Feature**: 001-finance-apis  
**Date**: February 25, 2026  
**Status**: Ready for Implementation  
**Total Tasks**: 42 implementation tasks across 7 phases

---

## Executive Summary

This document breaks down the Consumer Finance Multi-API Platform into implementable tasks organized by user story priority and technical dependency. The implementation follows a progressive delivery model:

- **Phase 1**: Setup & Infrastructure (6 tasks) - Project configuration, dependencies, build pipeline
- **Phase 2**: Foundational Services (8 tasks) - Core domain entities, repositories, base services
- **Phase 3**: User Story 1 - Consumer Registration & Onboarding (7 tasks)
- **Phase 4**: User Story 2 - Loan Application & Approval (8 tasks)
- **Phase 5**: User Story 3 - EMI Calculation (6 tasks)
- **Phase 6**: User Story 4 - Loan Repayment Processing (4 tasks)
- **Phase 7**: User Story 5&6 - Vendor & Health APIs (3 tasks)

---

## Dependency Graph & Story Completion Order

```
Phase 1: Setup
    ↓
Phase 2: Foundational Services (Consumer, Loan, Repayment entities)
    ↓
Phase 3: User Story 1 (P1) - Consumer Registration
    ├─ Parallel: Phase 4: User Story 2 (P1) - Loan Application
    ├─ Parallel: Phase 5: User Story 3 (P1) - EMI Calculation
    ↓
Phase 6: User Story 4 (P2) - Loan Repayment
Phase 7: User Story 5&6 (P2/P3) - Vendor & Health APIs

Legend:
- P1 = Critical path (Consumer, Loans, EMI, Repayment)
- P2 = Important but can wait (Vendor APIs)
- P3 = Nice-to-have, non-blocking (Health monitoring)
```

**Recommended MVP Scope**: Phase 1 + Phase 2 + Phase 3 + Phase 4 + Phase 5
- Delivers: Complete loan lifecycle from consumer registration to EMI calculation
- Delivery: 2-3 weeks with full team
- Value: Core business functionality

---

## Parallel Execution Strategy

### Sprint 1 (Week 1)
- **Track A** (Backend): Phase 1 (Setup) + Phase 2 (Foundations)
- **Track B** (Frontend/Testing): Research, environment setup, test infrastructure
- **Sync Point**: End of Phase 2 - all services ready

### Sprint 2 (Week 2)
- **Track A**: Phase 3 (Consumer API) + Phase 4 (Loan API) [parallel]
- **Track B**: Phase 5 (EMI Calculation) + integration tests
- **Sync Point**: All three stories integrated

### Sprint 3 (Week 3)
- **Track A**: Phase 6 (Repayment API) + Phase 7 (Vendor + Health)
- **Track B**: End-to-end testing, performance validation
- **Sync Point**: All 8 endpoints deployed, ready for UAT

---

## Phase 1: Setup & Infrastructure (6 Tasks)

**Objective**: Configure project structure, dependencies, and build pipeline

- [ ] **T001** Create Maven project structure with Spring Boot 3.2.0 parent
  - **File**: `pom.xml`
  - **Details**: 
    - Set Java 17 LTS compiler target
    - Add Spring Boot 3.2.0 BOM
    - Add core dependencies: spring-web, spring-data-jpa, mysql-connector
    - Add security: spring-security, jjwt
    - Add utilities: lombok, springdoc-openapi-ui
    - Add testing: junit-jupiter, mockito, h2 (in-memory DB for tests)
    - Configure maven-compiler-plugin with -Xlint:all and -Werror
  - **Acceptance Criteria**:
    - `mvn clean compile` completes without warnings
    - All dependencies resolve correctly
    - Build time < 30 seconds

- [ ] **T002** Configure application properties and profiles
  - **File**: `src/main/resources/`
  - **Details**:
    - Create `application.properties` (default)
    - Create `application-mysql.properties` (MySQL profile)
    - Create `application-prod.properties` (production profile)
    - Create `application.yml` (YAML configuration)
    - Configure logging: `logback.xml`, `logback-prod.xml`
  - **Acceptance Criteria**:
    - Application starts with `--spring.profiles.active=mysql`
    - Health check responds at `http://localhost:8080/api/v1/health`
    - Logging output visible in console and file

- [ ] **T003** Set up MySQL database and Flyway migrations
  - **Files**: `src/main/resources/db/migration/`
  - **Details**:
    - Create `V1__create_personal_loans_table.sql` - Personal loans schema
    - Create `V2__create_loan_repayments_table.sql` - Repayment tracking
    - Create `V3__create_audit_log_table.sql` - Audit trail
    - Configure Flyway in pom.xml
    - Enable auto-migration on startup
  - **Acceptance Criteria**:
    - `mvn flyway:info` shows 3 migrations
    - Application auto-creates schema on first run
    - `flyway_schema_history` table populated

- [ ] **T004** Configure Spring Security and JWT authentication
  - **Files**: `src/main/java/com/consumerfinance/config/SecurityConfig.java`
  - **Details**:
    - Create SecurityFilterChain bean
    - Configure JWT filter for all endpoints except `/health`
    - Enable method-level security with `@PreAuthorize`
    - Configure CORS if needed
    - Set CSRF disabled for stateless API
  - **Acceptance Criteria**:
    - POST requests without JWT return 401 Unauthorized
    - GET `/health` accessible without JWT
    - Valid JWT token grants access to protected endpoints

- [ ] **T005** Set up OpenAPI/Swagger documentation
  - **Files**: `src/main/java/com/consumerfinance/config/OpenApiConfig.java`
  - **Details**:
    - Add springdoc-openapi dependency
    - Create OpenApiConfig bean
    - Configure API title, version, description
    - Endpoint: `/v3/api-docs` returns OpenAPI JSON
    - Endpoint: `/swagger-ui.html` displays Swagger UI
  - **Acceptance Criteria**:
    - `http://localhost:8080/swagger-ui.html` loads in browser
    - OpenAPI JSON available at `/v3/api-docs`
    - All endpoints will auto-document once created

- [ ] **T006** Configure build pipeline and Maven plugins
  - **Files**: `pom.xml`, `Dockerfile`, `docker-compose.yml`
  - **Details**:
    - Add maven-surefire-plugin for test execution
    - Add jacoco-maven-plugin for code coverage
    - Add jib-maven-plugin for Docker image creation
    - Create `Dockerfile` for production builds
    - Create `docker-compose.yml` for local MySQL + app
    - Configure GitHub Actions workflow (if applicable)
  - **Acceptance Criteria**:
    - `mvn clean package` creates executable JAR
    - `mvn jacoco:report` generates coverage report (target/site/jacoco/)
    - `mvn jib:build` creates Docker image successfully
    - `docker-compose up` starts MySQL and app

---

## Phase 2: Foundational Services (8 Tasks)

**Objective**: Implement core domain entities, repositories, and base service layer

**Independent Test**: Can test data persistence separately from business logic

- [ ] **T007** Create Consumer entity and repository
  - **Files**: 
    - `src/main/java/com/consumerfinance/domain/Consumer.java`
    - `src/main/java/com/consumerfinance/repository/ConsumerRepository.java`
  - **Details**:
    - Define Consumer JPA entity with all fields from data-model.md
    - Use UUID for consumerId, @GeneratedValue(strategy = GenerationType.UUID)
    - Add optimistic locking with @Version
    - Create custom repository methods: findByEmail, findByPhone
    - Add validation: @NotBlank, @Email, etc.
  - **Test File**: `src/test/java/com/consumerfinance/repository/ConsumerRepositoryTest.java`
  - **Acceptance Criteria**:
    - `mvn test` passes ConsumerRepositoryTest
    - Consumer saved to MySQL with correct fields
    - Queries return expected results

- [ ] **T008** Create PrincipalAccount entity and repository
  - **Files**:
    - `src/main/java/com/consumerfinance/domain/PrincipalAccount.java`
    - `src/main/java/com/consumerfinance/repository/PrincipalAccountRepository.java`
  - **Details**:
    - Define PrincipalAccount JPA entity
    - Add @OneToOne relationship to Consumer with UNIQUE constraint
    - Include verification status enum (PENDING, VERIFIED, FAILED, REJECTED)
    - Encrypted account number at rest (if security requirement)
  - **Test File**: `src/test/java/com/consumerfinance/repository/PrincipalAccountRepositoryTest.java`
  - **Acceptance Criteria**:
    - Account linked to consumer correctly
    - Only one principal account per consumer (UNIQUE constraint enforced)

- [ ] **T009** Create PersonalLoan entity and repository with BigDecimal precision
  - **Files**:
    - `src/main/java/com/consumerfinance/domain/PersonalLoan.java`
    - `src/main/java/com/consumerfinance/repository/PersonalLoanRepository.java`
  - **Details**:
    - Define PersonalLoan JPA entity
    - Use BigDecimal for: principal, annualInterestRate, monthlyEMI, disbursedAmount
    - Use @Enumerated for status (PENDING, APPROVED, REJECTED, ACTIVE, CLOSED, DEFAULTED)
    - Add @Version for optimistic locking
    - Add custom finder methods with @Lock annotations for pessimistic locking
    - Add @OneToMany relationship to LoanRepayment (cascade delete)
    - Add @ManyToOne to Consumer
  - **Test File**: `src/test/java/com/consumerfinance/repository/PersonalLoanRepositoryTest.java`
  - **Acceptance Criteria**:
    - BigDecimal fields preserve precision (no floating-point errors)
    - Loan linked to consumer correctly
    - Optimistic locking prevents concurrent modifications
    - Pessimistic locks available for payment processing

- [ ] **T010** Create LoanRepayment entity and repository
  - **Files**:
    - `src/main/java/com/consumerfinance/domain/LoanRepayment.java`
    - `src/main/java/com/consumerfinance/repository/LoanRepaymentRepository.java`
  - **Details**:
    - Define LoanRepayment JPA entity
    - Use BigDecimal for emiAmount, paidAmount
    - Status enum (PENDING, PAID, DEFAULTED, WAIVED, ADJUSTED)
    - Add @ManyToOne relationship to PersonalLoan
    - Include unique constraint on transactionId
    - Add @Version for locking
    - Custom finder: findByLoanIdAndStatus, findOverdueRepayments
  - **Test File**: `src/test/java/com/consumerfinance/repository/LoanRepaymentRepositoryTest.java`
  - **Acceptance Criteria**:
    - Repayment linked to loan correctly
    - Transaction ID unique constraint enforced
    - Query methods return correct result sets

- [ ] **T011** Create Vendor and VendorLinkedAccount entities with repositories
  - **Files**:
    - `src/main/java/com/consumerfinance/domain/Vendor.java`
    - `src/main/java/com/consumerfinance/domain/VendorLinkedAccount.java`
    - `src/main/java/com/consumerfinance/repository/VendorRepository.java`
    - `src/main/java/com/consumerfinance/repository/VendorLinkedAccountRepository.java`
  - **Details**:
    - Create Vendor entity with registration tracking
    - Create VendorLinkedAccount with @ManyToOne to Vendor
    - Status enum for both (ACTIVE, INACTIVE, SUSPENDED, etc.)
    - Unique constraints on registrationNumber, gstNumber
    - Max 5 accounts per vendor (validate in service layer)
  - **Test File**: `src/test/java/com/consumerfinance/repository/VendorRepositoryTest.java`
  - **Acceptance Criteria**:
    - Vendor and account relationships work correctly
    - Unique constraints enforced

- [ ] **T012** Create AuditLog entity and repository
  - **Files**:
    - `src/main/java/com/consumerfinance/domain/AuditLog.java`
    - `src/main/java/com/consumerfinance/repository/AuditLogRepository.java`
  - **Details**:
    - Create AuditLog entity with auto-incrementing auditId
    - Include: action, loanId, userId, amount, details (JSON), timestamp
    - Make immutable (no update/delete methods exposed)
    - Add indexes on loanId, userId, timestamp for fast querying
    - Custom finder: getAuditsByLoanId, getAuditsByAction, getRecentAudits
  - **Test File**: `src/test/java/com/consumerfinance/repository/AuditLogRepositoryTest.java`
  - **Acceptance Criteria**:
    - Audit logs created and persisted
    - No update/delete operations available
    - Queries return correct results

- [ ] **T013** Create global exception handler and error response DTOs
  - **Files**:
    - `src/main/java/com/consumerfinance/exception/GlobalExceptionHandler.java`
    - `src/main/java/com/consumerfinance/config/ErrorResponse.java`
    - `src/main/java/com/consumerfinance/exception/LoanNotFoundException.java`
    - `src/main/java/com/consumerfinance/exception/InvalidRepaymentException.java`
  - **Details**:
    - Create custom exceptions for business logic
    - Implement @ControllerAdvice for global error handling
    - Standardize error response format (timestamp, status, message, errors[], path)
    - Map exceptions to appropriate HTTP status codes
    - Include field-level validation error details
    - Log errors with appropriate levels (INFO, WARN, ERROR)
  - **Test File**: `src/test/java/com/consumerfinance/exception/GlobalExceptionHandlerTest.java`
  - **Acceptance Criteria**:
    - Validation errors return 400 with field details
    - Not found errors return 404
    - Conflict errors return 409
    - Error response format consistent

- [ ] **T014** Create base DTOs for requests and responses
  - **Files**: `src/main/java/com/consumerfinance/dto/`
  - **Details**:
    - Create ConsumerRequest, ConsumerResponse
    - Create PrincipalAccountRequest, PrincipalAccountResponse
    - Create CreateLoanRequest, LoanResponse
    - Create EMICalculationRequest, EMICalculationResponse
    - Create PaymentRequest, PaymentResponse
    - Add validation annotations: @NotBlank, @Min, @Max, @DecimalMin, @Pattern, etc.
    - Use Lombok @Data/@Builder for boilerplate reduction
    - Include BigDecimal fields with proper precision
  - **Acceptance Criteria**:
    - Serialization/deserialization works correctly
    - Validation constraints enforced
    - Response DTOs include only necessary fields

---

## Phase 3: User Story 1 - Consumer Registration & Onboarding (P1) (7 Tasks)

**User Story**: "As a customer, I want to register in the system and link my banking account so I can apply for loans"

**Independent Test**: 
- Can create consumer with valid KYC data
- Can link principal account to consumer
- Cannot create loan without verified account
- Complete onboarding flow testable in isolation

### Story Goal
Enable customer registration, profile management, and banking account linking with full input validation.

### Implementation Sequence

- [ ] **T015** [P] Implement ConsumerService with business logic
  - **File**: `src/main/java/com/consumerfinance/service/ConsumerService.java`
  - **Details**:
    - Method: createConsumer(ConsumerRequest) → ConsumerResponse
      - Validate input (name, email format, phone E.164)
      - Check email/phone uniqueness
      - Create Consumer with status=ACTIVE, kycStatus=PENDING
      - Save to repository
      - Log action: CONSUMER_CREATED
    - Method: getConsumer(UUID consumerId) → ConsumerResponse
    - Method: updateConsumer(UUID, ConsumerRequest) → ConsumerResponse
      - Reset kycStatus to PENDING if identity changes
    - Method: getAllConsumers(Pageable) → Page<ConsumerResponse>
      - Filter by status
      - Support search by name/email/phone
    - Add @Transactional on all methods
    - Add validation exception handling
  - **Test File**: `src/test/java/com/consumerfinance/service/ConsumerServiceTest.java`
  - **Coverage**: 80%+ on service methods
  - **Acceptance Criteria**:
    - Consumer created successfully with valid data
    - Duplicate email/phone rejected (409 Conflict)
    - Invalid email format rejected (400 Bad Request)
    - KYC status resets on identity update
    - Pagination works correctly

- [ ] **T016** [P] Implement PrincipalAccountService
  - **File**: `src/main/java/com/consumerfinance/service/PrincipalAccountService.java`
  - **Details**:
    - Method: linkPrincipalAccount(UUID consumerId, PrincipalAccountRequest) → PrincipalAccountResponse
      - Validate consumer exists
      - Validate account holder name matches consumer name (80%+ fuzzy match)
      - Validate account number format (IBAN or national)
      - Create PrincipalAccount with status=PENDING
      - Only one principal account per consumer (replace if exists)
      - Log action: ACCOUNT_LINKED
    - Method: getPrincipalAccount(UUID consumerId) → PrincipalAccountResponse
    - Method: updatePrincipalAccount(UUID consumerId, Request) → Response
      - Reset verification status to PENDING
      - Cannot update if consumer has ACTIVE loans
    - Method: verifyAccount(UUID accountId) → Response (admin operation)
      - Set status=VERIFIED
      - Set linkedDate=now
  - **Test File**: `src/test/java/com/consumerfinance/service/PrincipalAccountServiceTest.java`
  - **Coverage**: 80%+
  - **Acceptance Criteria**:
    - Account linked successfully
    - Fuzzy name matching works (80%+ threshold)
    - Only one account per consumer enforced
    - Verification status managed correctly
    - Cannot update account if loan active

- [ ] **T017** [P] Implement ConsumerController with REST endpoints
  - **File**: `src/main/java/com/consumerfinance/controller/ConsumerController.java`
  - **Details**:
    - Endpoint: POST `/consumers` → Create consumer
      - Input: ConsumerRequest
      - Output: 201 Created with ConsumerResponse
      - Validation: @Valid on request
    - Endpoint: GET `/consumers/{consumerId}` → Get consumer
      - Output: 200 OK with ConsumerResponse
      - Authorization: CUSTOMER views own; ADMIN/LOAN_MANAGER view any
    - Endpoint: PUT `/consumers/{consumerId}` → Update consumer
      - Output: 200 OK with updated response
    - Endpoint: GET `/consumers` → List consumers (pagination)
      - Query params: page, size, status, search
      - Output: 200 OK with Page<ConsumerResponse>
      - Authorization: ADMIN/LOAN_MANAGER only
    - All endpoints require JWT (except health)
    - Add proper error handling via GlobalExceptionHandler
  - **Annotation-based Security**: Use @PreAuthorize
  - **Test File**: `src/test/java/com/consumerfinance/controller/ConsumerControllerTest.java`
  - **Coverage**: 80%+
  - **Acceptance Criteria**:
    - POST returns 201 with Location header
    - GET returns 200 with correct data
    - PUT returns 200 with updated data
    - LIST returns paginated results
    - Unauthorized requests return 401
    - Forbidden requests return 403

- [ ] **T018** [P] Implement PrincipalAccountController
  - **File**: `src/main/java/com/consumerfinance/controller/PrincipalAccountController.java`
  - **Details**:
    - Endpoint: POST `/consumers/{consumerId}/principal-account` → Link account
      - Input: PrincipalAccountRequest
      - Output: 201 Created with response
    - Endpoint: GET `/consumers/{consumerId}/principal-account` → Get account
      - Output: 200 OK with response
    - Endpoint: PUT `/consumers/{consumerId}/principal-account` → Update account
      - Output: 200 OK with updated response
    - Authorization: CUSTOMER views own; ADMIN/LOAN_MANAGER view any
    - All endpoints under `/consumers/{consumerId}` path
  - **Test File**: `src/test/java/com/consumerfinance/controller/PrincipalAccountControllerTest.java`
  - **Coverage**: 80%+
  - **Acceptance Criteria**:
    - Account linked successfully
    - Validation errors return 400 with field details
    - Authorization enforced correctly

- [ ] **T019** [P] Implement input validation layer with custom validators
  - **Files**: 
    - `src/main/java/com/consumerfinance/validation/EmailValidator.java`
    - `src/main/java/com/consumerfinance/validation/PhoneValidator.java`
    - `src/main/java/com/consumerfinance/validation/IBANValidator.java`
  - **Details**:
    - Email: RFC 5322 compliant validation
    - Phone: E.164 format (leading +, up to 15 digits)
    - Account number: IBAN or national format validation
    - Identity number format per type (AADHAR=12 digits, etc.)
    - Create custom @Constraint annotations for reuse
    - Integrate with Bean Validation (JSR-380)
  - **Test File**: `src/test/java/com/consumerfinance/validation/ValidatorTests.java`
  - **Acceptance Criteria**:
    - Valid inputs accepted
    - Invalid formats rejected with clear messages
    - Validators reusable across endpoints

- [ ] **T020** [P] Implement audit logging for consumer operations
  - **File**: `src/main/java/com/consumerfinance/config/AuditLogAspect.java`
  - **Details**:
    - Create AOP aspect for audit logging
    - Intercept service methods marked with @Auditable
    - Log: CONSUMER_CREATED, CONSUMER_UPDATED, ACCOUNT_LINKED, ACCOUNT_VERIFIED
    - Include: action, userId, entity ID, amount (if applicable), timestamp
    - Log asynchronously to avoid blocking
    - Use MDC (Mapped Diagnostic Context) for correlation IDs
  - **Test File**: `src/test/java/com/consumerfinance/config/AuditLogAspectTest.java`
  - **Acceptance Criteria**:
    - All state-changing operations logged
    - Audit logs queryable by loanId, userId, action
    - No performance impact on business operations

- [ ] **T021** [US1] Create integration test for complete consumer onboarding flow
  - **File**: `src/test/java/com/consumerfinance/LoanManagementApplicationIntegrationTest.java`
  - **Details**:
    - Test flow: Create Consumer → Link Account → Query Account
    - Use @SpringBootTest with MySQL test container
    - Use TestRestTemplate for API calls
    - Verify:
      - Consumer created with PENDING KYC
      - Account linked with PENDING verification
      - Queries return correct data
      - Audit logs created
    - Performance test: Onboarding flow < 1000ms (95th percentile)
  - **Acceptance Criteria**:
    - End-to-end flow works correctly
    - All 3 operations succeed sequentially
    - Data persisted correctly
    - Audit trail complete

---

## Phase 4: User Story 2 - Loan Application & Approval (P1) (8 Tasks)

**User Story**: "As a customer with a verified account, I want to apply for a loan and see the status so I can understand loan approval progress"

**Independent Test**:
- Can create loan with valid parameters
- Cannot create loan without verified account
- Can query loan status
- Loan approval workflow works independently

### Story Goal
Enable loan application submission, status tracking, and approval workflow with business rule enforcement.

- [ ] **T022** [P] Implement PersonalLoanService with loan lifecycle
  - **File**: `src/main/java/com/consumerfinance/service/PersonalLoanService.java`
  - **Details**:
    - Method: createLoan(UUID consumerId, CreateLoanRequest) → LoanResponse
      - Validate consumer exists with VERIFIED KYC
      - Validate consumer has VERIFIED principal account
      - Validate principal in range [10000.00, 50000000.00]
      - Validate interest rate in range [0.01, 36.00]%
      - Validate tenure in range [12, 360] months
      - Check consumer doesn't exceed max 5 ACTIVE loans
      - Create PersonalLoan with status=PENDING
      - Return monthlyEMI (requires EMI calculation)
      - Log action: LOAN_CREATED
    - Method: getLoan(UUID loanId) → LoanResponse with full details
    - Method: getConsumerLoans(UUID consumerId, status?) → List<LoanResponse>
    - Method: approveLoan(UUID loanId, String notes) → LoanResponse
      - Check loan is PENDING
      - Set status=APPROVED, approvalDate=now
      - Log action: LOAN_APPROVED
    - Method: rejectLoan(UUID loanId, String reason) → LoanResponse
      - Set status=REJECTED
      - Log action: LOAN_REJECTED
    - Method: disburseLoan(UUID loanId) → LoanResponse
      - Check loan is APPROVED
      - Verify funds available
      - Set status=ACTIVE, disbursementDate=now
      - Calculate maturityDate = disbursementDate + tenure
      - Create initial repayment schedule
      - Log action: LOAN_DISBURSED
    - All methods @Transactional with proper isolation
  - **Test File**: `src/test/java/com/consumerfinance/service/PersonalLoanServiceTest.java`
  - **Coverage**: 80%+
  - **Acceptance Criteria**:
    - Loan created with valid parameters
    - Constraints validated (amount, rate, tenure ranges)
    - Account verification required
    - Max loans per consumer enforced
    - Status transitions work correctly
    - Monthly EMI calculated correctly

- [ ] **T023** [P] Implement LoanController with loan endpoints
  - **File**: `src/main/java/com/consumerfinance/controller/PersonalLoanController.java`
  - **Details**:
    - Endpoint: POST `/loans` → Create loan
      - Input: CreateLoanRequest (principal, rate, tenure)
      - Output: 201 Created with LoanResponse
      - Authorization: CUSTOMER creates own; LOAN_MANAGER/ADMIN can override
    - Endpoint: GET `/loans/{loanId}` → Get loan details
      - Output: 200 OK
      - Authorization: CUSTOMER views own; LOAN_MANAGER/ADMIN view any
    - Endpoint: GET `/consumers/{consumerId}/loans` → List consumer's loans
      - Query params: status filter
      - Output: 200 OK with List<LoanResponse>
    - Endpoint: PUT `/loans/{loanId}/approve` → Approve loan
      - Input: { approvalNotes: String }
      - Output: 200 OK
      - Authorization: LOAN_MANAGER/ADMIN only
    - Endpoint: PUT `/loans/{loanId}/reject` → Reject loan
      - Input: { rejectionReason: String }
      - Output: 200 OK
      - Authorization: LOAN_MANAGER/ADMIN only
    - Endpoint: PUT `/loans/{loanId}/disburse` → Disburse loan
      - Output: 200 OK
      - Authorization: LOAN_MANAGER/ADMIN only
    - Add error handling (409 for invalid status, 403 for permissions, 404 for not found)
  - **Test File**: `src/test/java/com/consumerfinance/controller/PersonalLoanControllerTest.java`
  - **Coverage**: 80%+
  - **Acceptance Criteria**:
    - All endpoints respond correctly
    - Authorization enforced
    - Status codes correct
    - Validation errors include field details

- [ ] **T024** [P] Implement EMI calculation logic in domain service
  - **File**: `src/main/java/com/consumerfinance/service/EMICalculationService.java`
  - **Details**:
    - Method: calculateEMI(BigDecimal principal, BigDecimal annualRate, int months) → BigDecimal
      - Use formula: EMI = P × r × (1+r)^n / ((1+r)^n - 1)
      - Convert annual rate to monthly: r = annual_rate / 100 / 12
      - Use BigDecimal with HALF_EVEN rounding
      - Handle zero-interest case: EMI = principal / months
      - Return EMI with 2 decimal places
      - Performance: < 50ms per calculation
    - Method: calculateSchedule(Loan) → List<RepaymentSchedule>
      - Generate all monthly repayment records
      - Calculate principal and interest components for each month
      - Return with dueDate, emiAmount, principalComponent, interestComponent, balance
    - Method: calculateTotalRepayment(BigDecimal principal, BigDecimal annualRate, int months) → {emi, totalPayment, totalInterest}
    - All calculations use BigDecimal for precision
  - **Test File**: `src/test/java/com/consumerfinance/service/EMICalculationServiceTest.java`
  - **Test Cases**:
    - Standard loan: 500k @ 12% for 60 months → verify EMI = 10644.56
    - Zero interest: 100k @ 0% for 12 months → EMI = 8333.33
    - Min principal: 10k @ 1% for 12 months
    - Max tenure: 50M @ 36% for 360 months
    - Verify total payment = EMI × months (approximately)
    - Verify total interest = total payment - principal
  - **Acceptance Criteria**:
    - All test cases pass
    - BigDecimal precision maintained
    - No floating-point errors
    - Performance < 50ms

- [ ] **T025** [P] Implement EMI Calculation Controller
  - **File**: `src/main/java/com/consumerfinance/controller/EMICalculationController.java`
  - **Details**:
    - Endpoint: POST `/emi/calculate` → Calculate EMI
      - Input: { principal, annualInterestRate, tenureMonths }
      - Output: { monthlyEMI, totalPayment, totalInterest, schedule: [...] }
      - Validation: constraints on amounts, rate, tenure
      - Performance requirement: < 500ms response
      - Authorization: authenticated users only
      - Return 200 OK with full calculation breakdown
    - Include 60+ month schedule in response for customer visibility
  - **Test File**: `src/test/java/com/consumerfinance/controller/EMICalculationControllerTest.java`
  - **Acceptance Criteria**:
    - Correct EMI returned
    - Schedule generated with all months
    - Response time < 500ms
    - Validation errors return 400

- [ ] **T026** [US2] Implement loan status state machine with validation
  - **File**: `src/main/java/com/consumerfinance/domain/LoanStatus.java` (or enum)
  - **Details**:
    - Define valid state transitions:
      - PENDING → APPROVED / REJECTED
      - APPROVED → ACTIVE (disburse) / REJECTED
      - ACTIVE → CLOSED (fully repaid) / DEFAULTED
    - Create validator: canTransitionTo(LoanStatus from, LoanStatus to) → boolean
    - Enforce in service layer before state changes
    - Log state transitions to audit
  - **Test File**: `src/test/java/com/consumerfinance/domain/LoanStatusTest.java`
  - **Acceptance Criteria**:
    - Valid transitions succeed
    - Invalid transitions rejected (409 Conflict)
    - State machine enforced consistently

- [ ] **T027** [US2] Implement repayment schedule generation on loan disbursal
  - **File**: `src/main/java/com/consumerfinance/service/RepaymentScheduleGenerator.java`
  - **Details**:
    - On disburseLoan(), generate all 60 (or tenure) repayment records
    - For each month:
      - Calculate emiAmount (monthly EMI)
      - Set dueDate = disbursement + (month × 30 days)
      - Set status=PENDING
      - Save to LoanRepayment table
    - All records created in single transaction
    - Verify total repayments match tenure
  - **Test File**: `src/test/java/com/consumerfinance/service/RepaymentScheduleGeneratorTest.java`
  - **Acceptance Criteria**:
    - 60 repayment records created for 60-month loan
    - Dates correctly spaced (monthly)
    - All with PENDING status
    - EMI amounts correct

- [ ] **T028** [US2] Create integration test for loan lifecycle
  - **File**: `src/test/java/com/consumerfinance/LoanLifecycleIntegrationTest.java`
  - **Details**:
    - Test flow: Create Loan → Approve → Disburse → Verify Schedule
    - Use @SpringBootTest with MySQL
    - Verify:
      - Loan created with PENDING status
      - Approve transitions to APPROVED
      - Disburse transitions to ACTIVE
      - Repayment schedule created (60 records for 60-month loan)
      - All audit logs created
    - Performance: Complete flow < 2000ms
  - **Acceptance Criteria**:
    - End-to-end lifecycle works
    - Status transitions correct
    - Schedule generated correctly
    - All data persisted

---

## Phase 5: User Story 3 - EMI Calculation Support (P1) (6 Tasks)

**User Story**: "As a customer considering a loan, I want to calculate my monthly EMI and see the payment breakdown so I can decide if I can afford the loan"

**Independent Test**:
- EMI calculation works without creating loan
- Customers can use calculator before commitment
- Calculation results match loan-time calculations

### Story Goal
Provide transparent EMI calculations to help customers make informed loan decisions.

- [ ] **T029** [P] Create EMICalculationResponse with full breakdown
  - **File**: `src/main/java/com/consumerfinance/dto/EMICalculationResponse.java`
  - **Details**:
    - Fields:
      - principal: BigDecimal
      - annualInterestRate: BigDecimal (%)
      - tenureMonths: int
      - monthlyEMI: BigDecimal
      - totalPayment: BigDecimal
      - totalInterest: BigDecimal
      - schedule: List<MonthlyBreakdown>
    - MonthlyBreakdown:
      - installmentNumber: int
      - dueDate: LocalDate
      - emiAmount: BigDecimal
      - principalComponent: BigDecimal
      - interestComponent: BigDecimal
      - remainingBalance: BigDecimal
    - Use Lombok @Data/@Builder
    - Add validation annotations
  - **Acceptance Criteria**:
    - JSON serialization correct
    - All BigDecimal fields preserve precision

- [ ] **T030** [P] Enhance EMI calculation with amortization schedule details
  - **File**: `src/main/java/com/consumerfinance/service/EMICalculationService.java`
  - **Details**:
    - Update calculateSchedule() to return detailed breakdown
    - For each month:
      - Calculate interest component = remaining_balance × monthly_rate
      - Calculate principal component = EMI - interest
      - Calculate remaining_balance = previous_balance - principal
      - Include percentages and formatting
    - Verify schedule totals:
      - Sum of principal components = principal
      - Sum of interest components = total_interest
      - Final remaining_balance ≈ 0
  - **Test File**: `src/test/java/com/consumerfinance/service/ScheduleCalculationTest.java`
  - **Acceptance Criteria**:
    - Schedule sums to expected totals
    - Each month calculated correctly
    - Final balance approaches zero

- [ ] **T031** [P] Add input validation for EMI calculation constraints
  - **File**: Update EMICalculationRequest validation
  - **Details**:
    - Principal constraints:
      - @DecimalMin(value = "10000.00", message = "Minimum 10,000")
      - @DecimalMax(value = "50000000.00", message = "Maximum 50 million")
    - Interest rate:
      - @DecimalMin("0.01"), @DecimalMax("36.00")
    - Tenure:
      - @Min(12), @Max(360)
    - Return 400 with detailed error for each field
  - **Test File**: `src/test/java/com/consumerfinance/controller/EMICalculationControllerValidationTest.java`
  - **Acceptance Criteria**:
    - Out-of-range values rejected
    - Error messages clear and specific

- [ ] **T032** [P] Implement caching for EMI calculations (optional optimization)
  - **File**: `src/main/java/com/consumerfinance/service/CachedEMICalculationService.java`
  - **Details**:
    - Cache common EMI calculations using @Cacheable
    - Cache key: hash(principal, rate, tenure)
    - TTL: 1 hour (rates change frequently)
    - Monitor cache hit ratio
    - Invalidate on system events
  - **Acceptance Criteria**:
    - Repeated calculations return cached results
    - Performance improved for common parameters

- [ ] **T033** [P] Create customer-facing EMI calculator documentation
  - **File**: `src/main/resources/doc/EMI-Calculator.md`
  - **Details**:
    - Formula explanation
    - Example calculations
    - Common use cases
    - API documentation
  - **Acceptance Criteria**:
    - Customers understand how EMI is calculated
    - Examples match actual calculations

- [ ] **T034** [US3] Create end-to-end EMI calculation integration test
  - **File**: `src/test/java/com/consumerfinance/EMICalculationIntegrationTest.java`
  - **Details**:
    - Test pre-application EMI calculation (no loan created)
    - Test EMI calculation during loan application (with loan creation)
    - Verify both return same EMI amount
    - Test schedule against actual loan schedule
    - Performance: < 500ms for all calculations
  - **Acceptance Criteria**:
    - Calculations match between calculator and loan
    - Schedule consistent
    - Performance requirement met

---

## Phase 6: User Story 4 - Loan Repayment Processing (P2) (4 Tasks)

**User Story**: "As a customer with an active loan, I want to make monthly EMI payments and see my outstanding balance so I can manage my loan effectively"

**Independent Test**:
- Can record payment against active loan
- Outstanding balance updated correctly
- Cannot pay more than EMI (unless allowed)
- Loan status changes to CLOSED when fully paid

### Story Goal
Enable payment processing with accurate balance tracking and loan closure.

- [ ] **T035** [P] Implement LoanRepaymentService with payment processing
  - **File**: `src/main/java/com/consumerfinance/service/LoanRepaymentService.java`
  - **Details**:
    - Method: processRepayment(UUID loanId, UUID repaymentId, BigDecimal paidAmount) → PaymentResponse
      - Use PESSIMISTIC_WRITE lock on loan to prevent concurrent modifications
      - Validate: loan is ACTIVE, repayment is PENDING
      - Validate: paidAmount >= EMI amount (or configurable tolerance)
      - Validate: transactionId unique (prevent duplicate payments)
      - Update repayment: paidAmount, status=PAID, paidDate=now
      - Update loan: disbursedAmount -= paidAmount (or track remaining)
      - Check if loan fully paid: all repayments paid
      - If fully paid: set loan status=CLOSED, closedDate=now
      - Create audit log: PAYMENT_PROCESSED
      - Return: PaymentResponse with updated balance
    - Concurrent modification handling:
      - OptimisticLockingFailureException → Retry logic or return 409 CONFLICT
      - PessimisticLockingFailureException → Return 503 SERVICE_UNAVAILABLE
    - All payment operations @Transactional
    - Performance: < 1000ms (95th percentile)
  - **Test File**: `src/test/java/com/consumerfinance/service/LoanRepaymentServiceTest.java`
  - **Test Cases**:
    - Successful payment recorded
    - Duplicate payment rejected (same transactionId)
    - Payment updates remaining balance
    - Loan closes when fully paid
    - Concurrent payments handled (optimistic lock)
  - **Acceptance Criteria**:
    - Payment processed correctly
    - Balance updated accurately
    - Concurrent modifications handled
    - Audit logged

- [ ] **T036** [P] Implement LoanRepaymentController with payment endpoints
  - **File**: `src/main/java/com/consumerfinance/controller/LoanRepaymentController.java`
  - **Details**:
    - Endpoint: POST `/loans/{loanId}/repayments` → Record payment
      - Input: { installmentNumber, paidAmount, paymentMethod, transactionId }
      - Output: 201 Created with PaymentResponse
      - Response includes: remainingBalance, nextDueDate, status
      - Authorization: PAYMENT_PROCESSOR/ADMIN or customer via payment gateway
    - Endpoint: GET `/loans/{loanId}/repayments` → Get all repayments for loan
      - Query params: status filter
      - Output: 200 OK with List<RepaymentResponse> (paginated)
    - Endpoint: GET `/loans/{loanId}/repayments/{repaymentId}` → Get specific repayment
      - Output: 200 OK with full RepaymentResponse
    - Error handling:
      - 400 for validation errors
      - 404 for loan/repayment not found
      - 409 for concurrent modification or invalid status
      - 503 for database lock timeout
  - **Test File**: `src/test/java/com/consumerfinance/controller/LoanRepaymentControllerTest.java`
  - **Coverage**: 80%+
  - **Acceptance Criteria**:
    - Payment endpoint returns 201
    - Error responses correct
    - Authorization enforced

- [ ] **T037** [P] Implement balance calculation and loan closure logic
  - **File**: Update PersonalLoanService with balance calculation
  - **Details**:
    - Method: calculateOutstandingBalance(UUID loanId) → BigDecimal
      - Get all repayments for loan
      - Sum pending repayments' emiAmount
      - Return total outstanding
    - Method: checkIfFullyPaid(UUID loanId) → boolean
      - Get all repayments
      - Check all have status=PAID
      - Return true if all paid
    - Method: closeLoan(UUID loanId) → LoanResponse
      - Verify fully paid
      - Set status=CLOSED, closedDate=now
      - Log action: LOAN_CLOSED
    - Performance: < 100ms for balance calculation
  - **Test File**: `src/test/java/com/consumerfinance/service/LoanClosureTest.java`
  - **Acceptance Criteria**:
    - Outstanding balance calculated correctly
    - Loan closed when fully paid
    - Cannot close loan until all repayments paid

- [ ] **T038** [US4] Create integration test for complete repayment flow
  - **File**: `src/test/java/com/consumerfinance/LoanRepaymentIntegrationTest.java`
  - **Details**:
    - Test flow: Create Loan → Disburse → Make Payment → Verify Balance → Close Loan
    - Alternatively: Test first EMI payment on 60-month loan
    - Verify:
      - First repayment marked PENDING initially
      - Payment accepted and recorded
      - Outstanding balance updated
      - Audit log created
      - Loan still ACTIVE (60 more repayments remain)
    - Performance: Complete payment flow < 1500ms
    - Test concurrent payments (should serialize or reject cleanly)
  - **Acceptance Criteria**:
    - Payment recorded successfully
    - Balance calculated correctly
    - No duplicate payments allowed
    - Concurrent payments handled

---

## Phase 7: User Story 5 & 6 - Vendor Management & Health Monitoring (P2/P3) (3 Tasks)

**User Stories**:
- US5: "As a merchant partner, I want to register and link my settlement accounts so I can receive disbursements"
- US6: "As an operations team member, I want to monitor system health so I can ensure the platform is operating correctly"

**Independent Test**: Can be tested separately from consumer/loan features

### Story Goals
- Enable vendor onboarding and settlement account management
- Provide real-time system health monitoring for operations

- [ ] **T039** [P] Implement VendorService and VendorController
  - **File**: 
    - `src/main/java/com/consumerfinance/service/VendorService.java`
    - `src/main/java/com/consumerfinance/controller/VendorController.java`
  - **Details**:
    - Service methods:
      - createVendor(VendorRequest) → VendorResponse
      - getVendor(UUID vendorId) → VendorResponse
      - updateVendor(UUID, VendorRequest) → VendorResponse
      - getAllVendors(Pageable) → Page<VendorResponse>
      - Validate: registrationNumber unique, gstNumber unique
    - Controller endpoints:
      - POST `/vendors` → Create vendor (201)
      - GET `/vendors/{vendorId}` → Get vendor (200)
      - PUT `/vendors/{vendorId}` → Update vendor (200)
      - GET `/vendors` → List vendors (200, paginated)
      - Authorization: ADMIN only
  - **Test File**: `src/test/java/com/consumerfinance/service/VendorServiceTest.java`
  - **Acceptance Criteria**:
    - Vendor created and retrieved correctly
    - Unique constraints enforced
    - Pagination works

- [ ] **T040** [P] Implement VendorLinkedAccountService and Controller
  - **File**:
    - `src/main/java/com/consumerfinance/service/VendorLinkedAccountService.java`
    - `src/main/java/com/consumerfinance/controller/VendorLinkedAccountController.java`
  - **Details**:
    - Service methods:
      - linkVendorAccount(UUID vendorId, AccountRequest) → AccountResponse
        - Validate: vendor exists and ACTIVE
        - Max 5 accounts per vendor
      - getVendorAccounts(UUID vendorId) → List<AccountResponse>
      - updateAccountStatus(UUID accountId, status) → AccountResponse
    - Controller endpoints:
      - POST `/vendors/{vendorId}/linked-accounts` → Link account (201)
      - GET `/vendors/{vendorId}/linked-accounts` → List accounts (200)
      - PUT `/vendors/{vendorId}/linked-accounts/{accountId}` → Update (200)
      - Authorization: ADMIN/vendor owner
  - **Test File**: `src/test/java/com/consumerfinance/service/VendorLinkedAccountServiceTest.java`
  - **Acceptance Criteria**:
    - Account linked successfully
    - Max 5 accounts per vendor enforced

- [ ] **T041** [P] Implement HealthController with system health monitoring
  - **File**: `src/main/java/com/consumerfinance/controller/HealthController.java`
  - **Details**:
    - Endpoint: GET `/health` → System health check
      - Public endpoint (no authentication)
      - Response:
        ```json
        {
          "status": "UP",
          "timestamp": "2026-02-25T15:00:00Z",
          "uptime": 86400000,
          "components": {
            "database": {"status": "UP", "responseTime": 12},
            "diskSpace": {"status": "UP", "free": 549GB},
            "jvm": {"status": "UP", "memory": "512M/2048M"},
            "applicationReady": {"status": "UP"}
          }
        }
        ```
      - Check database connectivity (SELECT 1 from personal_loans limit 1)
      - Check disk space (warn if < 10GB free)
      - Check JVM memory usage
      - Check if application fully started
      - Performance: < 100ms response
    - Status values: UP, DOWN, DEGRADED
    - Overall status = UP if all critical components UP
    - Return HTTP 200 if UP, 503 if DOWN/DEGRADED
    - Use Spring Boot Actuator for metrics
  - **Test File**: `src/test/java/com/consumerfinance/controller/HealthControllerTest.java`
  - **Acceptance Criteria**:
    - Returns 200 when healthy
    - Returns 503 when database down
    - Response time < 100ms
    - All components checked

---

## Cross-Phase Tasks

These tasks span multiple phases or are supporting/infrastructure tasks:

- [ ] **T042** [P] Set up GitHub Actions CI/CD pipeline (optional)
  - **File**: `.github/workflows/build.yml`
  - **Details**:
    - Run `mvn clean compile` on every push (fail on warnings)
    - Run `mvn test` and fail if coverage < 80%
    - Run `mvn package` to create JAR
    - Build Docker image with Jib
    - Push to registry (if configured)
    - Deploy to staging (if configured)
  - **Acceptance Criteria**:
    - Pipeline passes for all commits
    - Warnings cause build failure
    - Coverage reports generated

---

## Implementation Statistics

| Metric | Value |
|--------|-------|
| **Total Implementation Tasks** | 42 |
| **Phase 1: Setup** | 6 tasks |
| **Phase 2: Foundations** | 8 tasks |
| **Phase 3: Consumer API (P1)** | 7 tasks |
| **Phase 4: Loan API (P1)** | 8 tasks |
| **Phase 5: EMI (P1)** | 6 tasks |
| **Phase 6: Repayment (P2)** | 4 tasks |
| **Phase 7: Vendor & Health (P2/P3)** | 3 tasks |
| **Infrastructure/CI-CD** | 1 task |
| **Total Test Files** | 25+ |
| **Target Code Coverage** | 80%+ on business logic |
| **Estimated Effort** | 3-4 weeks (full team) |

---

## Task Dependencies & Critical Path

```
T001-T006 (Phase 1: Setup) [CRITICAL]
    ↓
T007-T014 (Phase 2: Foundations) [CRITICAL]
    ↓
T015-T021 (Phase 3: Consumer) [CRITICAL]
    ↓
T022-T028 (Phase 4: Loan) [CRITICAL]
    ├─ Parallel: T029-T034 (Phase 5: EMI) [P1]
    ↓
T035-T038 (Phase 6: Repayment) [CRITICAL]
    ↓
T039-T041 (Phase 7: Vendor & Health)

Critical path items MUST complete in sequence before downstream phases.
Non-critical items (P2, P3) can proceed in parallel with critical path.
```

---

## Testing Strategy

### Unit Tests (Per Task)
- ✅ Service layer: MockMvc, Mockito for dependencies
- ✅ Repository layer: H2 in-memory database, @DataJpaTest
- ✅ Controller layer: MockMvc, @WebMvcTest
- ✅ Target: 80%+ coverage on all business logic

### Integration Tests (Per Story)
- ✅ T021: Consumer onboarding flow (Create → Link Account)
- ✅ T028: Loan lifecycle (Create → Approve → Disburse)
- ✅ T034: EMI calculation consistency
- ✅ T038: Repayment flow (Payment → Balance → Closure)
- ✅ MySQL test container for real database
- ✅ TestRestTemplate for API calls

### Performance Tests
- ✅ EMI calculation: < 50ms per calculation
- ✅ Standard API: < 1000ms (95th percentile)
- ✅ Health check: < 100ms
- ✅ JMeter or similar for load testing

---

## Quality Gates

Each phase must pass before proceeding:

✅ **Phase Gate Requirements**:
1. All unit tests pass (100% of test cases)
2. Code coverage ≥ 80% on new code
3. No compiler warnings (`mvn clean compile`)
4. Integration tests pass
5. Performance requirements met
6. SonarQube quality gate passed (if configured)
7. Security scan passed (OWASP)
8. Code review approved

---

## Deliverables Summary

By completion of Phase 7:

✅ **8 REST API Endpoints** - All 8 endpoints implemented and tested
✅ **7 Domain Entities** - Consumer, Loan, Repayment, Account, Vendor, AuditLog
✅ **5+ Services** - Business logic fully encapsulated
✅ **8+ Controllers** - API endpoints with full CRUD operations
✅ **25+ Test Classes** - Unit and integration tests
✅ **80%+ Code Coverage** - On all business logic
✅ **Complete Documentation** - OpenAPI/Swagger, quickstart, architecture
✅ **Docker Support** - Containerized application via Jib
✅ **CI/CD Pipeline** - Automated testing and deployment
✅ **Performance Validated** - All endpoints meet SLA requirements

---

**Ready to Begin Implementation**: ✅

Choose Sprint structure (weekly phases) and assign team members to parallel tracks.

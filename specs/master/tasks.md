# Tasks: Consumer Finance Loan Management System

**Status**: Phase 2 (Implementation Planning) - Ready for Development  
**Framework**: Spring Boot 3.2.0, Java 17, Maven 3.9.6, MySQL 8.0  
**Prerequisites**: spec.md ✓, plan.md ✓, data-model.md ✓, contracts.md ✓  
**Input**: Detailed requirements from user specification for 4 entities and 3 APIs

---

## Task Format

Each task follows strict format:
```
- [ ] [TaskID] [P?] [US?] Description with file path
```

- **TaskID**: Sequential identifier (T001, T002, etc.)
- **[P]**: Parallelizable marker (can run simultaneously)
- **[US?]**: User story label (US1, US2, US3, etc.) - ONLY for story phase tasks
- **Description**: Clear action with exact file path

---

## Phase 1: Setup & Configuration (Hours 0-4)

**Purpose**: Maven/Spring Boot configuration, package structure, build verification  
**Duration**: 3-4 hours  
**Checkpoint**: `mvn clean compile` succeeds with 0 warnings

### Build & Dependencies

- [ ] T001 Verify pom.xml parent is spring-boot-starter-parent 3.2.0
- [ ] T002 [P] Verify Java version 17 in pom.xml (maven.compiler.source/target)
- [ ] T003 [P] Verify MySQL Connector/J 8.0.33 dependency in pom.xml
- [ ] T004 [P] Add Flyway 9.x (flyway-core, flyway-mysql) to pom.xml
- [ ] T005 [P] Add Springdoc-OpenAPI 2.1.0 to pom.xml for Swagger/OpenAPI
- [ ] T006 [P] Add JUnit 5, Mockito test dependencies to pom.xml
- [ ] T007 Configure maven-compiler-plugin with showWarnings=true in pom.xml
- [ ] T008 Verify clean build: `mvn clean compile -q` produces 0 warnings

### Application Properties

- [ ] T009 [P] Create src/main/resources/application.properties for H2 development
  - spring.datasource.url=jdbc:h2:mem:testdb
  - spring.h2.console.enabled=true
  - spring.jpa.hibernate.ddl-auto=create-drop (for development)

- [ ] T010 [P] Create src/main/resources/application-mysql.properties for MySQL
  - spring.datasource.url=jdbc:mysql://localhost:3306/loan_management
  - spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
  - spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
  - spring.jpa.hibernate.ddl-auto=validate
  - spring.flyway.enabled=true

- [ ] T011 [P] Create src/main/resources/application-prod.properties for production
  - Production MySQL connection (with environment variable placeholders)
  - spring.jpa.hibernate.ddl-auto=validate
  - spring.flyway.locations=classpath:db/migration

### Logging Configuration

- [ ] T012 Create src/main/resources/logback.xml with:
  - JSON format for structured logging
  - Rolling file appender (daily rotation, 30-day retention)
  - Console appender for development
  - Different log levels: DEBUG for tests, INFO for production
  - Package-specific levels (Spring Web: INFO, Application: DEBUG)

### Project Structure

- [ ] T013 [P] Create directory structure:
  - src/main/java/com/consumerfinance/{domain,dto,repository,service,controller,exception,config}/
  - src/test/java/com/consumerfinance/{service,controller}/
  - src/main/resources/db/migration/

- [ ] T014 Create LoanManagementApplication.java in src/main/java/com/consumerfinance/
  - @SpringBootApplication annotation
  - @OpenAPIDefinition with API title, version, description
  - main() method with SpringApplication.run()

**Checkpoint**: Project structure ready, pom.xml configured, clean build verified

---

## Phase 2: Data Model - Entities & Persistence (Hours 4-12)

**Purpose**: Implement 4 JPA entities and Flyway migrations  
**Duration**: 6-8 hours  
**Checkpoint**: All entities mapped, migrations created, repositories functional

### Entity: Customer (Future Enhancement - Optional for MVP)

- [ ] T015 [US1] Design Customer entity (optional, can use String customerId for MVP):
  - PK: id (String: CUST001)
  - Fields: name, email, phone, kycStatus (PENDING/APPROVED/REJECTED), createdAt
  - **Decision**: Deferred to Phase 2b - use customerId string for MVP

### Entity: PersonalLoan (Core)

- [ ] T016 [US1] Create PersonalLoan JPA entity in src/main/java/com/consumerfinance/domain/PersonalLoan.java
  - @Entity, @Table(name = "personal_loans")
  - PK: id (Long, @GeneratedValue)
  - Fields:
    - customerId (String, @Column nullable=false) - FK reference
    - principalAmount (BigDecimal, precision=15 scale=2, nullable=false)
    - annualInterestRate (BigDecimal, precision=5 scale=2, nullable=false)
    - loanTenureMonths (Integer, nullable=false)
    - monthlyEMI (BigDecimal, precision=15 scale=2, nullable=false)
    - totalInterestPayable (BigDecimal, precision=15 scale=2, nullable=false)
    - outstandingBalance (BigDecimal, precision=15 scale=2, nullable=false)
    - remainingTenure (Integer, nullable=false)
    - status (Enum: PENDING, APPROVED, ACTIVE, CLOSED, REJECTED, DEFAULTED)
    - createdAt, approvedAt, rejectedAt, closedAt (LocalDateTime)
    - approvalRemarks, rejectionReason (String, optional)
  - @OneToMany relationship with LoanRepayment (cascade PERSIST, fetch LAZY)
  - Lombok @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
  - Indices: customer_id, status, created_at

- [ ] T017 [P] Create LoanStatus enum in same file with:
  - PENDING (initial state after application)
  - APPROVED (credit decision approved)
  - ACTIVE (approved, EMI schedule active)
  - CLOSED (fully repaid or closed by admin)
  - REJECTED (credit decision rejected)
  - DEFAULTED (critical delinquency)

### Entity: EMI Schedule Entry (Repayment)

- [ ] T018 [US3] Create LoanRepayment JPA entity in src/main/java/com/consumerfinance/domain/LoanRepayment.java
  - @Entity, @Table(name = "loan_repayments")
  - PK: id (Long, @GeneratedValue)
  - FK: loanId → PersonalLoan (ManyToOne, @JoinColumn, nullable=false, fetch LAZY)
  - Fields:
    - installmentNumber (Integer, nullable=false) - ordinal number (1, 2, 3...)
    - principalAmount (BigDecimal, precision=15 scale=2)
    - interestAmount (BigDecimal, precision=15 scale=2)
    - totalAmount (BigDecimal, precision=15 scale=2) - EMI amount
    - status (Enum: PENDING, PAID, PARTIALLY_PAID, OVERDUE, WAIVED)
    - dueDate (LocalDateTime, nullable=false)
    - paidDate (LocalDateTime, nullable)
    - paidAmount (BigDecimal, nullable) - actual paid amount
    - paymentMode (String: ONLINE, CHEQUE, CASH, etc.)
    - transactionReference (String) - payment reference/receipt number
    - createdAt (LocalDateTime)
  - @UniqueConstraint(columnNames = {"loan_id", "installment_number"})
  - Indices: loan_id, status, due_date, paid_date

- [ ] T019 [P] Create RepaymentStatus enum with:
  - PENDING (EMI due but not paid)
  - PAID (fully paid, paidAmount = totalAmount)
  - PARTIALLY_PAID (partial payment, 0 < paidAmount < totalAmount)
  - OVERDUE (dueDate passed, still PENDING or PARTIALLY_PAID)
  - WAIVED (forgiven/written-off)

### Repositories (Spring Data JPA)

- [ ] T020 [US1] Create PersonalLoanRepository in src/main/java/com/consumerfinance/repository/PersonalLoanRepository.java
  - Extend JpaRepository<PersonalLoan, Long>
  - Query methods:
    - List<PersonalLoan> findByCustomerId(String customerId)
    - List<PersonalLoan> findByCustomerIdAndStatus(String customerId, LoanStatus status)
    - Optional<PersonalLoan> findByIdAndCustomerId(Long id, String customerId)
    - Page<PersonalLoan> findByCustomerId(String customerId, Pageable pageable)

- [ ] T021 [US3] Create LoanRepaymentRepository in src/main/java/com/consumerfinance/repository/LoanRepaymentRepository.java
  - Extend JpaRepository<LoanRepayment, Long>
  - Query methods:
    - List<LoanRepayment> findByLoanId(Long loanId)
    - List<LoanRepayment> findByLoanIdAndStatus(Long loanId, RepaymentStatus status)
    - Optional<LoanRepayment> findByLoanIdAndInstallmentNumber(Long loanId, Integer number)
    - Page<LoanRepayment> findByLoanId(Long loanId, Pageable pageable)
    - List<LoanRepayment> findAllByStatusAndDueDateBefore(RepaymentStatus status, LocalDateTime date)

### Database Migrations (Flyway)

- [ ] T022 [P] Configure Flyway in pom.xml
  - Add flyway-core 9.x dependency
  - Add flyway-mysql 9.x dependency
  - Add maven-flyway-plugin for command-line migrations
  - Configure in application-mysql.properties:
    - spring.flyway.enabled=true
    - spring.flyway.locations=classpath:db/migration
    - spring.flyway.baseline-on-migrate=true

- [ ] T023 [P] Create db/migration directory structure
  - Create src/main/resources/db/migration/ directory
  - Create src/test/resources/db/migration/ for test-specific migrations

- [ ] T024 [P] Create V1__init_personal_loans.sql in src/main/resources/db/migration/
  ```sql
  CREATE TABLE personal_loans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id VARCHAR(50) NOT NULL,
    principal_amount DECIMAL(15,2) NOT NULL CHECK (principal_amount > 0 AND principal_amount <= 10000000),
    annual_interest_rate DECIMAL(5,2) NOT NULL CHECK (annual_interest_rate >= 0 AND annual_interest_rate <= 25),
    loan_tenure_months INT NOT NULL CHECK (loan_tenure_months >= 6 AND loan_tenure_months <= 360),
    monthly_emi DECIMAL(15,2) NOT NULL,
    total_interest_payable DECIMAL(15,2) NOT NULL,
    outstanding_balance DECIMAL(15,2) NOT NULL,
    remaining_tenure INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NULL,
    rejected_at TIMESTAMP NULL,
    closed_at TIMESTAMP NULL,
    approval_remarks TEXT NULL,
    rejection_reason TEXT NULL,
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
  ```

- [ ] T025 [P] Create V2__init_loan_repayments.sql in src/main/resources/db/migration/
  ```sql
  CREATE TABLE loan_repayments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    installment_number INT NOT NULL,
    principal_amount DECIMAL(15,2) NOT NULL,
    interest_amount DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL CHECK (total_amount > 0),
    status VARCHAR(20) DEFAULT 'PENDING',
    due_date TIMESTAMP NOT NULL,
    paid_date TIMESTAMP NULL,
    paid_amount DECIMAL(15,2) NULL,
    payment_mode VARCHAR(50) NULL,
    transaction_reference VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES personal_loans(id) ON DELETE RESTRICT,
    UNIQUE KEY unique_loan_installment (loan_id, installment_number),
    INDEX idx_loan_id (loan_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date),
    INDEX idx_paid_date (paid_date)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
  ```

- [ ] T026 [P] Create V3__create_audit_log.sql in src/main/resources/db/migration/
  ```sql
  CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    old_values JSON,
    new_values JSON,
    user_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
  ```

- [ ] T027 Verify Flyway executes on application startup
  - Run: `java -jar app.jar --spring.profiles.active=mysql`
  - Check logs: "Flyway v9.x migrating..."
  - Verify: `SHOW TABLES;` in MySQL (expect personal_loans, loan_repayments, flyway_schema_history)
  - Query: `SELECT * FROM flyway_schema_history;` (audit trail of migrations)

**Checkpoint**: Flyway migrations versioned, database schema created, audit trail established

---

## Phase 3: EMI Calculation Service (Hours 12-16)

**Purpose**: Implement core financial calculation engine  
**Duration**: 3-4 hours  
**Checkpoint**: EMI calculations accurate to 2 decimal places, validated

### EMI Calculation Logic (TDD - Tests First)

- [ ] T024 [US2] Create EMICalculationServiceTest in src/test/java/com/consumerfinance/service/EMICalculationServiceTest.java
  - **WRITE TESTS FIRST (TDD)**
  - Test Case 1: Standard inputs (500k, 10.5%, 60mo) → EMI ≈ 9638.22, total interest ≈ 78293.20
  - Test Case 2: Zero interest (500k, 0%, 60mo) → EMI = 500000/60
  - Test Case 3: Minimum inputs (1000, 0%, 6mo)
  - Test Case 4: Maximum inputs (10M, 25%, 360mo)
  - Test Case 5: Validation - negative amount → exception
  - Test Case 6: Validation - interest > 25% → exception
  - Test Case 7: Validation - tenure < 6 → exception
  - Test Case 8: Accuracy - verify 2 decimal place precision
  - Test Case 9: Formula accuracy - match standard financial calculator

- [ ] T025 [US2] Create EMICalculationService in src/main/java/com/consumerfinance/service/EMICalculationService.java
  - @Service annotation
  - @Slf4j for logging
  - Method: calculateEMI(BigDecimal principal, BigDecimal annualRate, Integer months) → EMIResult
  - Implementation:
    ```
    r = annualRate / 12 / 100  (monthly rate as decimal)
    n = months
    EMI = principal × r × (1+r)^n / ((1+r)^n - 1)
    totalInterest = EMI × n - principal
    totalAmount = principal + totalInterest
    ```
  - Use BigDecimal with HALF_UP rounding, scale=2
  - Validate: principal > 0, annualRate >= 0, months >= 6
  - Log calculation: principal, rate, tenure, resulting EMI

- [ ] T026 [US2] Create EMICalculationRequest DTO in src/main/java/com/consumerfinance/dto/EMICalculationRequest.java
  - Fields: principalAmount, annualInterestRate, tenureMonths
  - Validation: @NotNull, @DecimalMin, @DecimalMax, @Min, @Max

- [ ] T027 [US2] Create EMICalculationResponse DTO in src/main/java/com/consumerfinance/dto/EMICalculationResponse.java
  - Fields: monthlyEMI, totalInterest, totalAmount, principal, annualInterestRate, tenureMonths

**Checkpoint**: EMI tests passing, calculations accurate, service documented

---

## Phase 4: Loan Management Service (Hours 16-24)

**Purpose**: Loan lifecycle management (create, approve, reject, retrieve)  
**Duration**: 6-8 hours  
**Checkpoint**: All loan operations functional, business rules enforced

### Loan Service (TDD - Tests First)

- [ ] T028 [US1] Create PersonalLoanServiceTest in src/test/java/com/consumerfinance/service/PersonalLoanServiceTest.java
  - **WRITE TESTS FIRST (TDD)**
  - Test Case 1: Create loan - verify PENDING status, EMI calculated, schedule generated
  - Test Case 2: Get loan - verify retrieval or throws LoanNotFoundException
  - Test Case 3: Get customer loans - returns correct filtered list
  - Test Case 4: Approve loan - status PENDING → APPROVED, then to ACTIVE
  - Test Case 5: Reject loan - store reason, status REJECTED
  - Test Case 6: Outstanding balance updated after repayment
  - Test Case 7: Close loan - status → CLOSED when balance = 0
  - Test Case 8: Cannot close with outstanding balance
  - Test Case 9: Repayment schedule generated with N entries (1..tenure)
  - Test Case 10: Schedule principal+interest calculations correct

- [ ] T029 [US1] Create PersonalLoanService in src/main/java/com/consumerfinance/service/PersonalLoanService.java
  - @Service annotation, @Transactional
  - @Slf4j for logging
  - Dependency: PersonalLoanRepository, LoanRepaymentRepository, EMICalculationService

  - **Method: createLoan(CreateLoanRequest, String customerId)**
    - Validate input (amount, rate, tenure ranges)
    - Call EMICalculationService.calculateEMI()
    - Create PersonalLoan with status=PENDING
    - Save to DB
    - Call generateRepaymentSchedule(loan)
    - Log: "Loan created for customer {}, amount {}, EMI {}"
    - Return LoanResponse

  - **Method: approveLoan(Long loanId, String approvedBy, String remarks)**
    - Find loan by ID or throw LoanNotFoundException
    - Verify status == PENDING
    - Update status → APPROVED
    - Set approvedAt = now()
    - Set approvalRemarks
    - Generate schedule if not exists
    - Update status → ACTIVE (after approval)
    - Log: "Loan {} approved by {}"
    - Return LoanResponse

  - **Method: rejectLoan(Long loanId, String reason)**
    - Find loan or throw exception
    - Verify status == PENDING
    - Update status → REJECTED
    - Set rejectionReason
    - Set rejectedAt = now()
    - Log: "Loan {} rejected: {}"
    - Return LoanResponse

  - **Method: getLoan(Long loanId)**
    - Find by ID or throw LoanNotFoundException
    - Return LoanResponse

  - **Method: getLoansByCustomerId(String customerId, Pageable)**
    - Find all loans for customer
    - Return paginated list

  - **Method: getActiveLoansByCustomerId(String customerId)**
    - Find loans with status IN (ACTIVE, PENDING, APPROVED)
    - Return list

  - **Method: closeLoan(Long loanId)**
    - Find loan or throw exception
    - Verify outstandingBalance == 0
    - Update status → CLOSED
    - Set closedAt = now()
    - Log: "Loan {} closed"
    - Return LoanResponse

  - **Method: generateRepaymentSchedule(PersonalLoan loan) - PRIVATE**
    - For i=1 to loanTenureMonths:
      - Calculate principal portion: EMI - (remainingPrincipal × monthlyRate)
      - Calculate interest portion: remainingPrincipal × monthlyRate
      - Create LoanRepayment record with status=PENDING
      - dueDate = approvalDate + (i months)
      - Save to DB

- [ ] T030 [US1] Create CreateLoanRequest DTO in src/main/java/com/consumerfinance/dto/CreateLoanRequest.java
  - Fields: customerId, principalAmount, annualInterestRate, loanTenureMonths
  - Validation annotations for all fields
  - Messages for each validation error

- [ ] T031 [US1] Create LoanResponse DTO in src/main/java/com/consumerfinance/dto/LoanResponse.java
  - Fields: id, customerId, principalAmount, annualInterestRate, loanTenureMonths, monthlyEMI, totalInterestPayable, outstandingBalance, remainingTenure, status, createdAt, approvedAt, rejectedAt, closedAt, approvalRemarks, rejectionReason

**Checkpoint**: Loan service fully functional, all CRUD operations working, schedule generated

---

## Phase 5: Repayment Processing Service (Hours 24-32)

**Purpose**: Payment processing, balance updates, transaction history  
**Duration**: 6-8 hours  
**Checkpoint**: Payments processed atomically, balances accurate

### Repayment Service (TDD - Tests First)

- [ ] T032 [US3] Create LoanRepaymentServiceTest in src/test/java/com/consumerfinance/service/LoanRepaymentServiceTest.java
  - **WRITE TESTS FIRST (TDD)**
  - Test Case 1: Full payment - status → PAID, balance updated
  - Test Case 2: Partial payment - status → PARTIALLY_PAID, balance updated
  - Test Case 3: Overpayment rejected - throws InvalidRepaymentException
  - Test Case 4: Already paid rejection - throws exception
  - Test Case 5: Negative amount rejected
  - Test Case 6: Non-existent repayment - throws exception
  - Test Case 7: Loan not ACTIVE - throws exception
  - Test Case 8: Multiple partial payments until PAID
  - Test Case 9: Transaction history persisted
  - Test Case 10: Next installment identified correctly

- [ ] T033 [US3] Create LoanRepaymentService in src/main/java/com/consumerfinance/service/LoanRepaymentService.java
  - @Service annotation, @Transactional(isolation = SERIALIZABLE for financial safety)
  - @Slf4j for logging
  - Dependency: LoanRepaymentRepository, PersonalLoanRepository

  - **Method: processRepayment(Long loanId, Integer installmentNum, BigDecimal amount, String paymentMode, String reference)**
    - Find loan by ID or throw LoanNotFoundException
    - Verify loan status == ACTIVE
    - Find repayment by loanId + installmentNum or throw exception
    - Validate amount > 0
    - Validate amount ≤ (totalAmount - already paid)
    - Update repayment:
      - status = (amount == due) ? PAID : PARTIALLY_PAID
      - paidDate = now()
      - paidAmount = amount
      - paymentMode, transactionReference
    - Update loan:
      - outstandingBalance -= amount
      - If outstandingBalance == 0: status → CLOSED, closedAt = now()
    - Count remaining PENDING repayments: remainingTenure = count
    - Save loan + repayment
    - Log: "Repayment processed: loanId={}, amount={}, status={}, remainingBalance={}"
    - Return RepaymentResponse

  - **Method: getRepaymentsByLoanId(Long loanId, Pageable)**
    - Find loan or throw exception
    - Get all repayments with pagination
    - Return with status, due date, paid date, amounts

  - **Method: getPendingRepaymentsByLoanId(Long loanId)**
    - Find loan or throw exception
    - Get repayments with status IN (PENDING, PARTIALLY_PAID, OVERDUE)
    - Return list sorted by dueDate

  - **Method: getOverdueRepayments()**
    - Query: dueDate < now(), status != PAID
    - Return all overdue installments system-wide

  - **Method: markOverdueRepayments() - SCHEDULED TASK (optional)**
    - Query repayments with dueDate < now() and status = PENDING
    - Update status → OVERDUE
    - Log count

- [ ] T034 [US3] Create ProcessRepaymentRequest DTO in src/main/java/com/consumerfinance/dto/ProcessRepaymentRequest.java
  - Fields: installmentNumber, paidAmount, paymentMode, transactionReference, remarks
  - Validation: @NotNull for required fields

- [ ] T035 [US3] Create RepaymentResponse DTO in src/main/java/com/consumerfinance/dto/RepaymentResponse.java
  - Fields: id, loanId, installmentNumber, principalAmount, interestAmount, totalAmount, paidAmount, status, dueDate, paidDate, paymentMode, transactionReference, remarks, nextDueDate, remainingBalance

### Exception Handling

- [ ] T036 [P] Create custom exceptions in src/main/java/com/consumerfinance/exception/
  - LoanNotFoundException(message) extends RuntimeException
  - InvalidLoanOperationException(message) extends RuntimeException
  - InvalidRepaymentException(message) extends RuntimeException
  - IneligibleCustomerException(message) extends RuntimeException

- [ ] T037 Create GlobalExceptionHandler in src/main/java/com/consumerfinance/exception/GlobalExceptionHandler.java
  - @RestControllerAdvice annotation
  - @ExceptionHandler methods for each custom exception:
    - LoanNotFoundException → 404 with message
    - InvalidLoanOperationException → 400 with message
    - InvalidRepaymentException → 400 with message
    - MethodArgumentNotValidException → 400 with field errors
    - Exception (generic) → 500 with generic message
  - Error response DTO: error, message, details[], timestamp, path

**Checkpoint**: Repayment processing atomic, balances verified, exceptions handled

---

## Phase 6: REST Controllers & API Endpoints (Hours 32-42)

**Purpose**: Expose all APIs via REST endpoints with proper validation and OpenAPI documentation  
**Duration**: 8-10 hours  
**Checkpoint**: All 8+ endpoints functional, documented, tested

### Loan Controller

- [ ] T038 [US1] Create LoanController in src/main/java/com/consumerfinance/controller/LoanController.java
  - @RestController, @RequestMapping("/api/v1/loans"), @Slf4j

  - **POST /api/v1/loans - Apply for Loan**
    - Accept CreateLoanRequest in @RequestBody
    - Call personalLoanService.createLoan()
    - Return 200 with LoanResponse
    - Add @PostMapping, @Valid, @RequestBody
    - Add @Operation(summary="Apply for personal loan")
    - Add @ApiResponse(responseCode="200"), @ApiResponse(responseCode="400")
    - Log request and response

  - **GET /api/v1/loans/{loanId} - Get Loan by ID**
    - @PathVariable Long loanId
    - Call personalLoanService.getLoan(loanId)
    - Return 200 with LoanResponse or 404
    - Add @GetMapping("/{loanId}"), @PathVariable
    - Add OpenAPI annotations

  - **GET /api/v1/customers/{customerId}/loans - Get Customer Loans**
    - @PathVariable String customerId
    - @RequestParam(defaultValue="0") int page
    - @RequestParam(defaultValue="20") int size
    - @RequestParam(required=false) String status
    - @RequestParam(defaultValue="createdAt") String sort
    - Call personalLoanService.getLoansByCustomerId()
    - Return 200 with Page<LoanResponse>
    - Add pagination, filtering, sorting support

  - **PUT /api/v1/loans/{loanId}/status - Approve/Reject Loan**
    - @PathVariable Long loanId
    - Accept status update request: {newStatus, approvedBy/reason, remarks}
    - Call service.approveLoan() or service.rejectLoan()
    - Return 200 with updated LoanResponse or 400 if invalid transition
    - Add authorization header check (admin role - future)

### EMI Calculator Controller

- [ ] T039 [US2] Create EMICalculationController in src/main/java/com/consumerfinance/controller/EMICalculationController.java
  - @RestController, @RequestMapping("/api/v1/emi"), @Slf4j

  - **POST /api/v1/emi/calculate - Calculate EMI**
    - Accept EMICalculationRequest
    - Call emiCalculationService.calculateEMI()
    - Return 200 with EMICalculationResponse
    - Add @PostMapping("/calculate"), @Valid, @RequestBody
    - Add OpenAPI: @Operation(summary="Calculate EMI for loan parameters")
    - Log: "EMI calculated: amount={}, rate={}, tenure={}, EMI={}"

### Repayment Controller

- [ ] T040 [US3] Create RepaymentController in src/main/java/com/consumerfinance/controller/RepaymentController.java
  - @RestController, @RequestMapping("/api/v1/loans/{loanId}/repayments"), @Slf4j

  - **POST /api/v1/loans/{loanId}/repayments - Process Repayment**
    - Accept ProcessRepaymentRequest
    - Call loanRepaymentService.processRepayment()
    - Return 200 with RepaymentResponse or 409 if already paid
    - Add @PostMapping, @PathVariable, @Valid
    - Log: "Repayment received: loanId={}, installment={}, amount={}"

  - **GET /api/v1/loans/{loanId}/repayments - Get Repayment Schedule**
    - @PathVariable Long loanId
    - @RequestParam(defaultValue="0") int page
    - @RequestParam(defaultValue="50") int size
    - @RequestParam(required=false) String status
    - Call loanRepaymentService.getRepaymentsByLoanId()
    - Return 200 with Page<RepaymentResponse>
    - Include pagination, filtering

  - **GET /api/v1/loans/{loanId}/repayments/pending - Get Pending Installments**
    - @PathVariable Long loanId
    - Call loanRepaymentService.getPendingRepaymentsByLoanId()
    - Return 200 with List<RepaymentResponse>

  - **GET /api/v1/repayments/overdue - Get All Overdue Repayments (ADMIN)**
    - Call loanRepaymentService.getOverdueRepayments()
    - Return 200 with List<RepaymentResponse>
    - Add authorization check for ADMIN role (future)

### OpenAPI Configuration

- [ ] T041 Create OpenApiConfig in src/main/java/com/consumerfinance/config/OpenApiConfig.java
  - @Configuration class
  - @Bean public OpenAPI customOpenAPI()
    - Set title: "Consumer Finance Loan Management"
    - Set version: "1.0.0"
    - Set description with API capabilities
    - Expose /v3/api-docs (JSON)
    - Swagger UI at /swagger-ui.html

**Checkpoint**: All 8+ endpoints working, Swagger UI accessible, contracts verified

---

## Phase 7: Unit & Integration Tests (Hours 42-54)

**Purpose**: Comprehensive test coverage (80%+ for business logic)  
**Duration**: 8-12 hours  
**Target**: All tests passing, coverage verified

### Unit Tests (Already written in Phase 3-5 TDD)

- [ ] T042 Run all existing unit tests: EMI, Loan, Repayment services
  - Verify all tests pass: `mvn clean test`
  - Check coverage with JaCoCo: `mvn jacoco:report`
  - Target ≥ 80% for service layer

### Integration Tests

- [ ] T043 [P] Create LoanControllerIntegrationTest in src/test/java/com/consumerfinance/controller/
  - @SpringBootTest, @AutoConfigureMockMvc or TestRestTemplate
  - Use @DataJpaTest + @Service combo or full app context
  - Test Case 1: POST /api/v1/loans with valid data → 200
  - Test Case 2: GET /api/v1/loans/{id} → 200 or 404
  - Test Case 3: GET /api/v1/customers/{cid}/loans → 200 with list
  - Test Case 4: PUT /api/v1/loans/{id}/status → approval flow
  - Test Case 5: Invalid request → 400 with error details
  - Verify database state after each operation

- [ ] T044 [P] Create EMICalculationControllerIntegrationTest
  - Test POST /api/v1/emi/calculate with various inputs
  - Verify response accuracy (2 decimal places)
  - Verify no database writes (stateless API)

- [ ] T045 [P] Create RepaymentControllerIntegrationTest
  - Test Case 1: Create loan + process repayment → balance updated
  - Test Case 2: Full payment → status CLOSED
  - Test Case 3: Partial payment → status PARTIALLY_PAID
  - Test Case 4: Overpayment → 400 error
  - Test Case 5: Already paid → 409 conflict
  - Verify database balance matches calculation

- [ ] T046 Create LoanLifecycleIntegrationTest
  - E2E test: Create → Approve → Activate → Make repayment → Close
  - Verify all state transitions correct
  - Verify balance calculations accurate throughout

### Performance Tests (Manual/Benchmark)

- [ ] T047 [P] Create performance test
  - EMI calculation: measure response time (target < 100ms)
  - Loan creation with schedule (60 EMIs): measure time
  - Repayment processing: measure time
  - Document baseline metrics

**Checkpoint**: 80%+ test coverage, all tests passing, performance baselines documented

---

## Phase 8: Logging & Observability (Hours 54-58)

**Purpose**: Comprehensive audit and operational logging  
**Duration**: 3-4 hours  
**Checkpoint**: All significant operations logged and traceable

### Service Layer Logging

- [ ] T048 Add logging to PersonalLoanService
  - Log loan creation: "Loan application submitted by customer={}, principal={}, tenure={}"
  - Log EMI calculation: "EMI calculated: {}, total interest: {}"
  - Log approval: "Loan {} approved by {}"
  - Log rejection: "Loan {} rejected: reason={}"
  - Log closure: "Loan {} closed, outstanding balance: 0"
  - Use @Slf4j, log level INFO

- [ ] T049 Add logging to LoanRepaymentService
  - Log payment receipt: "Repayment received: loanId={}, amount={}, mode={}"
  - Log status updates: "Installment {} status updated to {}"
  - Log balance updates: "Outstanding balance updated to {}"
  - Log overdue marking: "Marked {} installments as overdue"
  - Log warnings for edge cases

- [ ] T050 Add logging to GlobalExceptionHandler
  - Log exceptions: level ERROR with full stack trace
  - Include request path, timestamp, error details
  - Don't log sensitive data (PII)

### Correlation IDs & Tracing

- [ ] T051 [P] Create CorrelationIdFilter (optional, advanced)
  - Add X-Correlation-ID header support
  - Generate UUID if not present
  - Add to MDC for thread-local logging context
  - Include in all logs and responses

### Logback Configuration

- [ ] T052 Verify logback.xml has:
  - JSON formatted output for production
  - Rolling file appender (daily, 30-day retention)
  - Async appender for performance
  - Different levels per package

**Checkpoint**: All operations auditable, logs structured and searchable

---

## Phase 9: Documentation & Deployment (Hours 58-66)

**Purpose**: Complete documentation and deployment readiness  
**Duration**: 6-8 hours  
**Checkpoint**: README complete, JAR deployable

### Documentation

- [ ] T053 [P] Create README.md in repository root
  - Project description and capabilities
  - Tech stack: Spring Boot 3.2.0, Java 17, MySQL 8.0, Maven 3.9.6
  - Quick start: Prerequisites, build, run
  - API overview table (3 APIs, 8+ endpoints)
  - Database setup: H2 (dev), MySQL (prod)
  - Configuration profiles
  - Examples: curl commands for each endpoint
  - Swagger UI access
  - Logging & monitoring
  - Troubleshooting

- [ ] T054 [P] Create DEPLOYMENT.md
  - MySQL setup scripts
  - Environment variables (DB connection, ports)
  - Build for production: `mvn clean package -Pprod`
  - JAR execution with MySQL
  - Docker setup (optional)
  - Health check endpoints
  - Rollback procedures
  - Backup & restore

- [ ] T055 [P] Create API_EXAMPLES.md
  - Curl examples for all 8+ endpoints
  - Request/response for each with real values
  - Error cases and handling
  - Postman collection JSON (for import)

- [ ] T056 [P] Create DEVELOPMENT.md
  - Local setup with H2
  - Running tests: `mvn clean test`
  - Code coverage check
  - Code organization (packages, layers)
  - Adding new features (step-by-step)
  - Debugging: logging, breakpoints
  - IDE setup (IntelliJ/Eclipse tips)

- [ ] T057 [P] Create ARCHITECTURE.md
  - Component diagram (Controller → Service → Repository → Entity)
  - Entity relationships (ER diagram description)
  - API flow (request → validation → business logic → response)
  - Transaction boundaries
  - Error handling strategy

### Build & Package

- [ ] T058 Verify clean compilation
  - `mvn clean compile -q` → 0 warnings (final gate)
  - All deprecation warnings resolved
  - No security vulnerabilities in dependencies

- [ ] T059 Verify test suite
  - `mvn clean test` → all tests passing
  - `mvn jacoco:report` → coverage ≥ 80%
  - Generate coverage HTML report

- [ ] T060 Create production JAR
  - `mvn clean package`
  - JAR size reasonable (~60-100MB)
  - Test JAR locally: `java -jar target/loan-management-system-1.0.0.jar`
  - Verify startup: < 10 seconds
  - Hit health endpoint: http://localhost:8080/actuator/health (future)

### Final Verification

- [ ] T061 [P] Manual API verification (all 8 endpoints)
  - Create loan (POST /api/v1/loans)
  - Retrieve loan (GET /api/v1/loans/{id})
  - List customer loans (GET /api/v1/customers/{cid}/loans)
  - Approve loan (PUT /api/v1/loans/{id}/status)
  - Calculate EMI (POST /api/v1/emi/calculate)
  - Process repayment (POST /api/v1/loans/{id}/repayments)
  - View schedule (GET /api/v1/loans/{id}/repayments)
  - View pending (GET /api/v1/loans/{id}/repayments/pending)
  - Verify all responses match contracts.md

- [ ] T062 [P] Database verification
  - Verify personal_loans table populated
  - Verify loan_repayments schedule generated
  - Verify payment transactions recorded
  - Check constraints enforce (overpayment rejected, etc.)

- [ ] T063 [P] Code quality checklist
  - All SOLID principles followed
  - No hardcoded values or credentials
  - Proper exception handling
  - DTOs properly validated
  - Services transactional
  - Logging comprehensive
  - Comments where complex

- [ ] T064 [P] Security checklist
  - No SQL injection (JPA prevents)
  - No XSS (JSON API)
  - Input validation all endpoints
  - Error responses generic (no PII leak)
  - Logs sanitized (no passwords, sensitive data)
  - Future: API authentication (JWT/OAuth ready)

**Checkpoint**: Deployment-ready JAR, documentation complete, all verification passed

---

## Task Summary Table

| Phase | Tasks | Hours | Deliverable |
|-------|-------|-------|------------|
| 1. Setup | T001-T014 | 3-4 | Clean build, configured |
| 2. Data Model | T015-T027 | 8-10 | Entities, migrations, Flyway audit |
| 3. EMI Service | T028-T031 | 3-4 | EMI calculations |
| 4. Loan Service | T032-T041 | 6-8 | Loan CRUD, lifecycle |
| 5. Repayment | T042-T051 | 6-8 | Payment processing |
| 6. Controllers | T052-T055 | 8-10 | REST endpoints |
| 7. Testing | T056-T061 | 8-12 | Tests, coverage |
| 8. Logging | T062-T066 | 3-4 | Audit logging |
| 9. Documentation | T067-T079 | 6-8 | Deploy-ready JAR |
| 10. Docker/Jib | T080-T098 | 6-8 | Containerized image, K8s-ready |
| **Total** | **98 tasks** | **60-76 hours** | **Production-ready microservice** |

---

## Parallelization Examples

### Fast Track: Single Developer
1. Phase 1 (setup) → Phase 2 (entities) → Phase 3-5 (services) → Phase 6 (controllers) → Phase 7-9 (tests, docs)
**Timeline**: 50-60 hours sequentially

### Team of 2-3 Developers
- **Dev 1**: Phase 1-2 (setup, entities) → Phase 3 (EMI service)
- **Dev 2**: Phase 4 (Loan service) in parallel with Dev 1
- **Dev 3**: Phase 5 (Repayment service) in parallel
- **All**: Phase 6-9 (controllers, tests, docs) after services complete
**Timeline**: 20-25 hours with parallelization

### Optimal Multi-Team
- **Team A**: Phases 1-2 (4 hours setup)
- **Team B**: Phase 3 (EMI) + Phase 4 (Loan) in parallel (12 hours)
- **Team C**: Phase 5 (Repayment) + Tests (TDD in parallel) (12 hours)
- **Team D**: Phase 6 (Controllers + OpenAPI) (10 hours)
- **Team E**: Phase 7-9 (Testing, Docs, Deployment) (8 hours)
**Timeline**: 12-15 hours critical path

---

## Quality Gates (Must Pass Each Phase)

- [ ] Phase 1: `mvn clean compile` → 0 warnings
- [ ] Phase 2: Migrations applied, tables created, repositories functional
- [ ] Phase 3: EMI tests passing, accuracy verified
- [ ] Phase 4: Loan service tests passing, transactions working
- [ ] Phase 5: Repayment tests passing, balance updates verified
- [ ] Phase 6: All endpoints returning correct status codes
- [ ] Phase 7: Coverage ≥ 80%, all tests passing
- [ ] Phase 8: All operations logged and traceable
- [ ] Phase 9: JAR deployable, documentation complete, final verification passed

---

**Version**: 2.0.0 (Updated with User Requirements) | **Status**: Ready for Development | **Last Updated**: 2026-02-25 | **Framework**: Spring Boot 3.2.0, Java 17, MySQL 8.0, Maven 3.9.6


## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure MUST complete before user story implementation  
**Duration Estimate**: 3-4 hours  
**Critical Checkpoint**: No story work starts until Phase 2 complete

- [ ] T007 [P] Create base domain package structure in src/main/java/com/consumerfinance/domain/
- [ ] T008 [P] Create PersonalLoan entity with JPA annotations in src/main/java/com/consumerfinance/domain/PersonalLoan.java
- [ ] T009 [P] Create LoanRepayment entity with JPA annotations in src/main/java/com/consumerfinance/domain/LoanRepayment.java
- [ ] T010 [P] Create PersonalLoanRepository interface in src/main/java/com/consumerfinance/repository/PersonalLoanRepository.java
- [ ] T011 [P] Create LoanRepaymentRepository interface in src/main/java/com/consumerfinance/repository/LoanRepaymentRepository.java
- [ ] T012 [P] Create custom exceptions (LoanNotFoundException, InvalidLoanOperationException, InvalidRepaymentException) in src/main/java/com/consumerfinance/exception/
- [ ] T013 Create GlobalExceptionHandler with @RestControllerAdvice in src/main/java/com/consumerfinance/exception/GlobalExceptionHandler.java
- [ ] T014 [P] Create base DTOs package and common response structures in src/main/java/com/consumerfinance/dto/
- [ ] T015 Create REST controller base packages in src/main/java/com/consumerfinance/controller/
- [ ] T016 Setup Spring Data JPA configuration and datasource in application.properties
- [ ] T017 Verify foundational build: `mvn clean compile` must pass with zero warnings

**Checkpoint**: Foundation complete - all packages exist, entities persisted, repositories queryable

---

## Phase 3: User Story 1 - Customer Applies for Personal Loan (Priority: P1) 🎯 MVP

**Goal**: Enable customers to apply for loans with automatic EMI calculation and repayment schedule generation  
**Independent Test**: Create a loan, verify EMI calculated, verify repayment schedule has 60 installments (for 60-month loan)  
**Success**: Loan created with ACTIVE status, outstanding balance = principal, next repayment in schedule visible

### Tests for User Story 1 (TDD - Write First)

- [ ] T018 [P] [US1] Unit test EMI calculation accuracy in src/test/java/com/consumerfinance/service/EMICalculationServiceTest.java
- [ ] T019 [P] [US1] Unit test loan creation validation in src/test/java/com/consumerfinance/service/PersonalLoanServiceTest.java
- [ ] T020 [US1] Integration test full loan creation flow in src/test/java/com/consumerfinance/controller/LoanControllerIntegrationTest.java

### Implementation for User Story 1

- [ ] T021 [P] [US1] Create EMICalculationService with amortization formula in src/main/java/com/consumerfinance/service/EMICalculationService.java
- [ ] T022 [P] [US1] Create CreateLoanRequest DTO with validation annotations in src/main/java/com/consumerfinance/dto/CreateLoanRequest.java
- [ ] T023 [P] [US1] Create LoanResponse DTO in src/main/java/com/consumerfinance/dto/LoanResponse.java
- [ ] T024 [US1] Create PersonalLoanService with createLoan() method in src/main/java/com/consumerfinance/service/PersonalLoanService.java
- [ ] T025 [US1] Implement repayment schedule generation in PersonalLoanService.generateRepaymentSchedule() 
- [ ] T026 [US1] Create LoanController with POST /api/v1/loans endpoint in src/main/java/com/consumerfinance/controller/LoanController.java
- [ ] T027 [US1] Add input validation for principal amount (1000-10000000), interest rate (1-25%), tenure (6-360 months)
- [ ] T028 [US1] Add comprehensive logging to loan creation workflow (service + controller)
- [ ] T029 [US1] Add OpenAPI/Swagger annotations to LoanController for API documentation

**Checkpoint**: Customers can create loans, EMI is calculated, repayment schedule exists in database

---

## Phase 4: User Story 2 - Calculate EMI for Loan Options (Priority: P1)

**Goal**: Provide standalone EMI calculator without creating loan, enable comparison shopping  
**Independent Test**: Call EMI calculator with 500000 principal, 10.5% rate, 60 months → verify monthly EMI ~9638.22  
**Success**: Response contains monthly EMI, total interest, total amount payable (all to 2 decimal places)

### Tests for User Story 2

- [ ] T030 [P] [US2] Unit test EMI calculation with multiple scenarios in EMICalculationServiceTest.java
- [ ] T031 [P] [US2] Integration test EMI API endpoint in src/test/java/com/consumerfinance/controller/EMICalculationControllerIntegrationTest.java

### Implementation for User Story 2

- [ ] T032 [P] [US2] Create EMICalculationRequest DTO in src/main/java/com/consumerfinance/dto/EMICalculationRequest.java
- [ ] T033 [P] [US2] Create EMICalculationResponse DTO in src/main/java/com/consumerfinance/dto/EMICalculationResponse.java
- [ ] T034 [US2] Create EMICalculationController with POST /api/v1/emi/calculate endpoint in src/main/java/com/consumerfinance/controller/EMICalculationController.java
- [ ] T035 [US2] Add input validation (same as loan: principal 1000-10000000, rate 1-25%, tenure 6-360)
- [ ] T036 [US2] Add error handling for invalid inputs with clear messages
- [ ] T037 [US2] Add OpenAPI annotations to EMICalculationController
- [ ] T038 [US2] Add structured logging to EMI calculation service

**Checkpoint**: Users can calculate EMI for multiple scenarios without creating loans; accurate to 2 decimal places

---

## Phase 5: User Story 3 - Customer Makes Loan Repayment (Priority: P1)

**Goal**: Enable customers to make payments against loans, track status, update outstanding balance  
**Independent Test**: Create loan with first installment, process 50% payment, verify PARTIALLY_PAID status, verify balance reduced  
**Success**: Payment recorded, repayment status updated, loan balance decreased, audit log created

### Tests for User Story 3

- [ ] T039 [P] [US3] Unit test repayment processing logic in src/test/java/com/consumerfinance/service/LoanRepaymentServiceTest.java
- [ ] T040 [P] [US3] Unit test outstanding balance calculation after payment
- [ ] T041 [US3] Integration test repayment API endpoint in src/test/java/com/consumerfinance/controller/RepaymentControllerIntegrationTest.java

### Implementation for User Story 3

- [ ] T042 [P] [US3] Create LoanRepaymentService with processRepayment() method in src/main/java/com/consumerfinance/service/LoanRepaymentService.java
- [ ] T043 [P] [US3] Create RepaymentResponse DTO in src/main/java/com/consumerfinance/dto/RepaymentResponse.java
- [ ] T044 [US3] Create RepaymentController with POST /api/v1/loans/{loanId}/repayment endpoint in src/main/java/com/consumerfinance/controller/RepaymentController.java
- [ ] T045 [US3] Implement payment validation (amount > 0, installment exists, not already paid)
- [ ] T046 [US3] Update loan outstanding balance and remaining tenure after payment
- [ ] T047 [US3] Implement transaction handling for atomic payment processing
- [ ] T048 [US3] Add comprehensive audit logging (who, when, amount, status change)
- [ ] T049 [US3] Add OpenAPI annotations to RepaymentController

**Checkpoint**: Customers can make payments, balances update correctly, audit trail created

---

## Phase 6: User Story 4 - View Loan Details and Repayment Schedule (Priority: P2)

**Goal**: Customers retrieve their loan information and complete repayment schedule  
**Independent Test**: Retrieve loan details, verify all fields correct; retrieve repayment schedule, verify all 60 installments with correct amounts and dates  
**Success**: API returns complete loan details and repayment history with status

### Tests for User Story 4

- [ ] T050 [P] [US4] Integration test GET /api/v1/loans/{loanId} endpoint in LoanControllerIntegrationTest.java
- [ ] T051 [P] [US4] Integration test repayment schedule retrieval in RepaymentControllerIntegrationTest.java

### Implementation for User Story 4

- [ ] T052 [US4] Create PersonalLoanService.getLoan() method to retrieve loan by ID
- [ ] T053 [US4] Create PersonalLoanService.getLoansByCustomerId() method for customer's all loans
- [ ] T054 [US4] Create LoanController GET /api/v1/loans/{loanId} endpoint
- [ ] T055 [US4] Create LoanController GET /api/v1/customers/{customerId}/loans endpoint
- [ ] T056 [US4] Create LoanRepaymentService.getRepaymentsByLoanId() method
- [ ] T057 [US4] Create LoanRepaymentService.getPendingRepaymentsByLoanId() method for upcoming due
- [ ] T058 [US4] Create RepaymentController GET /api/v1/loans/{loanId}/repayments endpoint
- [ ] T059 [US4] Create RepaymentController GET /api/v1/loans/{loanId}/repayments/pending endpoint
- [ ] T060 [US4] Add pagination support for large repayment schedules
- [ ] T061 [US4] Add OpenAPI annotations for retrieval endpoints

**Checkpoint**: All loan and repayment details retrievable with proper filtering and pagination

---

## Phase 7: User Story 5 - Admin Views Overdue Repayments (Priority: P3)

**Goal**: Enable admin users to identify and manage overdue repayments for collection  
**Independent Test**: Create loan with past-due date, query overdue endpoint, verify returned correctly  
**Success**: Overdue report shows customers with missed payments

### Tests for User Story 5

- [ ] T062 [US5] Integration test overdue repayment query in RepaymentControllerIntegrationTest.java

### Implementation for User Story 5

- [ ] T063 [US5] Create LoanRepaymentService.getOverdueRepayments() method
- [ ] T064 [US5] Create scheduled task to mark repayments OVERDUE if due date passed (optional: Spring @Scheduled)
- [ ] T065 [US5] Create RepaymentController GET /api/v1/repayments/overdue endpoint (admin-only)
- [ ] T066 [US5] Add authorization check for admin role on overdue endpoint
- [ ] T067 [US5] Add OpenAPI annotation marking endpoint as admin-only

**Checkpoint**: Admin dashboard can identify overdue repayments for collection activities

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Finalize documentation, performance, security, and deployment readiness  
**Duration Estimate**: 2-3 hours

- [ ] T068 [P] Create comprehensive README.md with project overview, tech stack, setup instructions
- [ ] T069 [P] Create API documentation examples (curl commands for each endpoint)
- [ ] T070 [P] Add performance optimization: Enable query result caching for frequently accessed loans
- [ ] T071 [P] Configure security: Add Spring Security authentication (JWT or API Key) in src/main/java/com/consumerfinance/config/SecurityConfig.java
- [ ] T072 [P] Add CORS configuration for frontend integration
- [ ] T073 Create database migration scripts (optional for MVP) in src/main/resources/db/migration/
- [ ] T074 Add monitoring health check endpoint with Spring Boot Actuator configuration
- [ ] T075 Verify all code compiles: `mvn clean compile` must complete with zero warnings
- [ ] T076 Verify test suite: `mvn clean test` must pass all unit and integration tests
- [ ] T077 Verify code coverage: `mvn jacoco:report` shows 80%+ coverage for business logic
- [ ] T078 Generate OpenAPI spec: Verify `/v3/api-docs` endpoint returns complete OpenAPI 3.0 schema
- [ ] T079 Create deployment guide: Docker (optional), JAR execution, environment variables

**Checkpoint**: Production-ready application with full documentation

---

## Phase 10: Container Deployment with Google Jib (Hours 66-74)

**Purpose**: Containerize application for Kubernetes/cloud deployment without writing Dockerfile  
**Duration**: 6-8 hours  
**Checkpoint**: Multi-stage Docker image built, pushed to registry, deployable

### Jib Plugin Configuration

- [ ] T080 [P] Add Google Jib Maven plugin to pom.xml
  ```xml
  <plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>3.4.0</version>
    <configuration>
      <from>
        <image>openjdk:17-jdk-slim</image>
      </from>
      <to>
        <image>docker.io/${env.DOCKER_HUB_USER}/loan-management-system:${project.version}</image>
        <auth>
          <username>${env.DOCKER_HUB_USER}</username>
          <password>${env.DOCKER_HUB_TOKEN}</password>
        </auth>
      </to>
      <container>
        <jvmFlags>
          <jvmFlag>-XX:+UseG1GC</jvmFlag>
          <jvmFlag>-XX:MaxRAMPercentage=75</jvmFlag>
          <jvmFlag>-XX:+UseStringDeduplication</jvmFlag>
        </jvmFlags>
        <environment>
          <JAVA_TOOL_OPTIONS>-XX:+UseStringDeduplication</JAVA_TOOL_OPTIONS>
        </environment>
        <ports>
          <port>8080</port>
        </ports>
        <creationTime>USE_CURRENT_TIMESTAMP</creationTime>
        <workingDirectory>/app</workingDirectory>
      </container>
      <extraDirectories>
        <paths>
          <path>
            <from>src/main/docker/jib</from>
            <into>/</into>
          </path>
        </paths>
      </extraDirectories>
    </configuration>
  </plugin>
  ```

- [ ] T081 [P] Configure maven-compiler-plugin for reproducible builds
  - Set property: `<project.build.outputTimestamp>1000</project.build.outputTimestamp>` (epoch time)
  - Enables deterministic builds (same input → same output checksum)

### Docker Build Profiles

- [ ] T082 Create src/main/docker/jib/docker-entrypoint.sh (optional)
  - Custom startup script for environment variable handling
  - Fallback to default if not needed

- [ ] T083 [P] Verify local Docker build (development)
  ```bash
  mvn clean compile jib:dockerBuild
  docker images | grep loan-management-system
  docker run -p 8080:8080 loan-management-system:1.0.0
  curl http://localhost:8080/v3/api-docs
  ```

### Multi-Stage Optimization

- [ ] T084 [P] Verify Jib layers are optimized
  - Layer 1: Base image (openjdk:17-jdk-slim) - cached
  - Layer 2: Dependencies JAR (cached if pom.xml unchanged)
  - Layer 3: Application JAR (frequently changed)
  - Layer 4: Configuration & metadata
  - Expected image size: 150-180MB (vs 300MB+ with manual Docker)

- [ ] T085 [P] Measure build performance
  - First build: ~60-90 seconds
  - Subsequent builds: ~20-30 seconds (cached layers)
  - Image push: ~5-10 seconds to Docker Hub

### Production Build & Registry Push

- [ ] T086 [P] Create GitHub Actions workflow (optional) in .github/workflows/docker-build.yml
  - Trigger: Push to main branch
  - Build: `mvn clean package jib:build`
  - Push to Docker Hub automatically
  - Tag: `latest` and version tag (v1.0.0)

- [ ] T087 [P] Manual production build with environment variables
  ```bash
  export DOCKER_HUB_USER=your_dockerhub_username
  export DOCKER_HUB_TOKEN=your_dockerhub_personal_access_token
  mvn clean package jib:build
  # Automatically pushes to docker.io/your_username/loan-management-system:1.0.0
  ```

- [ ] T088 Verify pushed image in Docker Hub registry
  - Check registry: docker.io/username/loan-management-system
  - Verify tags: 1.0.0, latest
  - Check image size: 150-180MB (smaller than manual Docker)
  - Check layer count: 4-5 optimized layers

### Runtime Container Deployment

- [ ] T089 [P] Create docker-compose.yml for local MySQL + App testing
  ```yaml
  version: '3.8'
  services:
    mysql:
      image: mysql:8.0
      environment:
        MYSQL_ROOT_PASSWORD: root
        MYSQL_DATABASE: loan_management
      ports:
        - "3306:3306"
      healthcheck:
        test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
        timeout: 5s
        retries: 10
    
    app:
      image: docker.io/your_user/loan-management-system:1.0.0
      depends_on:
        mysql:
          condition: service_healthy
      environment:
        SPRING_PROFILES_ACTIVE: mysql
        SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/loan_management
        SPRING_DATASOURCE_USERNAME: root
        SPRING_DATASOURCE_PASSWORD: root
      ports:
        - "8080:8080"
      healthcheck:
        test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
        interval: 10s
        timeout: 3s
        retries: 3
  ```

- [ ] T090 [P] Test container deployment with docker-compose
  ```bash
  docker-compose up -d
  sleep 10  # Wait for MySQL + migrations
  curl http://localhost:8080/v3/api-docs
  curl -X POST http://localhost:8080/api/v1/loans \
    -H "Content-Type: application/json" \
    -d '{"customerId":"CUST001","principalAmount":500000,"annualInterestRate":10.5,"loanTenureMonths":60}'
  docker-compose logs app
  docker-compose down
  ```

### Kubernetes Deployment (Optional - Advanced)

- [ ] T091 Create k8s/deployment.yaml for Kubernetes deployment
  ```yaml
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: loan-management-system
  spec:
    replicas: 3
    selector:
      matchLabels:
        app: loan-management
    template:
      metadata:
        labels:
          app: loan-management
      spec:
        containers:
        - name: app
          image: docker.io/your_user/loan-management-system:1.0.0
          ports:
          - containerPort: 8080
          env:
          - name: SPRING_PROFILES_ACTIVE
            value: "mysql"
          - name: SPRING_DATASOURCE_URL
            valueFrom:
              configMapKeyRef:
                name: db-config
                key: url
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
  ```

- [ ] T092 Create k8s/service.yaml for Kubernetes service exposure
  ```yaml
  apiVersion: v1
  kind: Service
  metadata:
    name: loan-management-service
  spec:
    type: LoadBalancer
    ports:
    - port: 80
      targetPort: 8080
      protocol: TCP
    selector:
      app: loan-management
  ```

### Container Security & Best Practices

- [ ] T093 [P] Verify container security hardening
  - Base image: openjdk:17-jdk-slim (minimal attack surface, ~150MB)
  - No root user inside container (Jib handles non-root)
  - Read-only root filesystem (optional config)
  - Security scanning: `docker scan loan-management-system:1.0.0`

- [ ] T094 [P] Add health check endpoints for container orchestration
  - Create HealthController in src/main/java/com/consumerfinance/controller/HealthController.java
  - Endpoint: GET /actuator/health (liveness)
  - Endpoint: GET /actuator/health/readiness (readiness to receive traffic)
  - Verify dependencies: Database connectivity check

- [ ] T095 [P] Document environment variables for container runtime
  - SPRING_PROFILES_ACTIVE: mysql, prod (default: dev with H2)
  - SPRING_DATASOURCE_URL: jdbc:mysql://host:3306/db
  - SPRING_DATASOURCE_USERNAME: database user
  - SPRING_DATASOURCE_PASSWORD: database password
  - Optional: JAVA_OPTS for JVM tuning

### Final Verification & Documentation

- [ ] T096 [P] Create DOCKER.md deployment guide
  - Quick start: docker run command
  - docker-compose example
  - Environment variables reference
  - Image size & layer breakdown
  - Security considerations
  - Kubernetes deployment steps

- [ ] T097 Update README.md with Docker section
  - Add "Docker Deployment" section
  - Link to DOCKER.md
  - Quick docker run example
  - Prerequisites (Docker installed)

- [ ] T098 Verify complete containerization workflow
  - Build JAR: `mvn clean package` ✓
  - Build image: `mvn jib:dockerBuild` ✓
  - Run container: `docker run -p 8080:8080 ...` ✓
  - Test APIs: All endpoints functional ✓
  - Check logs: `docker logs <container-id>` ✓

**Checkpoint**: Containerized application deployable to Docker Hub, Kubernetes-ready with health checks

---

```
Phase 1: Setup → Phase 2: Foundational (blocking)
                         ↓
        Phase 3: US1 (P1) → Phase 4: US2 (P1) → Phase 5: US3 (P1) 
                        ↓                                    ↓
                Phase 6: US4 (P2) ──────────────────────────┘
                        ↓
        Phase 7: US5 (P3)
                        ↓
        Phase 8: Polish & Cross-Cutting
```

**Key Constraints**:
- Phases 1-2 must complete sequentially (foundation)
- US1, US2, US3 can be parallelized within phase (different files)
- US4 depends on US1-US3 completion
- US5 (admin feature) is optional for MVP
- Phase 8 can start after Phase 5 completion

---

## Parallel Execution Examples

### Parallel Option 1: Fast Track (Single Developer)
Execute sequentially: Phase 1 → Phase 2 → US1 → US2 → US3 → US4 → Polish

### Parallel Option 2: Team of 2-3 Developers
- **Dev 1**: Phase 1-2 (setup) → US1 (loan creation)
- **Dev 2**: US2 (EMI calculation - parallel with US1)
- **Dev 3**: US3 (repayment - after US1)
- **All**: US4 (view details), Phase 8 (polish)

### Parallel Option 3: Optimal Multi-Team
- **Team A (Backend)**: Phase 1-2 setup (1 hour) → Implement all services (4-6 hours)
- **Team B (API)**: Wait for Phase 2 → Implement all controllers in parallel (3-4 hours)
- **Team C (Testing)**: Write tests in parallel with Phase 3-7 implementation (TDD)
- **Team D (Documentation)**: Phase 8 polish and OpenAPI docs

---

## MVP (Minimum Viable Product) Scope

**Deliver Value Quickly**: Complete Phases 1-5 for functional MVP

```
Phases 1-2: Foundation (6-8 hours)
Phase 3: Loan Creation with EMI (US1) - 3-4 hours
Phase 4: EMI Calculator (US2) - 2-3 hours  
Phase 5: Repayment Processing (US3) - 3-4 hours
───────────────────────────────────────────────
MVP Complete: 14-19 hours

Optional Later: Phase 6-7 (view details, admin overdue) + Phase 8 (polish)
```

**MVP Deliverables**:
- ✅ Create loan with EMI calculation
- ✅ Calculate EMI standalone
- ✅ Process loan repayments
- ✅ Basic API documentation
- ✅ Comprehensive audit logging

**Deferred to Phase 2**:
- View loan history/details (Phase 6)
- Admin overdue management (Phase 7)
- Advanced security/monitoring (Phase 8)

---

## Build & Verification Checklist

- [ ] `mvn clean compile` → Zero warnings ✓
- [ ] `mvn clean test` → All tests pass ✓
- [ ] `mvn jacoco:report` → 80%+ coverage ✓
- [ ] `curl http://localhost:8080/v3/api-docs` → Valid OpenAPI ✓
- [ ] Loan creation API tested with valid/invalid inputs ✓
- [ ] EMI calculation verified against financial calculator ✓
- [ ] Repayment processing verified with balance updates ✓
- [ ] All database transactions are ACID-compliant ✓
- [ ] Structured logging contains request/response correlation IDs ✓
- [ ] README includes setup, API examples, and deployment guide ✓

---

**Version**: 1.0.0 | **Status**: Ready for Implementation | **Last Updated**: 2026-02-24

# Task Breakdown: Consumer Finance Loan Management System

**Feature**: Consumer Finance - Loan Management System  
**Project**: loan-management-system  
**Generated**: 2026-02-25  
**Status**: Ready for Implementation  

---

## Overview

This document breaks down the loan management system into 100 executable tasks organized by implementation phases. Each task is specific, actionable, and includes file paths for direct execution.

### Task Structure Format
- ✅ **Checkbox**: Task completion tracking
- **ID**: Sequential task identifier (T001, T002, etc.)
- **[P]**: Parallelizable (can run simultaneously with other [P] tasks)
- **[Story]**: Associated user story (US1, US2, US3, US4, US5)
- **Description**: Clear action with file paths

### Dependencies Map
```
Phase 1 (Setup) → Phase 2 (Foundational) → Phase 3 (US1) → Phase 4 (US2) 
                                            ↓
                                      Phase 5 (US3) → Phase 6 (US4)
                                            ↓
                                      Phase 7 (US5) → Phase 8 (Polish)
```

---

## Phase 1: Project Setup & Configuration

**Goal**: Initialize project structure, build configuration, and development environment  
**Duration**: 1-2 hours  
**Blockers**: None  

### Setup Tasks

- [ ] T001 Verify project structure and Maven build configuration in `pom.xml`
- [ ] T002 [P] Configure Spring Boot application.yml for development environment in `src/main/resources/application.yml`
- [ ] T003 [P] Configure MySQL test database properties in `src/main/resources/application-mysql.properties`
- [ ] T004 [P] Configure H2 test database properties in `src/main/resources/application.properties` for unit tests
- [ ] T005 [P] Setup Flyway database migration folder structure in `src/main/resources/db/migration/`
- [ ] T006 Configure Logback logging in `src/main/resources/logback.xml` with JSON output for structured logging
- [ ] T007 [P] Create GlobalExceptionHandler in `src/main/java/com/consumerfinance/config/GlobalExceptionHandler.java`
- [ ] T008 [P] Create ErrorResponse DTO in `src/main/java/com/consumerfinance/config/ErrorResponse.java` for standardized error responses
- [ ] T009 Create LoanManagementApplication main class in `src/main/java/com/consumerfinance/LoanManagementApplication.java`
- [ ] T010 [P] Verify Lombok builder annotations are properly configured in `pom.xml` with annotation processors
- [ ] T011 Verify Maven clean compile and test compilation succeed

**Phase 1 Completion Check**: 
- ✅ pom.xml properly configured with all dependencies
- ✅ All configuration files created
- ✅ Build succeeds without errors

---

## Phase 2: Foundational Infrastructure & Database

**Goal**: Implement database schema, entities, repositories, and core exception handling  
**Duration**: 3-4 hours  
**Blockers**: Requires Phase 1  
**Can Unblock**: All user story phases  

### Database & Entity Tasks

- [ ] T012 Create LoanStatus enum in `src/main/java/com/consumerfinance/domain/PersonalLoan.java` (part of PersonalLoan class)
- [ ] T013 [P] Create PersonalLoan JPA entity in `src/main/java/com/consumerfinance/domain/PersonalLoan.java` with @Builder(toBuilder=true) annotation
- [ ] T014 [P] Create LoanRepayment JPA entity in `src/main/java/com/consumerfinance/domain/LoanRepayment.java` with complete repayment tracking fields
- [ ] T015 [P] Create PersonalLoanRepository interface in `src/main/java/com/consumerfinance/repository/PersonalLoanRepository.java` extending JpaRepository
- [ ] T016 [P] Create LoanRepaymentRepository interface in `src/main/java/com/consumerfinance/repository/LoanRepaymentRepository.java` with custom find methods
- [ ] T017 Create V1__init_personal_loans.sql migration in `src/main/resources/db/migration/V1__init_personal_loans.sql`
- [ ] T018 Create V2__init_loan_repayments.sql migration in `src/main/resources/db/migration/V2__init_loan_repayments.sql` with foreign key to PersonalLoan
- [ ] T019 [P] Create V3__create_audit_log.sql migration in `src/main/resources/db/migration/V3__create_audit_log.sql` for audit logging

### Exception & Validation Tasks

- [ ] T020 [P] Create LoanNotFoundException in `src/main/java/com/consumerfinance/exception/LoanNotFoundException.java`
- [ ] T021 [P] Create InvalidLoanOperationException in `src/main/java/com/consumerfinance/exception/InvalidLoanOperationException.java`
- [ ] T022 [P] Create InvalidRepaymentException in `src/main/java/com/consumerfinance/exception/InvalidRepaymentException.java`
- [ ] T023 Update GlobalExceptionHandler in `src/main/java/com/consumerfinance/config/GlobalExceptionHandler.java` to handle all custom exceptions

### DTO Tasks

- [ ] T024 [P] Create CreateLoanRequest DTO in `src/main/java/com/consumerfinance/dto/CreateLoanRequest.java`
- [ ] T025 [P] Create LoanResponse DTO in `src/main/java/com/consumerfinance/dto/LoanResponse.java`
- [ ] T026 [P] Create EMICalculationRequest DTO in `src/main/java/com/consumerfinance/dto/EMICalculationRequest.java`
- [ ] T027 [P] Create EMICalculationResponse DTO in `src/main/java/com/consumerfinance/dto/EMICalculationResponse.java`
- [ ] T028 [P] Create RepaymentResponse DTO in `src/main/java/com/consumerfinance/dto/RepaymentResponse.java`

**Phase 2 Completion Check**:
- ✅ All database migrations execute successfully
- ✅ JPA entities map correctly to database tables
- ✅ Repositories are injectable and queryable
- ✅ Exception handling covers all error scenarios
- ✅ DTOs serialize/deserialize correctly

---

## Phase 3: User Story 1 - Personal Loan Application & Creation (P1)

**Story Goal**: Customers can apply for personal loans with EMI pre-calculation and confirmation  
**Independent Test**: Loan creation → Persistence → Retrieval  
**Duration**: 4-5 hours  
**Blockers**: Requires Phase 2  

### Service Layer Tasks

- [ ] T029 Create EMICalculationService in `src/main/java/com/consumerfinance/service/EMICalculationService.java` with `calculateEMI()` method using amortization formula
- [ ] T030 Create EMICalculationService unit tests in `src/test/java/com/consumerfinance/service/EMICalculationServiceTest.java` covering standard, edge cases, and boundary conditions
- [ ] T031 Run and verify all EMICalculationServiceTest tests pass

- [ ] T032 [US1] Create PersonalLoanService in `src/main/java/com/consumerfinance/service/PersonalLoanService.java` with `createLoan()` and `getLoan()` methods
- [ ] T033 [US1] Implement repayment schedule generation in PersonalLoanService `generateRepaymentSchedule()` method
- [ ] T034 [US1] Create PersonalLoanService unit tests in `src/test/java/com/consumerfinance/service/PersonalLoanServiceTest.java` for loan creation and retrieval
- [ ] T035 [US1] Run and verify all PersonalLoanServiceTest tests pass

### Controller Layer Tasks

- [ ] T036 [US1] Create PersonalLoanController in `src/main/java/com/consumerfinance/controller/PersonalLoanController.java` with POST /api/v1/loans endpoint
- [ ] T037 [US1] Create EMICalculationController in `src/main/java/com/consumerfinance/controller/EMICalculationController.java` with POST /api/v1/emi/calculate endpoint
- [ ] T038 [US1] Create controller tests in `src/test/java/com/consumerfinance/controller/PersonalLoanControllerTest.java` for loan creation API
- [ ] T039 [US1] Create controller tests in `src/test/java/com/consumerfinance/controller/EMICalculationControllerTest.java` for EMI calculation API
- [ ] T040 [US1] Create integration test for complete loan creation flow in `src/test/java/com/consumerfinance/LoanManagementApplicationIntegrationTest.java`

### Validation Tasks

- [ ] T041 [US1] Implement input validation for CreateLoanRequest (positive amount, valid tenure, interest rate range)
- [ ] T042 [US1] Implement input validation for EMICalculationRequest
- [ ] T043 [US1] Add validation error tests to PersonalLoanControllerTest

**Phase 3 Completion Check**:
- ✅ POST /api/v1/loans creates loan with calculated EMI
- ✅ Repayment schedule is generated with 60+ monthly installments
- ✅ POST /api/v1/emi/calculate returns accurate EMI, total interest, total payment
- ✅ All input validation tests pass
- ✅ Integration test confirms loan creation flow works end-to-end

---

## Phase 4: User Story 2 - EMI Calculation API (P1)

**Story Goal**: Customers can calculate EMI for different loan options before applying  
**Independent Test**: EMI calculation with various inputs vs. standard financial formulas  
**Duration**: 2-3 hours  
**Blockers**: Requires Phase 2 (EMI calculation logic already in Phase 3)  

### API Endpoint Tasks

- [ ] T044 [P] [US2] Implement GET /api/v1/emi/calculate endpoint in EMICalculationController (add support for query parameters as alternative to POST body)
- [ ] T045 [P] [US2] Add rate comparison endpoint GET /api/v1/emi/compare in EMICalculationController to calculate EMI for multiple rate scenarios
- [ ] T046 [US2] Create comprehensive EMI calculation tests covering:
  - Standard EMI calculation validation against financial calculator
  - Edge cases: 0% interest, very high rates, max/min tenure, min/max amounts
  - Performance test: EMI calculation returns < 100ms

**Phase 4 Completion Check**:
- ✅ EMI API returns accurate results consistent with standard calculators
- ✅ All edge cases handled gracefully with error messages
- ✅ Response times < 100ms for standard calculations

---

## Phase 5: User Story 3 - Loan Repayment Processing (P1)

**Story Goal**: Customers can make loan repayments, update balances, and track payment status  
**Independent Test**: Loan creation → Repayment submission → Balance update → Status verification  
**Duration**: 5-6 hours  
**Blockers**: Requires Phase 2 + Phase 3  

### Service Layer Tasks

- [ ] T047 [US3] Implement `processRepayment()` method in PersonalLoanService with balance update logic
- [ ] T048 [US3] Implement `validateRepayment()` method to check:
  - Loan exists and is ACTIVE
  - Payment amount > 0 and ≤ outstanding balance
  - Installment exists and is not already fully paid
- [ ] T049 [US3] Implement `updateOutstandingBalance()` method to atomically update balance and remaining tenure
- [ ] T050 [US3] Implement `updateLoanStatus()` method to change status to CLOSED when balance = 0
- [ ] T051 [US3] Implement `markInstallmentPaid()` method to update repayment status (PAID/PARTIALLY_PAID/OVERDUE)
- [ ] T052 [US3] Add @Transactional on repayment processing methods for ACID compliance
- [ ] T053 [US3] Create PersonalLoanService tests for repayment processing: valid payment, insufficient balance, duplicate payment, zero balance loan closure

### Controller Layer Tasks

- [ ] T054 [US3] Create LoanRepaymentController in `src/main/java/com/consumerfinance/controller/LoanRepaymentController.java`
- [ ] T055 [US3] Implement POST /api/v1/loans/{loanId}/repayments endpoint for payment submission
- [ ] T056 [US3] Implement GET /api/v1/loans/{loanId}/repayments endpoint to retrieve repayment schedule
- [ ] T057 [US3] Implement GET /api/v1/loans/{loanId}/pending-repayments endpoint to show pending installments
- [ ] T058 [US3] Create LoanRepaymentController tests covering valid/invalid payment scenarios

### Persistence & Audit Tasks

- [ ] T059 [US3] Create AuditLog entity in `src/main/java/com/consumerfinance/domain/AuditLog.java` to track loan operations
- [ ] T060 [US3] Create AuditLogRepository in `src/main/java/com/consumerfinance/repository/AuditLogRepository.java`
- [ ] T061 [US3] Implement audit logging service in `src/main/java/com/consumerfinance/service/AuditLogService.java` to log all loan transactions
- [ ] T062 [US3] Call audit service on loan creation, repayment, and status changes

### Integration Test Tasks

- [ ] T063 [US3] Create comprehensive integration test: create loan → retrieve pending repayments → submit payment → verify balance update → verify status if fully paid

**Phase 5 Completion Check**:
- ✅ Repayment submitted and balance updated atomically
- ✅ Loan status changes to CLOSED when fully repaid
- ✅ All repayment validations work correctly
- ✅ Audit logs capture all transactions
- ✅ Concurrent repayments handled correctly

---

## Phase 6: User Story 4 - Loan Details & Repayment Schedule Retrieval (P2)

**Story Goal**: Customers can view loan details, outstanding balance, and complete repayment schedule  
**Independent Test**: Loan retrieval with full repayment schedule after loan creation  
**Duration**: 2-3 hours  
**Blockers**: Requires Phase 3 + Phase 5  

### API Endpoint Tasks

- [ ] T064 [P] [US4] Implement GET /api/v1/loans/{loanId} endpoint in PersonalLoanController to retrieve complete loan details
- [ ] T065 [P] [US4] Implement GET /api/v1/customers/{customerId}/loans endpoint to list all loans for a customer
- [ ] T066 [P] [US4] Implement GET /api/v1/loans/{loanId}/repayments endpoint to retrieve full repayment schedule with status
- [ ] T067 [US4] Implement filtering/pagination for loan list endpoint (offset, limit)
- [ ] T068 [US4] Create tests for loan retrieval endpoints with various scenarios

### Response Formatting Tasks

- [ ] T069 [P] [US4] Update LoanResponse DTO to include:
  - Outstanding balance
  - Remaining tenure
  - Next payment due date
  - Current status
  - All historical repayment amounts
- [ ] T070 [US4] Create RepaymentScheduleResponse DTO with full installment details
- [ ] T071 [US4] Implement response serialization tests

**Phase 6 Completion Check**:
- ✅ GET /api/v1/loans/{loanId} returns complete loan details with accurate calculations
- ✅ GET /api/v1/loans/{loanId}/repayments returns all installments with current status
- ✅ Customer loan list shows all active and closed loans
- ✅ Pagination works correctly on large datasets

---

## Phase 7: User Story 5 - Admin Overdue Repayment Reporting (P3)

**Story Goal**: Admins can identify overdue repayments for collection management  
**Independent Test**: Create past-due loans and verify overdue report retrieval  
**Duration**: 2-3 hours  
**Blockers**: Requires Phase 5  

### Service Layer Tasks

- [ ] T072 [US5] Create OverdueReportService in `src/main/java/com/consumerfinance/service/OverdueReportService.java`
- [ ] T073 [US5] Implement `getOverdueRepayments()` method to query all past-due installments
- [ ] T074 [US5] Implement `calculateDaysOverdue()` method
- [ ] T075 [US5] Add scheduled task to mark repayments as OVERDUE when due date passes

### Controller Layer Tasks

- [ ] T076 [US5] Create ReportController in `src/main/java/com/consumerfinance/controller/ReportController.java`
- [ ] T077 [US5] Implement GET /api/v1/admin/overdue-repayments endpoint with filters:
  - Date range
  - Overdue days threshold
  - Customer ID (optional)
- [ ] T078 [US5] Create OverdueResponse DTO with customer, loan, and overdue amount details
- [ ] T079 [US5] Create ReportController tests for overdue report generation

### Scheduled Tasks

- [ ] T080 [US5] Implement @Scheduled task to run daily and mark past-due installments as OVERDUE status
- [ ] T081 [US5] Add configuration in application.yml for scheduled task timing

**Phase 7 Completion Check**:
- ✅ GET /api/v1/admin/overdue-repayments returns all past-due installments
- ✅ Scheduled task automatically marks overdue repayments
- ✅ Overdue report includes customer contact information
- ✅ Admin filtering by date range and days overdue works correctly

---

## Phase 8: Cross-Cutting Concerns & Polish

**Goal**: Security, observability, documentation, and production readiness  
**Duration**: 3-4 hours  
**Blockers**: Requires all user story phases  

### Security & Authentication Tasks

- [ ] T082 Implement Spring Security configuration in `src/main/java/com/consumerfinance/config/SecurityConfig.java`
- [ ] T083 Create JWT token provider for customer authentication
- [ ] T084 Implement role-based access control:
  - CUSTOMER: Can access own loans only
  - ADMIN: Can access all loans and overdue reports
  - STAFF: Can process repayments
- [ ] T085 Add @PreAuthorize annotations to controller methods for authorization checks
- [ ] T086 Create security tests for authentication and authorization

### Observability & Monitoring Tasks

- [ ] T087 [P] Configure Spring Boot Actuator endpoints in application.yml
- [ ] T088 [P] Implement correlation ID logging in GlobalExceptionHandler for request tracing
- [ ] T089 [P] Create health check endpoint (/actuator/health) with database connectivity check
- [ ] T090 [P] Configure application metrics collection (Spring Actuator Micrometer integration)
- [ ] T091 Create observability tests

### Documentation Tasks

- [ ] T092 Create API documentation using Springdoc-OpenAPI (auto-generated Swagger UI at /swagger-ui.html)
- [ ] T093 Add @ApiOperation, @ApiParam, @ApiResponse annotations to all controller endpoints
- [ ] T094 Create README.md with:
  - Project overview
  - Setup instructions
  - Running the application
  - API endpoint reference
  - Testing instructions
- [ ] T095 Create DEPLOYMENT.md with cloud deployment instructions

### Performance & Load Testing Tasks

- [ ] T096 Create load test in `src/test/java/com/consumerfinance/PerformanceTest.java` to verify:
  - API response times < 500ms p95
  - Concurrent user support (100+)
  - EMI calculation < 100ms
- [ ] T097 Run load tests and document results

### Final Integration & Smoke Tests

- [ ] T098 Run full integration test suite
- [ ] T099 Run end-to-end smoke test:
  - Create 3 loans with different parameters
  - Process payments on each
  - Verify balances and status changes
  - Query overdue repayments
- [ ] T100 Verify all migrations execute cleanly on fresh database

**Phase 8 Completion Check**:
- ✅ All endpoints require proper authentication
- ✅ Authorization prevents unauthorized access
- ✅ Structured logging with correlation IDs
- ✅ API documentation is complete and accurate
- ✅ Performance tests confirm < 500ms response time
- ✅ All integration tests pass

---

## Parallel Execution Examples

### Phase 2 (Parallel Opportunities):
- T013, T014 (Entities) - different files, no dependencies
- T015, T016 (Repositories) - after entities defined
- T020, T021, T022 (Exceptions) - independent
- T024-T028 (DTOs) - independent

### Phase 3 (Parallel Opportunities):
- T038, T039 (Controller tests) - after controllers implemented
- T041, T042, T043 (Validation) - independent

### Phase 8 (Parallel Opportunities):
- T087, T088, T089, T090 (Observability) - independent
- T092, T093, T094, T095 (Documentation) - different files

---

## Suggested MVP Scope (Minimum Viable Product)

**Recommended MVP**: Phase 1 + Phase 2 + Phase 3 + Phase 5 (Core Use Cases)

**Why this scope?**
- Phases 1-2: Foundation required for everything
- Phase 3: Primary revenue driver (loan creation)
- Phase 5: Critical business function (payment processing)
- **Phases 4, 6, 7, 8 can be delivered in subsequent releases**

**MVP Timeline**: 10-12 days (5-person team, 2 weeks sprint)

---

## Implementation Strategy

### Suggested 2-Week Sprint Execution

**Sprint 1 (Days 1-5)**:
- Phase 1: Setup (T001-T011) → 3-4 hours
- Phase 2: Foundational (T012-T028) → 3-4 hours
- Phase 3 (Start): EMI Service (T029-T031) → 2 hours

**Sprint 2 (Days 6-10)**:
- Phase 3 (Continue): Loan Service & Controllers (T032-T043) → 4-5 hours
- Phase 5 (Start): Repayment Service (T047-T062) → 3-4 hours

**Sprint 3 (Days 11-15)**:
- Phase 5 (Complete): Controllers & Integration (T054-T063) → 2-3 hours
- Phase 8 (Start): Security & Observability (T082-T089) → 2-3 hours

**Future Sprints**:
- Phase 4: EMI Comparison APIs → 1 sprint
- Phase 6: Loan Details APIs → 1 sprint
- Phase 7: Overdue Reports → 1 sprint
- Phase 8: Complete hardening → 1 sprint

---

## Task Completion Workflow

1. Complete Phase 1 (Setup)
2. Complete Phase 2 (Foundational) - Can parallelize after initial setup
3. Start Phase 3 (US1 - Loan Creation)
4. Start Phase 5 (US3 - Repayment) in parallel with Phase 3 after Phase 2 complete
5. Start Phases 4, 6, 7 when their blockers are complete
6. Phase 8 (Polish) last, after all core phases complete
7. MVP Ready for Production

---

## Success Criteria

- ✅ All Phase 1-2 tasks completed with green build
- ✅ All Phase 3 tests pass (loan creation, EMI calculation)
- ✅ All Phase 5 tests pass (repayment processing with zero failures)
- ✅ All integration tests pass end-to-end
- ✅ No compilation errors or warnings
- ✅ API response times consistently < 500ms (p95)
- ✅ Code coverage > 80% for service layer
- ✅ All mandatory security checks passed

---

**Version**: 1.0.0 (Complete Task Breakdown)  
**Last Updated**: 2026-02-25  
**Status**: Ready for Implementation  

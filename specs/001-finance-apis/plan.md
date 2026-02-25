# Implementation Plan: Consumer Finance Multi-API Platform

**Branch**: `001-finance-apis` | **Date**: February 25, 2026 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-finance-apis/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Build a comprehensive consumer finance REST API platform with 8 core endpoints supporting personal loan management: Consumer registration, loan application, EMI calculation, loan repayment processing, and operational management. Platform must validate all user input, maintain comprehensive audit trails, and achieve sub-second response times while managing critical financial transactions with ACID compliance through Spring Boot 3.2.0 microservices architecture backed by MySQL 8.0 with Flyway migrations and Google Jib containerization.

## Technical Context

**Language/Version**: Java 17 LTS  
**Framework**: Spring Boot 3.2.0 with Spring Data JPA  
**Build Tool**: Apache Maven 3.9.6  
**Primary Dependencies**: Spring Web, Spring Data JPA, Hibernate ORM, Flyway DB Migration, Spring Security, Springdoc-OpenAPI, Lombok, Maven Compiler Plugin, JUnit 5, Mockito  
**Storage**: MySQL 8.0 (default), ACID-compliant relational database with JPA/Hibernate ORM  
**Testing**: JUnit 5, Mockito, integration tests with TestNG  
**Target Platform**: Linux server (containerized via Google Jib)  
**Project Type**: Web service (REST API microservices)  
**Performance Goals**: 
- Standard API endpoints: < 1000ms (95th percentile)
- EMI calculation: < 500ms (99th percentile)
- Health checks: < 100ms
- Throughput: 1000+ concurrent requests without degradation
**Constraints**: 
- Sub-second response times for financial transactions
- ACID compliance for loan operations and repayments
- Input validation on all endpoints before business logic
- Comprehensive audit logging for compliance
- Backward-compatible API versioning (/api/v1/)
**Scale/Scope**: 
- 8 core API endpoints (Consumer, Principal Account, Vendor, Vendor Linked Account, Loan, EMI, Repayment, Health)
- 7 domain entities with complex relationships
- ~2500+ lines of code estimated
- Support for unlimited concurrent users through MySQL connection pooling

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Core Principles Alignment:**

✅ **I. Service-Driven Architecture**: Spring Boot 3.2.0 microservices with clear service boundaries (PersonalLoanService, EMICalculationService, LoanRepaymentService, etc.). Each service encapsulates domain logic with REST API contracts and error handling through @ControllerAdvice global exception handler.

✅ **II. RESTful API Design**: All 8 endpoints follow REST conventions with proper HTTP methods (POST for create, GET for read, PUT for update), consistent JSON payloads, OpenAPI/Swagger documentation via springdoc-openapi, and API versioning (/api/v1/) for backward compatibility.

✅ **III. Test-First Development (NON-NEGOTIABLE)**: Commitment to JUnit 5 + Mockito for unit tests with minimum 80% coverage on business logic (EMICalculationService, LoanRepaymentService, PersonalLoanService). Integration tests required for critical workflows: loan application, EMI calculation, repayment processing.

✅ **IV. Database Integrity & Transactions**: MySQL 8.0 with JPA/Hibernate ORM (no raw SQL). All financial transactions (loan creation, repayment processing) use @Transactional with proper rollback handling. Flyway manages schema migrations with version control. JpaRepository pattern used throughout.

✅ **V. Security & Compliance**: RBAC implementation via Spring Security, audit logging for PII/financial operations, encrypted password storage, request correlation IDs via MDC (Mapped Diagnostic Context) for traceability.

✅ **VI. Observability & Monitoring**: Structured logging via SLF4J/Logback at INFO/WARN/ERROR levels, correlation IDs for request tracing, health check endpoint (/actuator/health) validates database and dependencies.

**Technology Stack Verification**: Java 17 LTS ✅ | Spring Boot 3.2.0 ✅ | Maven 3.9.6 ✅ | MySQL 8.0 ✅ | Flyway ✅ | Google Jib ✅ | All constitution standards met.

**GATE STATUS**: ✅ **PASS** - No violations. All core principles aligned with proposed implementation.

## Project Structure

### Documentation (this feature)

```text
specs/001-finance-apis/
├── plan.md              # This file - Implementation planning
├── research.md          # Phase 0 output - Research findings
├── data-model.md        # Phase 1 output - Entity definitions and relationships
├── quickstart.md        # Phase 1 output - Development quick start guide
├── contracts/           # Phase 1 output - API contract definitions
│   ├── consumer-api.md
│   ├── principal-account-api.md
│   ├── vendor-api.md
│   ├── vendor-linked-account-api.md
│   ├── loan-api.md
│   ├── emi-calculation-api.md
│   ├── loan-repayment-api.md
│   └── health-api.md
├── spec.md              # Original feature specification
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/consumerfinance/
│   │   ├── LoanManagementApplication.java          # Spring Boot entry point
│   │   ├── config/
│   │   │   ├── SecurityConfig.java                 # Spring Security & RBAC
│   │   │   ├── OpenApiConfig.java                  # OpenAPI/Swagger setup
│   │   │   └── AuditLogAspect.java                 # AOP for audit logging
│   │   ├── controller/
│   │   │   ├── ConsumerController.java             # Consumer API endpoints
│   │   │   ├── PrincipalAccountController.java     # Principal Account API
│   │   │   ├── VendorController.java               # Vendor API endpoints
│   │   │   ├── VendorLinkedAccountController.java  # Vendor Linked Account API
│   │   │   ├── PersonalLoanController.java         # Loan API endpoints
│   │   │   ├── EMICalculationController.java       # EMI calculation endpoints
│   │   │   ├── LoanRepaymentController.java        # Repayment processing
│   │   │   └── HealthController.java               # Health check endpoint
│   │   ├── service/
│   │   │   ├── ConsumerService.java                # Consumer business logic
│   │   │   ├── PrincipalAccountService.java        # Account management
│   │   │   ├── VendorService.java                  # Vendor business logic
│   │   │   ├── VendorLinkedAccountService.java     # Linked account logic
│   │   │   ├── PersonalLoanService.java            # Loan processing logic
│   │   │   ├── EMICalculationService.java          # EMI calculation logic
│   │   │   ├── LoanRepaymentService.java           # Repayment processing
│   │   │   └── AuditLogService.java                # Audit trail management
│   │   ├── domain/
│   │   │   ├── Consumer.java                       # Consumer entity
│   │   │   ├── PrincipalAccount.java               # Account entity
│   │   │   ├── Vendor.java                         # Vendor entity
│   │   │   ├── VendorLinkedAccount.java            # Linked account entity
│   │   │   ├── PersonalLoan.java                   # Loan entity
│   │   │   ├── LoanRepayment.java                  # Repayment entity
│   │   │   └── AuditLog.java                       # Audit log entity
│   │   ├── dto/
│   │   │   ├── ConsumerRequest.java
│   │   │   ├── ConsumerResponse.java
│   │   │   ├── PrincipalAccountRequest.java
│   │   │   ├── CreateLoanRequest.java
│   │   │   ├── EMICalculationRequest.java
│   │   │   ├── EMICalculationResponse.java
│   │   │   └── [other DTOs]
│   │   ├── repository/
│   │   │   ├── ConsumerRepository.java
│   │   │   ├── PrincipalAccountRepository.java
│   │   │   ├── VendorRepository.java
│   │   │   ├── VendorLinkedAccountRepository.java
│   │   │   ├── PersonalLoanRepository.java
│   │   │   ├── LoanRepaymentRepository.java
│   │   │   └── AuditLogRepository.java
│   │   └── exception/
│   │       ├── LoanNotFoundException.java
│   │       ├── InvalidRepaymentException.java
│   │       ├── InvalidLoanOperationException.java
│   │       └── GlobalExceptionHandler.java
│   └── resources/
│       ├── application.properties               # Default config
│       ├── application-mysql.properties         # MySQL profile
│       ├── application-prod.properties          # Production profile
│       ├── application.yml                      # YAML config
│       ├── logback.xml                          # Logging configuration
│       ├── logback-prod.xml                     # Production logging
│       └── db/migration/
│           ├── V1__init_personal_loans.sql      # Initial schema
│           ├── V2__init_loan_repayments.sql     # Repayment tables
│           └── V3__create_audit_log.sql         # Audit trail
├── test/
│   └── java/com/consumerfinance/
│       ├── LoanManagementApplicationIntegrationTest.java
│       ├── controller/
│       │   ├── ConsumerControllerTest.java
│       │   ├── EMICalculationControllerTest.java
│       │   ├── PersonalLoanControllerTest.java
│       │   └── [other controller tests]
│       └── service/
│           ├── EMICalculationServiceTest.java
│           ├── PersonalLoanServiceTest.java
│           ├── LoanRepaymentServiceTest.java
│           └── [other service tests]

Docker/
├── Dockerfile                                   # Google Jib containerization
└── docker-compose.yml                           # MySQL + app orchestration

pom.xml                                          # Maven configuration with all dependencies
```

**Structure Decision**: Selected **Option 1 (Single Project)** - Monolithic Spring Boot microservice. All 8 APIs coexist in single deployable unit with clear service/controller/domain layering. This aligns with constitution's service-driven architecture while maintaining simplified deployment via Google Jib. Database uses MySQL 8.0 with Flyway migrations for schema evolution. All financial transactions use JPA with proper transaction management.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**Status**: ✅ No constitution violations identified. All complexity is justified by:

| Design Decision | Justification | Alternative Considered |
|-----------------|---------------|------------------------|
| 8 REST endpoints | Business requirements demand 8 distinct APIs for different stakeholders (consumer, vendor, operations) | Single monolithic endpoint rejected - violates RESTful design principle |
| 7 entities with relationships | Financial domain requires separation: Consumer/Account/Loan/Repayment/Vendor entities with 1:N and N:N relationships for correct audit trail and data integrity | Flat schema rejected - would compromise ACID compliance and auditability |
| JPA repository pattern | Constitution requirement (Principle IV) + enables type-safe queries and automatic transaction management for critical financial operations | Direct SQL rejected - violates constitution |
| Global exception handler | Constitution requirement (Principle VI) + ensures consistent error response format across all 8 endpoints + enables centralized audit logging | Per-controller exception handling rejected - violates consistency principle |
| Flyway migrations | Enables version-controlled schema evolution + supports rollback + tested against multiple environments | Manual DDL rejected - violates constitution requirement for tested migrations |
| OpenAPI/Swagger integration | Constitution requirement (Principle II) + automatic API documentation + enables contract-first testing | Manual documentation rejected - violates contract principle |

**All design decisions align with Consumer Finance Spring Boot Constitution (v1.0.0)**

---

## Implementation Phases

### Phase 0: Research & Clarification ✅ IN PROGRESS

**Deliverable**: `research.md` - Research findings on technologies, patterns, and best practices

**Tasks**:
- [ ] Research EMI calculation algorithm standards in consumer finance
- [ ] Research Spring Boot 3.2.0 + Java 17 LTS compatibility and best practices
- [ ] Research MySQL 8.0 ACID transaction handling for payment processing
- [ ] Research Flyway migration best practices for financial schema
- [ ] Research Spring Security RBAC implementation for loan systems
- [ ] Research JPA/Hibernate transaction management for concurrent operations
- [ ] Research Google Jib containerization best practices
- [ ] Research OpenAPI 3.0 contract-first design for financial APIs

### Phase 1: Design & Contracts ⏳ QUEUED

**Deliverables**: 
- `data-model.md` - Entity definitions and relationships
- `/contracts/` - 8 API contract documents
- `quickstart.md` - Development quick start guide

**Tasks**:
- [ ] Define Consumer, PrincipalAccount, Vendor, VendorLinkedAccount entities
- [ ] Define Loan, LoanRepayment, AuditLog entities with JPA annotations
- [ ] Create OpenAPI/Swagger contract for Consumer API
- [ ] Create OpenAPI/Swagger contract for Principal Account API
- [ ] Create OpenAPI/Swagger contract for Vendor APIs (2 contracts)
- [ ] Create OpenAPI/Swagger contract for Loan API
- [ ] Create OpenAPI/Swagger contract for EMI Calculation API
- [ ] Create OpenAPI/Swagger contract for Loan Repayment API
- [ ] Create OpenAPI/Swagger contract for Health API
- [ ] Generate quickstart guide with local setup, database setup, and test examples
- [ ] Update agent context with Java 17 + Spring Boot 3.2.0 technology stack

### Phase 2: Implementation Planning ⏳ QUEUED

**Deliverable**: `tasks.md` - Detailed implementation tasks (created by /speckit.tasks command)

---

**Next Step**: Execute `Phase 0: Research` by running research agent queries to clarify technology decisions and best practices.


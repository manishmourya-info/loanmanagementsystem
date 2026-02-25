# Implementation Plan: Consumer Finance Loan Management System

**Feature**: Consumer Finance - Personal Loan, EMI Calculation, Loan Repayment  
**Branch**: `master` | **Date**: 2026-02-25 | **Spec**: spec.md  
**Input**: Feature specification from `/specs/master/spec.md`  
**Status**: Phase 0 (Research & Technical Design)

---

## Summary

Build a comprehensive Spring Boot 3 microservice for personal loan management with three core APIs: loan application/approval, EMI calculation, and repayment processing. System maintains complete transaction history, validates all inputs, updates loan status based on payment status, and provides real-time outstanding balance tracking using MySQL 8.0 persistence.

---

## Technical Context (CONFIRMED)

**Language/Version**: Java 17 LTS  
**Framework**: Spring Boot 3.2.0  
**Build Tool**: Apache Maven 3.9.6  
**Primary Dependencies**: Spring Data JPA, Spring Web, Spring Security, MySQL Connector/J 8.0.33, Flyway 9.x, Lombok, JUnit 5, Mockito, Springdoc-OpenAPI  
**Storage**: MySQL 8.0 (production), H2 (testing)  
**Testing**: JUnit 5, Mockito, Spring TestRestTemplate, Integration tests  
**Target Platform**: Linux server (cloud-deployable as executable JAR)  
**Project Type**: REST Web Service (Microservice)  
**Performance Goals**: API response time < 500ms (p95), support 100+ concurrent users  
**Constraints**: 2 decimal place accuracy for financial calculations, ACID-compliant transactions for payment processing, audit logging for all operations  
**Scale/Scope**: Single-tenant SaaS backend, support 10k+ active loans, 100k+ transactions/month

---

## Constitution Check

**GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.**

✅ **Service-Driven Architecture** (Principle I)  
- REST API-first design with three distinct service endpoints
- Clear separation: Domain (entities) → Service (business logic) → Controller (API)

✅ **RESTful API Design** (Principle II)  
- JSON request/response with proper HTTP methods (POST create, GET retrieve, PUT status)
- Versioned endpoints (/api/v1/)
- Explicit error responses with descriptive messages

✅ **Test-First Development** (Principle III - NON-NEGOTIABLE)  
- TDD mandatory: Unit tests → Integration tests → Implementation
- Task phase includes test tasks before implementation tasks

✅ **Database Integrity & Transactions** (Principle IV)  
- MySQL 8.0 with ACID compliance
- JPA/Hibernate for ORM
- @Transactional on payment processing for atomicity

✅ **Security & Compliance** (Principle V)  
- Loan data is PII: Require Spring Security with JWT authentication
- Role-based access control (CUSTOMER, ADMIN, STAFF)
- Audit logging for all financial transactions

✅ **Observability & Monitoring** (Principle VI)  
- Structured SLF4J logging with Logback JSON output
- Correlation IDs for request traceability
- Spring Boot Actuator health checks

**GATE RESULT: PASS ✅** - No violations. All constitution principles satisfied.

---

## API Requirements (User-Specified)

### Personal Loan API
- **Apply for personal loan** - POST /api/v1/loans
- **Get loan by ID** - GET /api/v1/loans/{loanId}
- **Get all loans for a customer** - GET /api/v1/customers/{customerId}/loans
- **Loan approval / rejection** - PUT /api/v1/loans/{loanId}/status

### EMI Calculation API
- **Calculate EMI** - POST /api/v1/emi/calculate
  - Accept: Loan amount, Interest rate, Tenure
  - Return: EMI, Total interest, Total payment

### Loan Repayment API
- **Accept repayment** - POST /api/v1/loans/{loanId}/repayments
- **Validate payment amount** - Input validation (positive amount, ≤ outstanding balance)
- **Update outstanding balance** - Subtract paid amount from balance
- **Update EMI schedule** - Mark installments as PAID/PARTIALLY_PAID
- **Change loan status when fully paid** - Status → CLOSED when balance = 0
- **Store transaction history** - Persist each repayment with timestamp, amount, status

---

## Phase 0: Research & Clarifications

**Purpose**: Resolve technical unknowns before design  
**Duration**: 2-4 hours  
**Output**: research.md (to be generated)

### Key Decisions

**✅ Q1: Database Choice**  
- **Decision**: MySQL 8.0 (user-specified)
- **Rationale**: Mature RDBMS, strong financial transaction support, excellent JPA/Hibernate integration, ACID compliance
- **Alternatives Rejected**: PostgreSQL (over-engineered for MVP), SQLite (poor concurrency)

**✅ Q2: Authentication Model**  
- **Decision**: Spring Security with JWT tokens (optional for MVP, required for production)
- **Rationale**: Standard for fintech APIs, enables role-based access control (CUSTOMER, ADMIN, STAFF)
- **Alternatives Rejected**: OAuth2 (overkill for MVP), API keys (less flexible for roles)

**✅ Q3: EMI Calculation Formula**  
- **Decision**: Standard amortization formula: EMI = P × r × (1+r)^n / ((1+r)^n - 1)
- **Rationale**: Industry standard, accurate to 2 decimal places, matches financial calculators
- **Alternatives Rejected**: Simple interest (mathematically inaccurate), declining balance (equivalent but less standard)

**✅ Q4: Repayment Schedule Generation**  
- **Decision**: Pre-generate full schedule upon loan creation, update status with each payment
- **Rationale**: Enables customer visibility, faster status queries, easier overdue detection
- **Alternatives Rejected**: On-demand calculation (slower), manual entry (error-prone)

**✅ Q5: Transaction Atomicity**  
- **Decision**: Use Spring @Transactional on repayment processing, MySQL ACID guarantees
- **Rationale**: Ensure payment + balance update never partially succeed (data integrity critical for finance)
- **Alternatives Rejected**: Manual transaction management (error-prone), no transactions (unacceptable for PII/finance)

**✅ Q6: Database Migration & Schema Versioning**  
- **Decision**: Flyway 9.x with SQL-based migrations, version prefix V{N}__ for all scripts
- **Rationale**: Version-controlled schema evolution, audit trail, repeatable deployments, database-agnostic
- **Alternatives Rejected**: Liquibase (XML overhead), Hibernate DDL (lost audit trail), manual SQL (error-prone)

**✅ Q7: Container Image Generation & Distribution**  
- **Decision**: Google Jib Maven plugin (cloudplugins/gib) for optimized Docker images, no Dockerfile needed
- **Rationale**: Efficient layers, smaller image size (~150MB vs 300MB), faster builds, standard OCI format, multi-stage optimization
- **Alternatives Rejected**: Manual Docker (slow builds, large images), Cloud Build (requires GCP), Buildpacks (less control)

---

## Phase 1: Design & Data Modeling (Next)

**Purpose**: Define contracts and data structures before implementation  
**Estimated Duration**: 3-4 hours  
**Deliverables**: data-model.md, contracts/, quickstart.md

### 1.1 Data Model to Define

**PersonalLoan Entity**:
- PK: id (Long)
- Fields: customerId, principalAmount, annualInterestRate, loanTenureMonths, monthlyEMI, totalInterestPayable, outstandingBalance, remainingTenure, status (ACTIVE/CLOSED/SUSPENDED/DEFAULTED), createdAt, approvedAt, closedAt
- Relationships: One-to-Many with LoanRepayment (cascade delete disabled for data integrity)
- Constraints: principal > 0, interest 0-25%, tenure 6-360 months, status enum validation

**LoanRepayment Entity**:
- PK: id (Long), FK: loanId
- Fields: installmentNumber, principalAmount, interestAmount, totalAmount, status (PENDING/PAID/PARTIALLY_PAID/OVERDUE/WAIVED), dueDate, paidDate, paidAmount, createdAt
- Relationships: Many-to-One with PersonalLoan (required)
- Constraints: installmentNumber sequential, paidAmount ≤ totalAmount, status enum

**Future: Customer Entity** (deferred for MVP):
- PK: id (String: CUST123)
- Fields: name, email, phone, kycStatus (PENDING/APPROVED/REJECTED), createdAt
- Relationships: One-to-Many with PersonalLoan

### 1.2 API Contracts to Define

- POST /api/v1/loans (loan application)
- GET /api/v1/loans/{loanId} (retrieve single loan)
- GET /api/v1/customers/{customerId}/loans (customer's all loans)
- PUT /api/v1/loans/{loanId}/status (approve/reject loan)
- POST /api/v1/emi/calculate (standalone calculator)
- POST /api/v1/loans/{loanId}/repayments (process payment)
- GET /api/v1/loans/{loanId}/repayments (view schedule)

### 1.3 Quickstart Guide to Create
- Setup instructions
- Build and run commands
- First API call examples (curl/Postman)

---

## Project Structure

### Documentation (this feature)

```text
specs/master/
├── plan.md              # This file (implementation plan)
├── research.md          # Phase 0 output (to be generated)
├── data-model.md        # Phase 1 output (to be generated)
├── quickstart.md        # Phase 1 output (to be generated)
├── contracts/           # Phase 1 output (API schemas)
└── tasks.md             # Phase 2 output (79 tasks for implementation)
```

### Source Code Structure

```text
springboot/
├── src/main/java/com/consumerfinance/
│   ├── LoanManagementApplication.java          # Main Spring Boot app
│   ├── domain/                                 # JPA entities
│   │   ├── PersonalLoan.java
│   │   └── LoanRepayment.java
│   ├── dto/                                    # Request/Response DTOs
│   │   ├── CreateLoanRequest.java
│   │   ├── LoanResponse.java
│   │   ├── EMICalculationRequest.java
│   │   ├── EMICalculationResponse.java
│   │   ├── ProcessRepaymentRequest.java
│   │   └── RepaymentResponse.java
│   ├── repository/                             # Spring Data JPA repositories
│   │   ├── PersonalLoanRepository.java
│   │   └── LoanRepaymentRepository.java
│   ├── service/                                # Business logic (services)
│   │   ├── EMICalculationService.java
│   │   ├── PersonalLoanService.java
│   │   └── LoanRepaymentService.java
│   ├── controller/                             # REST endpoints (@RestController)
│   │   ├── LoanController.java
│   │   ├── EMICalculationController.java
│   │   └── RepaymentController.java
│   ├── exception/                              # Custom exceptions & handlers
│   │   ├── LoanNotFoundException.java
│   │   ├── InvalidLoanOperationException.java
│   │   ├── InvalidRepaymentException.java
│   │   └── GlobalExceptionHandler.java
│   ├── security/                               # Security configuration
│   │   ├── JwtTokenProvider.java (future)
│   │   └── SecurityConfig.java (future)
│   └── config/                                 # Spring configuration
│       ├── DatabaseConfig.java
│       └── OpenApiConfig.java
├── src/main/resources/
│   ├── application.properties                  # Development H2 profile
│   ├── application-mysql.properties            # MySQL development profile
│   ├── application-prod.properties             # Production MySQL profile
│   ├── logback.xml                             # Structured JSON logging
│   └── db/migration/                           # Flyway versioned migrations
│       ├── V1__init_personal_loans.sql
│       ├── V2__init_loan_repayments.sql
│       └── V3__init_audit_log.sql (future)
├── src/test/java/com/consumerfinance/
│   ├── service/
│   │   ├── EMICalculationServiceTest.java
│   │   ├── PersonalLoanServiceTest.java
│   │   └── LoanRepaymentServiceTest.java
│   └── controller/
│       ├── LoanControllerIntegrationTest.java
│       ├── EMICalculationControllerIntegrationTest.java
│       └── RepaymentControllerIntegrationTest.java
├── pom.xml                                      # Maven build configuration
├── README.md                                    # Project documentation
└── specs/master/
    ├── spec.md, plan.md, tasks.md, research.md, data-model.md, etc.
```

---

## Maven pom.xml Overview

**Key Dependencies**:
- `spring-boot-starter-web` (REST controllers)
- `spring-boot-starter-data-jpa` (ORM/Hibernate)
- `spring-boot-starter-security` (authentication)
- `mysql-connector-java:8.0.33` (MySQL driver)
- `flyway-core` (database migrations)
- `springdoc-openapi-starter-webmvc-ui:2.1.0` (OpenAPI/Swagger)
- `lombok` (reduce boilerplate)
- `junit-jupiter`, `mockito-core`, `spring-boot-starter-test` (testing)

**Key Configuration**:
- `java.version`: 17
- `maven.compiler.source/target`: 17
- `showWarnings`: true (enforce clean compilation)

---

## Build & Deployment

### Flyway Database Migration Strategy

**Purpose**: Version-controlled, auditable database schema evolution  
**Configuration**: Flyway 9.x (Maven plugin + Spring Boot auto-configuration)

**Migration Location**: `src/main/resources/db/migration/`  
**Naming Convention**: `V{N}_{description}.sql` (e.g., `V1__init_personal_loans.sql`)

**Migration Execution Flow**:
1. Application startup → Flyway checks `flyway_schema_history` table in MySQL
2. Compares deployed versions vs available scripts
3. Applies missing migrations in version order (V1, V2, V3...)
4. Records execution in `flyway_schema_history` with timestamp, status, checksum
5. Application proceeds only if all migrations successful

**Core Migrations**:
- **V1__init_personal_loans.sql**: Create personal_loans table with constraints, indices, enums
- **V2__init_loan_repayments.sql**: Create loan_repayments table with FK, unique constraints, indices
- **V3__init_audit_log.sql** (future): Create audit_log table for compliance

**Spring Boot Configuration**:
```properties
# application-mysql.properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.schemas=loan_management
spring.flyway.baseline-on-migrate=true
```

**Development vs Production**:
- **Development (H2)**: `spring.flyway.enabled=false` (Hibernate creates schema)
- **Production (MySQL)**: `spring.flyway.enabled=true` (explicit migrations required)

**Validation**:
- ✅ `mvn clean test` - Flyway runs with H2 in-memory
- ✅ `mvn clean package` - JAR includes migrations in classpath
- ✅ Manual: `java -jar app.jar --spring.profiles.active=mysql` - Flyway executes on startup

---

### Docker Image Generation with Google Jib

**Purpose**: Containerized application packaging without Dockerfile  
**Technology**: Google Jib Maven plugin (cloudplugins/gib)

**Jib Advantages Over Docker**:
- ✅ No Dockerfile required (declarative pom.xml config)
- ✅ Optimized image layers (faster CI/CD builds)
- ✅ 40-60% smaller images (~150MB vs 300MB with Dockerfile)
- ✅ Multi-stage build optimization (dependencies/application separation)
- ✅ Direct push to registry (Docker Hub, GCR, ECR)
- ✅ Reproducible builds (deterministic timestamps)

**Maven Configuration** (in pom.xml):
```xml
<plugin>
  <groupId>com.google.cloud.tools</groupId>
  <artifactId>jib-maven-plugin</artifactId>
  <version>3.4.0</version>
  <configuration>
    <to>
      <image>docker.io/yourusername/loan-management-system:${project.version}</image>
      <auth>
        <username>${DOCKER_HUB_USER}</username>
        <password>${DOCKER_HUB_TOKEN}</password>
      </auth>
    </to>
    <container>
      <jvmFlags>
        <jvmFlag>-XX:+UseG1GC</jvmFlag>
        <jvmFlag>-XX:MaxRAMPercentage=75</jvmFlag>
      </jvmFlags>
      <environment>
        <JAVA_TOOL_OPTIONS>-XX:+UseStringDeduplication</JAVA_TOOL_OPTIONS>
      </environment>
      <ports>
        <port>8080</port>
      </ports>
      <creationTime>USE_CURRENT_TIMESTAMP</creationTime>
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

**Image Build Workflow**:
1. **Development Build** (local Docker daemon):
   ```bash
   mvn clean compile jib:dockerBuild
   # Output: localhost:5000/loan-management-system:1.0.0
   docker run -p 8080:8080 localhost:5000/loan-management-system:1.0.0
   ```

2. **Production Build** (push to registry):
   ```bash
   export DOCKER_HUB_USER=youruser
   export DOCKER_HUB_TOKEN=yourtoken
   mvn clean package jib:build
   # Output: Pushed to docker.io/youruser/loan-management-system:1.0.0
   ```

3. **Tag & Release**:
   ```bash
   docker tag docker.io/youruser/loan-management-system:1.0.0 docker.io/youruser/loan-management-system:latest
   docker push docker.io/youruser/loan-management-system:latest
   ```

**Image Details**:
- **Base Image**: `openjdk:17-jdk-slim` (slim variant ~150MB)
- **Layers**:
  1. Dependencies (cached layer)
  2. Application JAR (frequently changed)
  3. Entrypoint configuration
- **Image Size**: ~150-180MB (includes JDK 17)
- **Startup Time**: 2-3 seconds

**Runtime**: 
```bash
# Run with MySQL
docker run -e SPRING_PROFILES_ACTIVE=mysql \
           -e SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/loan_management \
           -e SPRING_DATASOURCE_USERNAME=root \
           -e SPRING_DATASOURCE_PASSWORD=password \
           -p 8080:8080 \
           docker.io/youruser/loan-management-system:1.0.0

# Docker Compose
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: loan_management
  app:
    image: docker.io/youruser/loan-management-system:1.0.0
    depends_on:
      - mysql
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: mysql
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/loan_management
```

---

**Development Build**:
```bash
mvn clean compile          # Must succeed with 0 warnings
mvn clean test             # Run all tests
mvn clean package          # Build JAR
java -jar target/loan-management-system-1.0.0.jar  # Run
```

**Docker Development**:
```bash
mvn clean compile jib:dockerBuild
docker run -p 8080:8080 loan-management-system:1.0.0
```

**Production Build**:
```bash
mvn clean compile -DskipTests=false -Pproduction
# JAR automatically runs with application-prod.properties (MySQL)
```

**Production Docker**:
```bash
export DOCKER_HUB_USER=your_username
export DOCKER_HUB_TOKEN=your_token
mvn clean package jib:build
# Image pushed to Docker Hub automatically
```

---

## Success Criteria

✅ Build: `mvn clean compile` → 0 warnings  
✅ Tests: All unit + integration tests passing  
✅ Coverage: 80%+ for business logic  
✅ APIs: All 3 endpoints functional (loan, EMI, repayment)  
✅ Accuracy: EMI to 2 decimal places, matches calculators  
✅ Data: MySQL persistence verified  
✅ Transactions: Payment + balance update atomic  
✅ Logging: All operations logged with timestamps  
✅ Validation: All endpoints reject invalid inputs  
✅ Performance: < 500ms p95 response time  
✅ Documentation: OpenAPI 3.0 spec available  
✅ README: Setup, examples, deployment guide  

---

## Implementation Phases Overview

| Phase | Focus | Duration | Output |
|-------|-------|----------|--------|
| **Phase 0** | Research & Clarifications | 2-4h | research.md ✓ (underway) |
| **Phase 1** | Design & Data Model | 3-4h | data-model.md, contracts/, quickstart.md |
| **Phase 2** | Task Breakdown | 2-3h | tasks.md (79 tasks already complete) |
| **Phase 3-8** | Implementation | 20-25h | Complete application (controller, service, persistence) |
| **Testing** | Unit & Integration | 5-8h | Test coverage 80%+ |
| **Deployment** | Package & Docs | 2-3h | JAR, Docker, README |

---

**Version**: 1.0.0 | **Status**: Phase 0 (Research) | **Last Updated**: 2026-02-25 | **Next**: Generate research.md with full technical investigation

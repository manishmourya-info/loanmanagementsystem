<!-- 
  Sync Impact Report:
  - Version: 1.0.0 (initial)
  - New Constitution created for Consumer Finance Spring Boot Project
  - Principles: Service-Driven Architecture, RESTful API Design, Test-First Development, Observability
  - Templates requiring updates: ✅ plan-template.md, ✅ spec-template.md, ✅ tasks-template.md
-->

# Consumer Finance Spring Boot Constitution

## Core Principles

### I. Service-Driven Architecture
The application must be built as a modular, service-oriented system with clear separation of concerns. Each financial domain (Personal Loan, EMI Calculation, Loan Repayment) must be encapsulated as independently deployable services with well-defined contracts and boundaries. Services communicate via REST APIs with versioned endpoints and comprehensive error handling.

### II. RESTful API Design
All APIs must follow REST conventions with proper HTTP methods, status codes, and resource-oriented design. Request/Response payloads must use consistent JSON schemas with explicit validation rules. Documentation via OpenAPI/Swagger is mandatory for all endpoints. API versioning must be implemented (e.g., /api/v1/) to support backward compatibility.

### III. Test-First Development (NON-NEGOTIABLE)
Test-driven development is mandatory: unit tests written first, feature requirements clarified through tests, implementation follows. All critical business logic (EMI calculation, repayment schedules, loan validation) requires integration tests. Minimum code coverage: 80% for business logic.

### IV. Database Integrity & Transactions
All financial transactions must be ACID-compliant with proper transaction management. Loan modifications and payment processing require atomicity guarantees. Database schema migrations must be versioned and tested. No raw SQL; use JPA/Hibernate for ORM with repository pattern.

### V. Security & Compliance
Loan data is sensitive PII and must be protected. Implement role-based access control (RBAC) for customer vs. admin operations. All APIs require authentication/authorization. Passwords and sensitive fields must be encrypted. Audit logging for all financial transactions and data modifications is mandatory.

### VI. Observability & Monitoring
Structured logging required at INFO, WARN, and ERROR levels for all significant operations. Include request/response correlation IDs for traceability. Monitor loan calculation accuracy and payment processing success rates. Health checks must validate database connectivity and service dependencies.

## Technology Stack & Implementation Standards

**Platform**: Java 17 LTS, Spring Boot 3.x
**Build**: Maven with clean compile passing without warnings
**Database**: Relational database (PostgreSQL/MySQL) with JPA/Hibernate
**Testing**: JUnit 5, Mockito, TestNG for integration tests
**Documentation**: OpenAPI 3.0 (Springdoc-OpenAPI)
**Logging**: SLF4J with Logback

All dependencies must be explicitly declared in `pom.xml` with verified compatibility for Java 17 and Spring Boot 3.x. Build must complete without warnings: `mvn clean compile` must produce zero warnings.

## Development Workflow & Quality Gates

1. **Feature Branch Workflow**: Create feature branches from `main` for new capabilities (e.g., `feature/personal-loan-service`)
2. **Pre-commit Checks**: 
   - Code compiles without warnings
   - All tests pass (unit + integration)
   - Code coverage ≥ 80% for new code
   - No hardcoded credentials or sensitive data
3. **Code Review Requirements**: 
   - Minimum 1 approval before merge
   - Tests must pass in CI/CD pipeline
   - Documentation updated (API docs, business logic comments)
4. **Deployment Safety**:
   - Database migration scripts must be tested against schema
   - Backward-compatible API changes only (deprecate old versions)
   - Rollback procedure documented for each release

## Governance

**Constitution Compliance**: All PRs must verify adherence to principles and technology standards. Deviations require explicit justification and approval.

**Amendment Procedure**: 
- Constitution changes require team consensus
- Version bumping follows semantic versioning (MAJOR.MINOR.PATCH)
- MAJOR: Principle removals or core redefinitions
- MINOR: New principles/standards added
- PATCH: Clarifications and wording refinements
- Amendment date updated to ISO format (YYYY-MM-DD)

**Verification**: Use `mvn clean compile` as a mandatory gate. All code modifications must compile cleanly.

**Scope**: This constitution defines the non-negotiable development standards for the Consumer Finance application. Runtime development guidance is in the project README and technical documentation.

---

**Version**: 1.0.0 | **Ratified**: 2026-02-24 | **Last Amended**: 2026-02-24

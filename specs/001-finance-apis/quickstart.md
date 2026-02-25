# Quick Start Guide: Consumer Finance Multi-API Platform

**Feature**: 001-finance-apis  
**Date**: February 25, 2026  
**Target Audience**: Developers

---

## Prerequisites

### Software Requirements
- **Java**: 17 LTS (download from [adoptium.net](https://adoptium.net))
- **Maven**: 3.9.6+ ([maven.apache.org](https://maven.apache.org/download.cgi))
- **MySQL**: 8.0+ ([mysql.com](https://www.mysql.com/downloads/))
- **Git**: For version control
- **Docker**: (Optional) For containerized deployment
- **Postman**: (Optional) For API testing ([postman.com](https://www.postman.com/downloads/))

### Verify Installations
```bash
java -version          # Should output Java 17.x.x
mvn -version           # Should output Maven 3.9.6+
mysql --version        # Should output MySQL 8.0.x
```

---

## Setup Instructions

### Step 1: Clone Repository

```bash
git clone https://github.com/yourdomain/loan-management-system.git
cd loan-management-system
git checkout 001-finance-apis
```

### Step 2: Configure Database

**Create MySQL Database**:
```sql
-- Login to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE loan_db 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- Create application user
CREATE USER 'loan_user'@'localhost' IDENTIFIED BY 'secure_password_123';
GRANT ALL PRIVILEGES ON loan_db.* TO 'loan_user'@'localhost';
FLUSH PRIVILEGES;
```

**Update Configuration** - `src/main/resources/application-mysql.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/loan_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=loan_user
spring.datasource.password=secure_password_123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Flyway Migrations
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=false
spring.flyway.clean-disabled=true
```

### Step 3: Configure Application

Create `src/main/resources/application-dev.yml`:
```yaml
spring:
  application:
    name: loan-management-service
    version: 1.0.0
  
  profiles:
    active: mysql,dev
  
  security:
    jwt:
      secret: ${JWT_SECRET:dev-secret-key-min-256-bits-configure-production}
      expiration: 3600000  # 1 hour
      refresh-expiration: 604800000  # 7 days
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true

server:
  port: 8080
  servlet:
    context-path: /api/v1

logging:
  level:
    root: INFO
    com.consumerfinance: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/loan-management.log
    max-size: 10MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: when-authorized
```

### Step 4: Build Project

```bash
# Clean build
mvn clean install

# Run with warnings as errors (Constitution requirement)
mvn clean compile -Dmaven.compiler.failOnWarning=true

# Run tests
mvn test

# Check test coverage (should be 80%+)
mvn test jacoco:report
```

### Step 5: Run Application

```bash
# Option 1: Using Maven
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mysql,dev"

# Option 2: Using Java directly
java -jar target/loan-management-service-1.0.0.jar --spring.profiles.active=mysql,dev

# Option 3: Using Docker
docker build -t loan-management:latest .
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:mysql://mysql:3306/loan_db \
  -e DATABASE_USER=loan_user \
  -e DATABASE_PASSWORD=secure_password_123 \
  loan-management:latest
```

### Step 6: Verify Setup

```bash
# Check application is running
curl http://localhost:8080/api/v1/health

# Expected response
{
  "status": "UP",
  "components": {
    "database": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

---

## Database Schema Initialization

Flyway automatically creates schema on first run. Verify tables created:

```sql
USE loan_db;

SHOW TABLES;

-- Expected tables:
-- - consumers
-- - principal_accounts
-- - personal_loans
-- - loan_repayments
-- - vendors
-- - vendor_linked_accounts
-- - audit_logs
-- - flyway_schema_history
```

---

## API Testing

### Option 1: Using curl

**1. Generate JWT Token**
```bash
# Login/get token (endpoint implementation required in Phase 2)
export JWT_TOKEN="eyJhbGciOiJIUzUxMiJ9...."
```

**2. Create Consumer**
```bash
curl -X POST http://localhost:8080/api/v1/consumers \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "+91-9876543210",
    "identityType": "AADHAR",
    "identityNumber": "123456789012"
  }'
```

**3. Get Consumer**
```bash
curl -X GET http://localhost:8080/api/v1/consumers/{consumerId} \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**4. Link Principal Account**
```bash
curl -X POST http://localhost:8080/api/v1/consumers/{consumerId}/principal-account \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "0061000100109999",
    "accountHolderName": "John Doe",
    "bankCode": "ICIC0000001",
    "bankName": "ICICI Bank",
    "accountType": "SAVINGS"
  }'
```

**5. Create Loan**
```bash
curl -X POST http://localhost:8080/api/v1/loans \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "consumerId": "550e8400-e29b-41d4-a716-446655440000",
    "principal": 500000.00,
    "annualInterestRate": 12.00,
    "tenureMonths": 60
  }'
```

**6. Calculate EMI**
```bash
curl -X POST http://localhost:8080/api/v1/emi/calculate \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "principal": 500000.00,
    "annualInterestRate": 12.00,
    "tenureMonths": 60
  }'
```

### Option 2: Using Postman

1. Open Postman
2. Create new collection "Loan Management API"
3. Import OpenAPI spec:
   - URL: `http://localhost:8080/v3/api-docs`
4. Set up Bearer Token in collection Authorization
5. Run requests from imported collection

### Option 3: Using Integration Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ConsumerControllerTest

# Run with coverage report
mvn test jacoco:report
# View report: target/site/jacoco/index.html
```

---

## Project Structure

```
loan-management-system/
├── src/main/
│   ├── java/com/consumerfinance/
│   │   ├── LoanManagementApplication.java       # Entry point
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── OpenApiConfig.java
│   │   │   └── AuditLogAspect.java
│   │   ├── controller/                          # REST endpoints
│   │   │   ├── ConsumerController.java
│   │   │   ├── LoanController.java
│   │   │   ├── EMICalculationController.java
│   │   │   ├── PaymentController.java
│   │   │   └── HealthController.java
│   │   ├── service/                             # Business logic
│   │   │   ├── ConsumerService.java
│   │   │   ├── LoanService.java
│   │   │   ├── EMICalculationService.java
│   │   │   ├── PaymentService.java
│   │   │   └── AuditLogService.java
│   │   ├── domain/                              # JPA entities
│   │   │   ├── Consumer.java
│   │   │   ├── PersonalLoan.java
│   │   │   ├── LoanRepayment.java
│   │   │   └── AuditLog.java
│   │   ├── dto/                                 # Data Transfer Objects
│   │   │   ├── ConsumerRequest.java
│   │   │   ├── CreateLoanRequest.java
│   │   │   ├── EMICalculationResponse.java
│   │   │   └── ErrorResponse.java
│   │   ├── repository/                          # Spring Data repositories
│   │   │   ├── ConsumerRepository.java
│   │   │   ├── PersonalLoanRepository.java
│   │   │   ├── LoanRepaymentRepository.java
│   │   │   └── AuditLogRepository.java
│   │   └── exception/                           # Custom exceptions
│   │       ├── LoanNotFoundException.java
│   │       ├── InvalidRepaymentException.java
│   │       └── GlobalExceptionHandler.java
│   └── resources/
│       ├── application.properties
│       ├── application-mysql.properties
│       ├── application-dev.yml
│       ├── logback.xml
│       └── db/migration/
│           ├── V1__create_personal_loans_table.sql
│           ├── V2__create_loan_repayments_table.sql
│           └── V3__create_audit_log_table.sql
├── src/test/
│   └── java/com/consumerfinance/
│       ├── controller/
│       │   ├── ConsumerControllerTest.java
│       │   └── LoanControllerTest.java
│       └── service/
│           ├── EMICalculationServiceTest.java
│           └── PaymentServiceTest.java
├── pom.xml                                      # Maven configuration
├── Dockerfile                                   # Container image
├── docker-compose.yml                           # Local environment
└── README.md
```

---

## Development Workflow

### 1. Create Feature Branch
```bash
git checkout -b feature/consumer-api
```

### 2. Make Changes
```bash
# Edit source files
# Write tests first (TDD)
mvn test
```

### 3. Verify Build Quality
```bash
# Must compile without warnings
mvn clean compile -Dmaven.compiler.failOnWarning=true

# All tests pass
mvn test

# Coverage >= 80%
mvn test jacoco:report
```

### 4. Commit and Push
```bash
git add .
git commit -m "feat(consumer-api): Add consumer registration endpoint"
git push origin feature/consumer-api
```

### 5. Create Pull Request
- Link to feature spec
- Reference issue/ticket
- Ensure CI/CD passes

---

## Troubleshooting

### Issue: MySQL Connection Failed
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**Solution**:
1. Verify MySQL is running: `mysql -u root -p`
2. Check connection string in `application-mysql.properties`
3. Verify credentials: `mysql -u loan_user -p -h localhost`
4. Check MySQL version: `SELECT VERSION();`

### Issue: Flyway Migration Failed
```
org.flywaydb.core.internal.command.DbValidate$ValidateErrorImpl
```

**Solution**:
1. Check migration files in `src/main/resources/db/migration/`
2. Verify SQL syntax in migrations
3. Check for previously applied migrations: `SELECT * FROM flyway_schema_history;`
4. If corrupted, reset database and re-run migrations

### Issue: Build Warnings
```
[WARNING] [1,18] warning: raw use of parameterized class 'X'
```

**Solution**:
1. Add generic type parameters: `List<String>` instead of `List`
2. Suppress if legitimate: `@SuppressWarnings("unchecked")`
3. Build with: `mvn clean compile`

### Issue: Tests Timeout
```
Test exceeded timeout after 30 seconds
```

**Solution**:
1. Increase timeout in test: `@Timeout(value = 60, unit = TimeUnit.SECONDS)`
2. Check for database locking
3. Increase MySQL max_connections if needed

---

## Performance Optimization

### Enable Query Logging (Development Only)
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Monitor JVM Memory
```bash
jps -l  # List Java processes
jstat -gc {process_id} 1000  # GC stats every 1 second
```

### Connection Pool Tuning
```properties
# Maximum pool size
spring.datasource.hikari.maximum-pool-size=20
# Minimum idle connections
spring.datasource.hikari.minimum-idle=5
# Connection timeout (ms)
spring.datasource.hikari.connection-timeout=30000
```

---

## Deployment Checklist

- [ ] All tests pass with > 80% coverage
- [ ] No compiler warnings
- [ ] Constitution compliance verified
- [ ] Security scan passed (OWASP)
- [ ] Database migrations tested
- [ ] Audit logging enabled
- [ ] Health checks configured
- [ ] API documentation updated (OpenAPI)
- [ ] Docker image built and tested
- [ ] Environment configuration externalized
- [ ] Credentials in environment variables
- [ ] Performance tested (< 1000ms response time)

---

## Documentation Resources

- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Architecture**: See `specs/001-finance-apis/plan.md`
- **Data Model**: See `specs/001-finance-apis/data-model.md`
- **API Contracts**: See `specs/001-finance-apis/contracts/api-contracts.md`
- **Research**: See `specs/001-finance-apis/research.md`

---

## Support & Contact

- **Issues**: Use GitHub Issues with label `001-finance-apis`
- **Questions**: See Architecture Decision Records (ADRs) in docs/
- **Security**: Contact security@yourdomain.com
- **Performance**: See performance-tuning.md

---

**Status**: ✅ Quick Start Guide Complete

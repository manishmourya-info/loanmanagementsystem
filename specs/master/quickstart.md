# Quickstart Guide: Consumer Finance Loan Management System

**Feature**: Consumer Finance - Loan Management System  
**Platform**: Spring Boot 3.2.0, Java 17, MySQL 8.0, Maven 3.9.6  
**Last Updated**: 2026-02-25

---

## Quick Setup (5 minutes)

### Prerequisites

- Java 17 LTS installed (`java -version` should show 17.x)
- Maven 3.9.6+ installed (`mvn --version` should show 3.9.6+)
- MySQL 8.0 server running locally (or use H2 for quick testing)
- Git (optional, for version control)

### Step 1: Clone/Navigate to Project

```bash
cd /path/to/springboot
```

### Step 2: Build the Project

```bash
mvn clean compile
```

**Expected Output**: Build success with zero warnings
```
[INFO] BUILD SUCCESS
[INFO] Total time: 15.234 s
[INFO] Finished at: 2026-02-25T10:00:00Z
```

### Step 3: Run Tests (Optional)

```bash
mvn clean test
```

### Step 4: Start the Application

#### Option A: Development (H2 Database)
```bash
mvn spring-boot:run
```

Application starts at: `http://localhost:8080`

#### Option B: Production (MySQL)
```bash
# Update application-prod.properties with your MySQL credentials first
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

#### Option C: Package as JAR
```bash
mvn clean package
java -jar target/loan-management-system-1.0.0.jar
```

---

## Database Setup

### Option A: H2 (In-Memory, for Development/Testing)

**Configuration** (default, no setup needed):
```properties
# application.properties
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
```

**Access H2 Console**: `http://localhost:8080/h2-console`

### Option B: MySQL 8.0

**Prerequisites**: MySQL 8.0 running

**Step 1: Create Database**
```sql
CREATE DATABASE loan_management;
CREATE USER 'loan_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON loan_management.* TO 'loan_user'@'localhost';
FLUSH PRIVILEGES;
```

**Step 2: Update Configuration**
```properties
# application-mysql.properties or application-prod.properties
spring.datasource.url=jdbc:mysql://localhost:3306/loan_management
spring.datasource.username=loan_user
spring.datasource.password=secure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

**Step 3: Run Application with MySQL Profile**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mysql"
```

---

## API Quick Test

### 1. Apply for Personal Loan

**Command**:
```bash
curl -X POST http://localhost:8080/api/v1/loans \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "principalAmount": 500000,
    "annualInterestRate": 10.5,
    "loanTenureMonths": 60
  }'
```

**Expected Response**:
```json
{
  "id": 1,
  "customerId": "CUST001",
  "principalAmount": 500000,
  "annualInterestRate": 10.5,
  "loanTenureMonths": 60,
  "monthlyEMI": 9638.22,
  "totalInterestPayable": 78293.20,
  "outstandingBalance": 500000,
  "remainingTenure": 60,
  "status": "ACTIVE",
  "createdAt": "2026-02-25T10:00:00Z",
  "approvedAt": "2026-02-25T10:00:00Z"
}
```

**Note**: Replace `CUST001` with any customer ID you want. The loan ID from response (e.g., `1`) is used in subsequent calls.

---

### 2. Calculate EMI (Without Creating Loan)

**Command**:
```bash
curl -X POST http://localhost:8080/api/v1/emi/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "principalAmount": 300000,
    "annualInterestRate": 12,
    "tenureMonths": 48
  }'
```

**Expected Response**:
```json
{
  "monthlyEMI": 7413.84,
  "totalInterest": 56265.22,
  "totalAmount": 356265.22,
  "principal": 300000,
  "annualInterestRate": 12,
  "tenureMonths": 48,
  "calculatedAt": "2026-02-25T10:00:00Z"
}
```

---

### 3. Retrieve Loan Details

**Command** (replace `1` with the loan ID from step 1):
```bash
curl http://localhost:8080/api/v1/loans/1
```

**Expected Response**:
```json
{
  "id": 1,
  "customerId": "CUST001",
  "principalAmount": 500000,
  "monthlyEMI": 9638.22,
  "outstandingBalance": 500000,
  "remainingTenure": 60,
  "status": "ACTIVE",
  "createdAt": "2026-02-25T10:00:00Z"
}
```

---

### 4. Get Customer's All Loans

**Command**:
```bash
curl "http://localhost:8080/api/v1/customers/CUST001/loans?status=ACTIVE"
```

**Expected Response**:
```json
{
  "content": [
    {
      "id": 1,
      "customerId": "CUST001",
      "principalAmount": 500000,
      "monthlyEMI": 9638.22,
      "outstandingBalance": 500000,
      "status": "ACTIVE",
      "createdAt": "2026-02-25T10:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

---

### 5. Process Repayment

**Command** (replace `1` with loan ID):
```bash
curl -X POST http://localhost:8080/api/v1/loans/1/repayments \
  -H "Content-Type: application/json" \
  -d '{
    "installmentNumber": 1,
    "paidAmount": 9638.22,
    "transactionReference": "TXN20260225001",
    "remarks": "Paid via online banking"
  }'
```

**Expected Response**:
```json
{
  "id": 101,
  "loanId": 1,
  "installmentNumber": 1,
  "principalAmount": 7808.33,
  "interestAmount": 1829.89,
  "totalAmount": 9638.22,
  "paidAmount": 9638.22,
  "status": "PAID",
  "dueDate": "2026-03-25T00:00:00Z",
  "paidDate": "2026-02-25T15:30:00Z",
  "remainingBalance": 492191.67,
  "nextDueDate": "2026-04-25T00:00:00Z"
}
```

---

### 6. View Repayment Schedule

**Command** (replace `1` with loan ID):
```bash
curl "http://localhost:8080/api/v1/loans/1/repayments"
```

**Expected Response**:
```json
{
  "loanId": 1,
  "loanStatus": "ACTIVE",
  "totalInstallments": 60,
  "paidInstallments": 1,
  "pendingInstallments": 59,
  "repayments": [
    {
      "id": 101,
      "installmentNumber": 1,
      "totalAmount": 9638.22,
      "status": "PAID",
      "dueDate": "2026-03-25T00:00:00Z",
      "paidDate": "2026-02-25T15:30:00Z"
    },
    {
      "id": 102,
      "installmentNumber": 2,
      "totalAmount": 9638.22,
      "status": "PENDING",
      "dueDate": "2026-04-25T00:00:00Z"
    }
  ],
  "pagination": {
    "totalElements": 60,
    "totalPages": 3,
    "currentPage": 0,
    "pageSize": 20
  }
}
```

---

### 7. Get Pending Installments

**Command** (replace `1` with loan ID):
```bash
curl "http://localhost:8080/api/v1/loans/1/repayments/pending"
```

**Expected Response**:
```json
{
  "loanId": 1,
  "pendingInstallments": 59,
  "nextDueInstallment": {
    "id": 102,
    "installmentNumber": 2,
    "totalAmount": 9638.22,
    "dueDate": "2026-04-25T00:00:00Z",
    "daysUntilDue": 58
  },
  "repayments": [
    {
      "id": 102,
      "installmentNumber": 2,
      "totalAmount": 9638.22,
      "status": "PENDING",
      "dueDate": "2026-04-25T00:00:00Z"
    }
  ]
}
```

---

## Using Postman

### Import API Collection

1. Open Postman
2. Click `Import` → `Paste Raw Text`
3. Paste the following:

```json
{
  "info": {
    "name": "Consumer Finance API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Apply for Loan",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": {"raw": "http://localhost:8080/api/v1/loans", "protocol": "http", "host": ["localhost"], "port": ["8080"], "path": ["api", "v1", "loans"]},
        "body": {"mode": "raw", "raw": "{\"customerId\": \"CUST001\", \"principalAmount\": 500000, \"annualInterestRate\": 10.5, \"loanTenureMonths\": 60}"}
      }
    },
    {
      "name": "Calculate EMI",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": {"raw": "http://localhost:8080/api/v1/emi/calculate", "protocol": "http", "host": ["localhost"], "port": ["8080"], "path": ["api", "v1", "emi", "calculate"]},
        "body": {"mode": "raw", "raw": "{\"principalAmount\": 300000, \"annualInterestRate\": 12, \"tenureMonths\": 48}"}
      }
    },
    {
      "name": "Get Loan",
      "request": {
        "method": "GET",
        "url": {"raw": "http://localhost:8080/api/v1/loans/1", "protocol": "http", "host": ["localhost"], "port": ["8080"], "path": ["api", "v1", "loans", "1"]}
      }
    },
    {
      "name": "Process Repayment",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": {"raw": "http://localhost:8080/api/v1/loans/1/repayments", "protocol": "http", "host": ["localhost"], "port": ["8080"], "path": ["api", "v1", "loans", "1", "repayments"]},
        "body": {"mode": "raw", "raw": "{\"installmentNumber\": 1, \"paidAmount\": 9638.22, \"transactionReference\": \"TXN001\"}"}
      }
    }
  ]
}
```

---

## Swagger UI

**Access OpenAPI Documentation**:
```
http://localhost:8080/swagger-ui.html
```

All endpoints are documented with:
- Request/response schemas
- Parameter descriptions
- Example values
- Try it out functionality

---

## Environment Configuration

### Development (H2)
```bash
# Automatic - use default application.properties
mvn spring-boot:run
```

### MySQL Local
```bash
# Create database first, then:
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mysql"
```

### Production
```bash
# Build and run JAR with environment variables
mvn clean package
java -jar target/loan-management-system-1.0.0.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://prod-server:3306/loan_management \
  --spring.datasource.username=app_user \
  --spring.datasource.password=secure_password
```

---

## Troubleshooting

### Issue: Port 8080 Already in Use

**Solution**:
```bash
# Use different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

### Issue: MySQL Connection Refused

**Solution**:
```bash
# Verify MySQL is running
mysql -u loan_user -p -h localhost -D loan_management

# If not running, start MySQL:
# Windows: net start MySQL80
# Linux: sudo systemctl start mysql
# Mac: brew services start mysql
```

### Issue: Compilation Errors with Java 17

**Solution**:
```bash
# Verify Java version
java -version  # Should be 17.x

# If not, set JAVA_HOME
export JAVA_HOME=/path/to/jdk-17
mvn clean compile
```

### Issue: Tests Failing

**Solution**:
```bash
# Run with debug logging
mvn test -X

# Or run single test class
mvn test -Dtest=EMICalculationServiceTest
```

---

## Project Structure Quick Reference

```
springboot/
├── src/main/java/com/consumerfinance/
│   ├── domain/              # JPA entities (PersonalLoan, LoanRepayment)
│   ├── dto/                 # Request/Response DTOs
│   ├── repository/          # Spring Data JPA repositories
│   ├── service/             # Business logic (EMI, Loan, Repayment services)
│   ├── controller/          # REST endpoints
│   ├── exception/           # Exception handling
│   └── config/              # Spring configuration
├── src/main/resources/
│   ├── application.properties       # Dev (H2)
│   ├── application-mysql.properties # MySQL
│   ├── application-prod.properties  # Production
│   └── db/migration/                # Flyway migrations
├── src/test/java/                   # Unit & integration tests
├── pom.xml                          # Maven dependencies
└── specs/master/                    # Specification documents
```

---

## Next Steps

1. ✅ **Build & Run**: `mvn clean compile && mvn spring-boot:run`
2. ✅ **Test APIs**: Use curl or Postman commands above
3. ✅ **Verify Data**: Check database (H2 console or MySQL client)
4. 📋 **Review Logs**: Check application output for errors
5. 🔍 **Explore Swagger**: Visit `http://localhost:8080/swagger-ui.html`

---

## Support & Documentation

- **Full API Specification**: See `contracts.md`
- **Data Model Details**: See `data-model.md`
- **Implementation Plan**: See `plan.md`
- **GitHub Issues**: Report bugs with reproduction steps
- **Email Support**: development-team@company.com

---

**Version**: 1.0.0 | **Last Updated**: 2026-02-25

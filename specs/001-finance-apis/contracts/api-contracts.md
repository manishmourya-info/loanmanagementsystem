# API Contracts: Consumer Finance Multi-API Platform

**Feature**: 001-finance-apis  
**Date**: February 25, 2026  
**Status**: Phase 1 Design

---

## Overview

This document defines the contracts for 8 REST API endpoints in the Consumer Finance platform. All endpoints follow REST conventions with consistent JSON payloads, error handling, and OpenAPI 3.0 specifications.

**Base URL**: `http://localhost:8080/api/v1`  
**Authentication**: JWT Bearer Token (required for all endpoints except health)  
**Content-Type**: `application/json`  
**Response Format**: JSON with error standardization

---

## 1. Consumer API

**Purpose**: Customer onboarding, profile management, and KYC operations

### 1.1 Create Consumer (POST /consumers)

**Description**: Register a new customer with KYC initial setup

```
POST /api/v1/consumers
Authorization: Bearer {jwt_token}
Content-Type: application/json

Request Body:
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+91-9876543210",
  "identityType": "AADHAR",
  "identityNumber": "123456789012"
}

Response: 201 Created
{
  "consumerId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+91-9876543210",
  "identityType": "AADHAR",
  "kycStatus": "PENDING",
  "status": "ACTIVE",
  "createdAt": "2026-02-25T10:30:45.123456Z"
}
```

**Request Schema**:
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["name", "email", "phone", "identityType", "identityNumber"],
  "properties": {
    "name": {
      "type": "string",
      "minLength": 2,
      "maxLength": 100,
      "pattern": "^[a-zA-Z\\s]+$",
      "description": "Full name of consumer"
    },
    "email": {
      "type": "string",
      "format": "email",
      "description": "Email address (must be unique)"
    },
    "phone": {
      "type": "string",
      "pattern": "^\\+?[1-9]\\d{1,14}$",
      "description": "Phone in E.164 format"
    },
    "identityType": {
      "type": "string",
      "enum": ["PASSPORT", "DRIVING_LICENSE", "AADHAR", "PAN"],
      "description": "Type of identity proof"
    },
    "identityNumber": {
      "type": "string",
      "minLength": 5,
      "maxLength": 50,
      "description": "Identity document number"
    }
  }
}
```

**Response Status Codes**:
- `201 Created`: Consumer created successfully
- `400 Bad Request`: Validation failed (invalid email, phone format, etc.)
- `409 Conflict`: Email or phone already exists
- `401 Unauthorized`: Missing/invalid JWT token
- `500 Internal Server Error`: Server error

**Error Response**:
```json
{
  "timestamp": "2026-02-25T10:30:45.123456Z",
  "status": 400,
  "error": "Validation Error",
  "message": "Invalid input",
  "errors": [
    {
      "field": "email",
      "message": "Email format is invalid"
    },
    {
      "field": "phone",
      "message": "Phone must be in E.164 format"
    }
  ],
  "path": "/api/v1/consumers"
}
```

**Performance Requirement**: Response time < 1000ms (95th percentile)  
**Audit**: CONSUMER_CREATED action logged

---

### 1.2 Get Consumer by ID (GET /consumers/{consumerId})

**Description**: Retrieve consumer details by ID

```
GET /api/v1/consumers/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "consumerId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+91-9876543210",
  "identityType": "AADHAR",
  "identityNumber": "123456789012",
  "kycStatus": "VERIFIED",
  "status": "ACTIVE",
  "createdAt": "2026-02-25T10:30:45.123456Z",
  "updatedAt": "2026-02-25T11:00:00.000000Z"
}
```

**Authorization**: CUSTOMER can view own; LOAN_MANAGER/ADMIN can view any

**Status Codes**:
- `200 OK`: Success
- `404 Not Found`: Consumer not found
- `403 Forbidden`: Insufficient permissions
- `401 Unauthorized`: Missing/invalid JWT

**Performance Requirement**: Response time < 500ms (95th percentile)

---

### 1.3 Update Consumer (PUT /consumers/{consumerId})

**Description**: Update consumer profile information

```
PUT /api/v1/consumers/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer {jwt_token}
Content-Type: application/json

Request Body (all fields optional):
{
  "name": "Jane Doe",
  "phone": "+91-9876543211",
  "identityType": "AADHAR",
  "identityNumber": "123456789013"
}

Response: 200 OK
{
  "consumerId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Jane Doe",
  "email": "john.doe@example.com",
  "phone": "+91-9876543211",
  "identityType": "AADHAR",
  "identityNumber": "123456789013",
  "kycStatus": "PENDING",  // Reset to PENDING when identity changes
  "status": "ACTIVE",
  "updatedAt": "2026-02-25T11:15:30.000000Z"
}
```

**Rules**:
- Email cannot be updated (immutable)
- Changing identity information resets KYC status to PENDING
- KYC-verified consumers cannot change identity without admin approval

**Status Codes**:
- `200 OK`: Updated successfully
- `400 Bad Request`: Validation error
- `404 Not Found`: Consumer not found
- `409 Conflict`: Phone already in use
- `403 Forbidden`: Cannot modify verified KYC without approval

**Performance Requirement**: Response time < 1000ms

**Audit**: CONSUMER_UPDATED action logged

---

### 1.4 Get All Consumers (GET /consumers)

**Description**: List all consumers (admin only) with pagination

```
GET /api/v1/consumers?page=0&size=20&status=ACTIVE
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "content": [
    {
      "consumerId": "550e8400-e29b-41d4-a716-446655440000",
      "name": "John Doe",
      "email": "john.doe@example.com",
      "status": "ACTIVE",
      "createdAt": "2026-02-25T10:30:45.123456Z"
    }
  ],
  "pageable": {
    "size": 20,
    "number": 0,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

**Query Parameters**:
- `page`: Page number (0-indexed, default: 0)
- `size`: Page size (default: 20, max: 100)
- `status`: Filter by status (ACTIVE, INACTIVE, etc.)
- `search`: Search by name/email/phone

**Authorization**: ADMIN/LOAN_MANAGER only

**Performance Requirement**: Response time < 2000ms for 100k records

---

## 2. Principal Account API

**Purpose**: Platform fund management and account linking

### 2.1 Create Principal Account (POST /consumers/{consumerId}/principal-account)

**Description**: Link consumer's primary banking account

```
POST /api/v1/consumers/550e8400-e29b-41d4-a716-446655440000/principal-account
Authorization: Bearer {jwt_token}
Content-Type: application/json

Request Body:
{
  "accountNumber": "0061000100109999",
  "accountHolderName": "John Doe",
  "bankCode": "ICIC0000001",
  "bankName": "ICICI Bank",
  "accountType": "SAVINGS"
}

Response: 201 Created
{
  "principalAccountId": "660f9511-f30c-42e5-b827-557766551111",
  "consumerId": "550e8400-e29b-41d4-a716-446655440000",
  "accountNumber": "0061000100109999",
  "accountHolderName": "John Doe",
  "bankCode": "ICIC0000001",
  "bankName": "ICICI Bank",
  "accountType": "SAVINGS",
  "verificationStatus": "PENDING",
  "createdAt": "2026-02-25T10:30:45.123456Z"
}
```

**Validation Rules**:
- Account holder name must match consumer name (80%+ fuzzy match)
- Account number format: 10-34 characters (IBAN or national format)
- Only one principal account per consumer (replaces previous)
- Bank code must be valid per region

**Status Codes**:
- `201 Created`: Account linked
- `400 Bad Request`: Invalid account details
- `404 Not Found`: Consumer not found
- `409 Conflict`: Principal account already exists (returned but replaced)

**Performance Requirement**: Response time < 1000ms

**Audit**: ACCOUNT_LINKED action logged

---

### 2.2 Get Principal Account (GET /consumers/{consumerId}/principal-account)

**Description**: Retrieve linked principal account

```
GET /api/v1/consumers/550e8400-e29b-41d4-a716-446655440000/principal-account
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "principalAccountId": "660f9511-f30c-42e5-b827-557766551111",
  "consumerId": "550e8400-e29b-41d4-a716-446655440000",
  "accountNumber": "0061000100109999",
  "accountHolderName": "John Doe",
  "bankCode": "ICIC0000001",
  "bankName": "ICICI Bank",
  "accountType": "SAVINGS",
  "verificationStatus": "VERIFIED",
  "linkedDate": "2026-02-25T11:00:00.000000Z",
  "createdAt": "2026-02-25T10:30:45.123456Z"
}
```

**Authorization**: Consumer can view own; ADMIN can view any

**Status Codes**:
- `200 OK`: Success
- `404 Not Found`: Consumer or account not found

---

### 2.3 Update Principal Account (PUT /consumers/{consumerId}/principal-account)

**Description**: Update linked principal account details

```
PUT /api/v1/consumers/550e8400-e29b-41d4-a716-446655440000/principal-account
Authorization: Bearer {jwt_token}
Content-Type: application/json

Request Body:
{
  "accountNumber": "0061000100110000",
  "bankCode": "HDFC0000001"
}

Response: 200 OK
{
  "principalAccountId": "660f9511-f30c-42e5-b827-557766551111",
  "accountNumber": "0061000100110000",
  "verificationStatus": "PENDING",  // Reset on update
  "updatedAt": "2026-02-25T11:30:00.000000Z"
}
```

**Rules**:
- Verification status resets to PENDING when updated
- Cannot update if loan is ACTIVE (account change not allowed)

**Status Codes**:
- `200 OK`: Updated
- `400 Bad Request`: Invalid account
- `409 Conflict`: Cannot update while loan active

**Audit**: ACCOUNT_UPDATED action logged

---

## 3. Loan API

**Purpose**: Loan application and lifecycle management

### 3.1 Create Loan (POST /loans)

**Description**: Submit new loan application

```
POST /api/v1/loans
Authorization: Bearer {jwt_token}
Content-Type: application/json

Request Body:
{
  "consumerId": "550e8400-e29b-41d4-a716-446655440000",
  "principal": 500000.00,
  "annualInterestRate": 12.00,
  "tenureMonths": 60
}

Response: 201 Created
{
  "loanId": "770a0622-g41d-53f6-c938-668877662222",
  "consumerId": "550e8400-e29b-41d4-a716-446655440000",
  "principal": 500000.00,
  "annualInterestRate": 12.00,
  "tenureMonths": 60,
  "monthlyEMI": 10644.56,
  "status": "PENDING",
  "createdAt": "2026-02-25T10:30:45.123456Z"
}
```

**Validation Rules**:
- Consumer must exist with VERIFIED KYC
- Consumer must have VERIFIED principal account
- Principal: 10,000.00 - 50,000,000.00
- Annual interest rate: 0.01% - 36.00%
- Tenure: 12 - 360 months
- Consumer max 5 ACTIVE loans

**EMI Calculation**:
$$\text{EMI} = \frac{P \times r \times (1+r)^n}{(1+r)^n - 1}$$
where r = (annual_rate / 100 / 12)

**Status Codes**:
- `201 Created`: Loan created
- `400 Bad Request`: Invalid parameters (amount, tenure out of range)
- `404 Not Found`: Consumer not found
- `409 Conflict`: Consumer limit exceeded
- `403 Forbidden`: Missing verified account/KYC

**Performance Requirement**: Response time < 1000ms

**Audit**: LOAN_CREATED action logged

---

### 3.2 Get Loan by ID (GET /loans/{loanId})

**Description**: Retrieve loan details

```
GET /api/v1/loans/770a0622-g41d-53f6-c938-668877662222
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "loanId": "770a0622-g41d-53f6-c938-668877662222",
  "consumerId": "550e8400-e29b-41d4-a716-446655440000",
  "principal": 500000.00,
  "annualInterestRate": 12.00,
  "tenureMonths": 60,
  "monthlyEMI": 10644.56,
  "disbursedAmount": 500000.00,
  "status": "ACTIVE",
  "approvalDate": "2026-02-25T11:00:00.000000Z",
  "disbursementDate": "2026-02-25T11:30:00.000000Z",
  "maturityDate": "2031-02-25T00:00:00.000000Z",
  "createdAt": "2026-02-25T10:30:45.123456Z"
}
```

**Authorization**: CUSTOMER views own; ADMIN views any

---

### 3.3 Get Customer's Loans (GET /consumers/{consumerId}/loans)

**Description**: List all loans for a customer

```
GET /api/v1/consumers/550e8400-e29b-41d4-a716-446655440000/loans?status=ACTIVE
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "content": [
    {
      "loanId": "770a0622-g41d-53f6-c938-668877662222",
      "principal": 500000.00,
      "monthlyEMI": 10644.56,
      "status": "ACTIVE",
      "disbursementDate": "2026-02-25T11:30:00.000000Z"
    }
  ],
  "totalElements": 3,
  "totalPages": 1
}
```

---

### 3.4 Approve Loan (PUT /loans/{loanId}/approve)

**Description**: Approve pending loan application (loan manager only)

```
PUT /api/v1/loans/770a0622-g41d-53f6-c938-668877662222/approve
Authorization: Bearer {jwt_token}
Content-Type: application/json

Request Body:
{
  "approvalNotes": "KYC verified, income verified"
}

Response: 200 OK
{
  "loanId": "770a0622-g41d-53f6-c938-668877662222",
  "status": "APPROVED",
  "approvalDate": "2026-02-25T12:00:00.000000Z"
}
```

**Authorization**: LOAN_MANAGER/ADMIN only

**Rules**:
- Only PENDING loans can be approved
- Cannot approve twice

**Status Codes**:
- `200 OK`: Approved
- `404 Not Found`: Loan not found
- `409 Conflict`: Loan not in PENDING status
- `403 Forbidden`: Insufficient permissions

**Audit**: LOAN_APPROVED action logged

---

### 3.5 Reject Loan (PUT /loans/{loanId}/reject)

**Description**: Reject loan application

```
PUT /api/v1/loans/770a0622-g41d-53f6-c938-668877662222/reject
Authorization: Bearer {jwt_token}
Content-Type: application/json

Request Body:
{
  "rejectionReason": "Income verification failed"
}

Response: 200 OK
{
  "loanId": "770a0622-g41d-53f6-c938-668877662222",
  "status": "REJECTED",
  "rejectionReason": "Income verification failed"
}
```

**Audit**: LOAN_REJECTED action logged

---

### 3.6 Disburse Loan (PUT /loans/{loanId}/disburse)

**Description**: Disburse approved loan amount to consumer's account

```
PUT /api/v1/loans/770a0622-g41d-53f6-c938-668877662222/disburse
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "loanId": "770a0622-g41d-53f6-c938-668877662222",
  "status": "ACTIVE",
  "disbursedAmount": 500000.00,
  "disbursementDate": "2026-02-25T12:30:00.000000Z",
  "maturityDate": "2031-02-25T00:00:00.000000Z"
}
```

**Rules**:
- Only APPROVED loans can be disbursed
- Disbursement date becomes start of EMI schedule

**Audit**: LOAN_DISBURSED action logged

---

## 4. EMI Calculation API

**Purpose**: Calculate monthly installment and repayment breakdown

### 4.1 Calculate EMI (POST /emi/calculate)

**Description**: Calculate monthly EMI and total repayment

```
POST /api/v1/emi/calculate
Authorization: Bearer {jwt_token}
Content-Type: application/json

Request Body:
{
  "principal": 500000.00,
  "annualInterestRate": 12.00,
  "tenureMonths": 60
}

Response: 200 OK
{
  "principal": 500000.00,
  "annualInterestRate": 12.00,
  "tenureMonths": 60,
  "monthlyEMI": 10644.56,
  "totalPayment": 639873.60,
  "totalInterest": 139873.60,
  "schedule": [
    {
      "installment": 1,
      "dueDate": "2026-03-25",
      "emiAmount": 10644.56,
      "principalComponent": 5144.56,
      "interestComponent": 5000.00,
      "remainingBalance": 494855.44
    }
    // ... 59 more installments
  ]
}
```

**Validation**:
- Principal: 10,000.00 - 50,000,000.00
- Interest rate: 0.01% - 36.00%
- Tenure: 12 - 360 months

**Calculations**:
- EMI = (P × r × (1+r)^n) / ((1+r)^n - 1), where r = annual_rate/100/12
- Total Payment = EMI × tenure_months
- Total Interest = Total Payment - Principal
- Principal component = EMI - Interest component
- Interest component = Outstanding balance × monthly rate
- Remaining balance = Previous balance - Principal component

**Performance Requirement**: Response time < 500ms (99th percentile)

**Status Codes**:
- `200 OK`: Success
- `400 Bad Request`: Invalid parameters

---

## 5. Loan Repayment API

**Purpose**: Record and process EMI payments

### 5.1 Create Repayment (POST /loans/{loanId}/repayments)

**Description**: Record EMI payment for a loan

```
POST /api/v1/loans/770a0622-g41d-53f6-c938-668877662222/repayments
Authorization: Bearer {jwt_token}
Content-Type: application/json

Request Body:
{
  "installmentNumber": 1,
  "paidAmount": 10644.56,
  "paymentMethod": "ONLINE",
  "transactionId": "TXN-20260225-001"
}

Response: 201 Created
{
  "repaymentId": "880b1733-h52e-64g7-d949-779988773333",
  "loanId": "770a0622-g41d-53f6-c938-668877662222",
  "installmentNumber": 1,
  "dueDate": "2026-03-25",
  "emiAmount": 10644.56,
  "paidAmount": 10644.56,
  "status": "PAID",
  "paidDate": "2026-02-25T13:00:00.000000Z",
  "remainingBalance": 489210.88
}
```

**Validation Rules**:
- Loan must exist and be ACTIVE
- Repayment must be PENDING
- Paid amount >= EMI amount (or configured tolerance)
- Payment date cannot exceed due date + 60 days (configurable)
- Transaction ID must be unique (prevents duplicate payments)

**Concurrent Payment Handling**:
- Uses @Version (optimistic lock) for high concurrency
- Uses PESSIMISTIC_WRITE lock for critical payment processing
- Returns HTTP 409 CONFLICT if concurrent modification detected

**Status Codes**:
- `201 Created`: Payment recorded
- `400 Bad Request`: Invalid amount or parameters
- `404 Not Found`: Loan/repayment not found
- `409 Conflict`: Concurrent modification detected
- `412 Precondition Failed`: Loan not ACTIVE

**Performance Requirement**: Response time < 1000ms (95th percentile)

**Audit**: PAYMENT_PROCESSED action logged with amount, transaction ID, remaining balance

---

### 5.2 Get Loan Repayments (GET /loans/{loanId}/repayments)

**Description**: Get all repayments for a loan

```
GET /api/v1/loans/770a0622-g41d-53f6-c938-668877662222/repayments
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "content": [
    {
      "repaymentId": "880b1733-h52e-64g7-d949-779988773333",
      "installmentNumber": 1,
      "dueDate": "2026-03-25",
      "emiAmount": 10644.56,
      "paidAmount": 10644.56,
      "status": "PAID",
      "paidDate": "2026-02-25T13:00:00.000000Z"
    }
    // ... 59 more repayments
  ],
  "totalElements": 60,
  "totalPages": 3
}
```

---

### 5.3 Get Repayment Details (GET /loans/{loanId}/repayments/{repaymentId})

**Description**: Retrieve specific repayment details

```
GET /api/v1/loans/770a0622-g41d-53f6-c938-668877662222/repayments/880b1733-h52e-64g7-d949-779988773333
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "repaymentId": "880b1733-h52e-64g7-d949-779988773333",
  "loanId": "770a0622-g41d-53f6-c938-668877662222",
  "installmentNumber": 1,
  "dueDate": "2026-03-25",
  "emiAmount": 10644.56,
  "paidAmount": 10644.56,
  "status": "PAID",
  "paidDate": "2026-02-25T13:00:00.000000Z",
  "paymentMethod": "ONLINE",
  "transactionId": "TXN-20260225-001"
}
```

---

## 6. Vendor API

**Purpose**: Merchant onboarding and management

### 6.1 Create Vendor (POST /vendors)

**Description**: Register new vendor

```
POST /api/v1/vendors
Authorization: Bearer {jwt_token}
Content-Type: application/json

Request Body:
{
  "vendorName": "ABC Services Ltd",
  "businessType": "MERCHANT",
  "registrationNumber": "REG-2024-001",
  "gstNumber": "22AABCT0000001Z0",
  "contactEmail": "vendor@example.com",
  "contactPhone": "+91-9876543210"
}

Response: 201 Created
{
  "vendorId": "990c2844-i63f-75h8-e050-880099884444",
  "vendorName": "ABC Services Ltd",
  "businessType": "MERCHANT",
  "registrationNumber": "REG-2024-001",
  "gstNumber": "22AABCT0000001Z0",
  "contactEmail": "vendor@example.com",
  "contactPhone": "+91-9876543210",
  "status": "ACTIVE",
  "registrationDate": "2026-02-25T14:00:00.000000Z"
}
```

**Status Codes**:
- `201 Created`: Vendor created
- `400 Bad Request`: Invalid registration number/GST
- `409 Conflict`: Vendor already exists

**Audit**: VENDOR_CREATED action logged

---

## 7. Vendor Linked Account API

**Purpose**: Manage vendor settlement accounts

### 7.1 Create Vendor Account (POST /vendors/{vendorId}/linked-accounts)

**Description**: Link vendor's bank account for settlements

```
POST /api/v1/vendors/990c2844-i63f-75h8-e050-880099884444/linked-accounts
Authorization: Bearer {jwt_token}
Content-Type: application/json

Request Body:
{
  "accountNumber": "0061000100119999",
  "accountHolderName": "ABC Services Ltd",
  "bankCode": "HDFC0000001",
  "accountType": "CURRENT"
}

Response: 201 Created
{
  "vendorAccountId": "aa0d3955-j74g-86i9-f061-991100995555",
  "vendorId": "990c2844-i63f-75h8-e050-880099884444",
  "accountNumber": "0061000100119999",
  "accountHolderName": "ABC Services Ltd",
  "bankCode": "HDFC0000001",
  "accountType": "CURRENT",
  "status": "PENDING",
  "createdAt": "2026-02-25T14:30:00.000000Z"
}
```

**Status Codes**:
- `201 Created`: Account linked
- `404 Not Found`: Vendor not found
- `409 Conflict`: Max accounts for vendor (5) exceeded

**Audit**: VENDOR_ACCOUNT_LINKED action logged

---

### 7.2 Get Vendor Linked Accounts (GET /vendors/{vendorId}/linked-accounts)

**Description**: List vendor's linked accounts

```
GET /api/v1/vendors/990c2844-i63f-75h8-e050-880099884444/linked-accounts
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "content": [
    {
      "vendorAccountId": "aa0d3955-j74g-86i9-f061-991100995555",
      "accountNumber": "0061000100119999",
      "bankCode": "HDFC0000001",
      "status": "ACTIVE"
    }
  ],
  "totalElements": 1
}
```

---

## 8. Application Health API

**Purpose**: System health monitoring and dependency status

### 8.1 Health Check (GET /health)

**Description**: Check application and dependency health

```
GET /api/v1/health
Authorization: optional (public endpoint)

Response: 200 OK
{
  "status": "UP",
  "timestamp": "2026-02-25T15:00:00.000000Z",
  "uptime": 86400000,
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "version": "8.0.35",
        "validationQuery": "isValid()"
      },
      "responseTime": 12
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1099511627776,
        "free": 549755813888,
        "threshold": 10485760,
        "status": "UP"
      }
    },
    "jvm": {
      "status": "UP",
      "details": {
        "memory": "512M/2048M",
        "processors": 8
      }
    },
    "applicationReady": {
      "status": "UP"
    }
  }
}
```

**Performance Requirement**: Response time < 100ms

**Status Codes**:
- `200 OK`: All systems operational
- `503 Service Unavailable`: One or more components DOWN

**Response Fields**:
- `status`: Overall health (UP, DOWN, DEGRADED)
- `timestamp`: Health check timestamp
- `uptime`: Application uptime in milliseconds
- `components`: Status of each dependency
  - `database`: Database connectivity
  - `diskSpace`: Disk availability
  - `jvm`: JVM heap and processor info
  - `applicationReady`: Application startup completion

---

## Cross-Cutting Concerns

### Error Response Format (All Endpoints)

```json
{
  "timestamp": "2026-02-25T15:00:00.123456Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "principal",
      "message": "must be >= 10000.00",
      "rejectedValue": 5000.00
    }
  ],
  "path": "/api/v1/loans",
  "correlationId": "abc-123-def-456"
}
```

### Authentication

All endpoints (except /health) require:
```
Authorization: Bearer {jwt_token}
```

JWT contains:
- `sub`: Username/User ID
- `roles`: List of roles (ADMIN, LOAN_MANAGER, CUSTOMER, etc.)
- `exp`: Expiration timestamp

### Pagination (List Endpoints)

```json
{
  "content": [...],
  "pageable": {
    "size": 20,
    "number": 0,
    "sort": "createdAt,desc"
  },
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

### Audit Logging

Every state-changing operation (POST, PUT, DELETE) is logged:
```
User: {userId}
Action: {OPERATION}
Entity: {LOAN_CREATED, PAYMENT_PROCESSED, etc.}
Amount: {transaction_amount}
Status: {SUCCESS, FAILURE}
Timestamp: {ISO8601}
```

---

**Status**: ✅ API Contracts Complete - Ready for Implementation

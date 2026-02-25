# API Contracts: Consumer Finance Loan Management System

**Feature**: Consumer Finance - Loan Management System  
**Phase**: 1 (API Design & Contracts)  
**Created**: 2026-02-25  
**Status**: Complete (Ready for Implementation)

---

## API Overview

| Resource | Method | Endpoint | Purpose |
|----------|--------|----------|---------|
| **Personal Loan** | POST | /api/v1/loans | Apply for a new loan |
| | GET | /api/v1/loans/{loanId} | Retrieve loan details |
| | GET | /api/v1/customers/{customerId}/loans | List customer's loans |
| | PUT | /api/v1/loans/{loanId}/status | Approve/reject/suspend loan |
| **EMI Calculator** | POST | /api/v1/emi/calculate | Calculate EMI for given parameters |
| **Repayment** | POST | /api/v1/loans/{loanId}/repayments | Process a repayment |
| | GET | /api/v1/loans/{loanId}/repayments | Get repayment schedule |
| | GET | /api/v1/loans/{loanId}/repayments/pending | Get pending installments |

---

## Authentication & Authorization

**Method**: Spring Security with JWT Bearer Token (production) / Optional for MVP

**Header Format**:
```
Authorization: Bearer <jwt-token>
```

**Roles** (future):
- `CUSTOMER`: Can view own loans, make payments
- `ADMIN`: Can approve/reject/manage all loans, view overdue
- `STAFF`: Can process payments on behalf of customers

---

## 1. Personal Loan API

### 1.1 Apply for Personal Loan

**Endpoint**: `POST /api/v1/loans`

**Purpose**: Customer applies for a personal loan. System calculates EMI and generates repayment schedule.

**Request**:
```json
{
  "customerId": "CUST001",
  "principalAmount": 500000,
  "annualInterestRate": 10.5,
  "loanTenureMonths": 60
}
```

**Request Validation**:
```
customerId:           Required, non-empty string (<=50 chars)
principalAmount:      Required, decimal, range [1000, 10000000]
annualInterestRate:   Required, decimal (2 places), range [0, 25]
loanTenureMonths:     Required, integer, range [6, 360]
```

**Response 200 (Success)**:
```json
{
  "id": 1,
  "customerId": "CUST001",
  "principalAmount": 500000.00,
  "annualInterestRate": 10.50,
  "loanTenureMonths": 60,
  "monthlyEMI": 9638.22,
  "totalInterestPayable": 78293.20,
  "outstandingBalance": 500000.00,
  "remainingTenure": 60,
  "status": "ACTIVE",
  "createdAt": "2026-02-25T10:00:00Z",
  "approvedAt": "2026-02-25T10:00:00Z",
  "closedAt": null
}
```

**Response 400 (Validation Error)**:
```json
{
  "error": "Validation failed",
  "details": [
    "principalAmount must be between 1000 and 10000000",
    "annualInterestRate must be between 0 and 25"
  ],
  "timestamp": "2026-02-25T10:00:00Z"
}
```

**Response 500 (Server Error)**:
```json
{
  "error": "Internal server error",
  "message": "Failed to create loan",
  "timestamp": "2026-02-25T10:00:00Z"
}
```

---

### 1.2 Retrieve Loan Details

**Endpoint**: `GET /api/v1/loans/{loanId}`

**Purpose**: Customer/Admin retrieves full details of a specific loan.

**Path Parameters**:
```
loanId: Numeric loan ID (Long)
```

**Query Parameters**:
```
(None)
```

**Response 200 (Success)**:
```json
{
  "id": 1,
  "customerId": "CUST001",
  "principalAmount": 500000.00,
  "annualInterestRate": 10.50,
  "loanTenureMonths": 60,
  "monthlyEMI": 9638.22,
  "totalInterestPayable": 78293.20,
  "outstandingBalance": 485000.00,
  "remainingTenure": 58,
  "status": "ACTIVE",
  "createdAt": "2026-02-25T10:00:00Z",
  "approvedAt": "2026-02-25T10:00:00Z",
  "closedAt": null
}
```

**Response 404 (Not Found)**:
```json
{
  "error": "Loan not found",
  "loanId": 999,
  "timestamp": "2026-02-25T10:00:00Z"
}
```

---

### 1.3 List Customer's Loans

**Endpoint**: `GET /api/v1/customers/{customerId}/loans`

**Purpose**: Retrieve all loans for a specific customer (with optional filters).

**Path Parameters**:
```
customerId: Customer ID string (e.g., CUST001)
```

**Query Parameters**:
```
status: Optional filter - "ACTIVE", "CLOSED", "SUSPENDED", "DEFAULTED"
sort:   Optional - "createdAt" (default), "outstandingBalance", "monthlyEMI"
order:  Optional - "asc" (default), "desc"
page:   Optional - Page number (0-indexed, default 0)
size:   Optional - Results per page (default 20, max 100)
```

**Example**:
```
GET /api/v1/customers/CUST001/loans?status=ACTIVE&sort=monthlyEMI&order=desc&page=0&size=10
```

**Response 200 (Success)**:
```json
{
  "content": [
    {
      "id": 1,
      "customerId": "CUST001",
      "principalAmount": 500000.00,
      "monthlyEMI": 9638.22,
      "outstandingBalance": 485000.00,
      "status": "ACTIVE",
      "createdAt": "2026-02-25T10:00:00Z"
    },
    {
      "id": 2,
      "customerId": "CUST001",
      "principalAmount": 200000.00,
      "monthlyEMI": 3855.29,
      "outstandingBalance": 0.00,
      "status": "CLOSED",
      "createdAt": "2025-12-15T08:30:00Z"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

**Response 200 (No Loans)**:
```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "currentPage": 0,
  "pageSize": 20
}
```

---

### 1.4 Update Loan Status (Approve/Reject)

**Endpoint**: `PUT /api/v1/loans/{loanId}/status`

**Purpose**: Admin approves, rejects, or modifies loan status.

**Path Parameters**:
```
loanId: Numeric loan ID (Long)
```

**Request**:
```json
{
  "newStatus": "ACTIVE",
  "reason": "Approved after KYC verification"
}
```

**Allowed Status Transitions**:
```
ACTIVE → SUSPENDED (manual suspension)
ACTIVE → DEFAULTED (critical delinquency)
SUSPENDED → ACTIVE (reinstate)
Any status → CLOSED (admin closure)
```

**Response 200 (Success)**:
```json
{
  "id": 1,
  "status": "SUSPENDED",
  "previousStatus": "ACTIVE",
  "updatedAt": "2026-02-25T12:00:00Z",
  "reason": "Suspension for review"
}
```

**Response 400 (Invalid Transition)**:
```json
{
  "error": "Invalid status transition",
  "currentStatus": "CLOSED",
  "requestedStatus": "ACTIVE",
  "message": "Cannot transition from CLOSED to ACTIVE",
  "timestamp": "2026-02-25T12:00:00Z"
}
```

---

## 2. EMI Calculation API

### 2.1 Calculate EMI

**Endpoint**: `POST /api/v1/emi/calculate`

**Purpose**: Calculate EMI (standalone, without creating loan) for given principal, rate, and tenure.

**Request**:
```json
{
  "principalAmount": 500000,
  "annualInterestRate": 10.5,
  "tenureMonths": 60
}
```

**Request Validation**:
```
principalAmount:      Required, decimal, range [1000, 10000000]
annualInterestRate:   Required, decimal (2 places), range [0, 25]
tenureMonths:         Required, integer, range [6, 360]
```

**Response 200 (Success)**:
```json
{
  "monthlyEMI": 9638.22,
  "totalInterest": 78293.20,
  "totalAmount": 578293.20,
  "principal": 500000.00,
  "annualInterestRate": 10.50,
  "tenureMonths": 60,
  "calculatedAt": "2026-02-25T10:00:00Z"
}
```

**Calculation Formula**:
```
r = annualInterestRate / 12 / 100       (monthly rate as decimal)
n = tenureMonths
EMI = principal × r × (1+r)^n / ((1+r)^n - 1)
Total Interest = EMI × n - principal
Total Amount = principal + Total Interest
```

**Example Calculation**:
```
Principal: 500,000
Annual Rate: 10.5% → Monthly: 0.875%
Tenure: 60 months

r = 0.10875
EMI = 500,000 × 0.00875 × (1.00875)^60 / ((1.00875)^60 - 1)
    = 9,638.22

Total Interest = 9,638.22 × 60 - 500,000 = 78,293.20
Total Amount = 578,293.20
```

**Response 400 (Validation Error)**:
```json
{
  "error": "Invalid EMI calculation parameters",
  "details": [
    "principalAmount must be >= 1000",
    "annualInterestRate must be <= 25"
  ],
  "timestamp": "2026-02-25T10:00:00Z"
}
```

---

## 3. Loan Repayment API

### 3.1 Process Repayment

**Endpoint**: `POST /api/v1/loans/{loanId}/repayments`

**Purpose**: Customer/Admin submits a payment for a specific installment.

**Path Parameters**:
```
loanId: Numeric loan ID (Long)
```

**Request**:
```json
{
  "installmentNumber": 1,
  "paidAmount": 9638.22,
  "transactionReference": "TXN20260225001",
  "remarks": "Paid via online banking"
}
```

**Request Validation**:
```
installmentNumber:    Required, integer >= 1
paidAmount:           Required, decimal > 0
transactionReference: Optional, string (for reconciliation)
remarks:              Optional, string <= 500 chars
```

**Response 200 (Success)**:
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
  "nextDueDate": "2026-04-25T00:00:00Z",
  "transactionReference": "TXN20260225001"
}
```

**Response 202 (Partial Payment)**:
```json
{
  "id": 101,
  "loanId": 1,
  "installmentNumber": 1,
  "totalAmount": 9638.22,
  "paidAmount": 5000.00,
  "status": "PARTIALLY_PAID",
  "remainingAmount": 4638.22,
  "remainingBalance": 497191.67,
  "paidDate": "2026-02-25T15:30:00Z"
}
```

**Response 400 (Validation Error)**:
```json
{
  "error": "Repayment validation failed",
  "details": [
    "Installment not found for loan 1, installment 10",
    "Paid amount cannot exceed due amount 9638.22"
  ],
  "timestamp": "2026-02-25T15:30:00Z"
}
```

**Response 409 (Conflict - Already Paid)**:
```json
{
  "error": "Installment already paid",
  "installmentNumber": 1,
  "paidDate": "2026-02-20T10:00:00Z",
  "paidAmount": 9638.22,
  "timestamp": "2026-02-25T15:30:00Z"
}
```

---

### 3.2 Get Repayment Schedule

**Endpoint**: `GET /api/v1/loans/{loanId}/repayments`

**Purpose**: Customer/Admin views complete repayment schedule for a loan.

**Path Parameters**:
```
loanId: Numeric loan ID (Long)
```

**Query Parameters**:
```
status: Optional - "PENDING", "PAID", "PARTIALLY_PAID", "OVERDUE", "WAIVED"
sort:   Optional - "installmentNumber" (default), "dueDate", "status"
page:   Optional - Page number (default 0)
size:   Optional - Results per page (default 50, max 100)
```

**Example**:
```
GET /api/v1/loans/1/repayments?status=PENDING&sort=dueDate&page=0&size=20
```

**Response 200 (Success)**:
```json
{
  "loanId": 1,
  "loanStatus": "ACTIVE",
  "principalAmount": 500000.00,
  "totalInstallments": 60,
  "paidInstallments": 1,
  "pendingInstallments": 59,
  "overdueInstallments": 0,
  "repayments": [
    {
      "id": 101,
      "installmentNumber": 1,
      "principalAmount": 7808.33,
      "interestAmount": 1829.89,
      "totalAmount": 9638.22,
      "paidAmount": 9638.22,
      "status": "PAID",
      "dueDate": "2026-03-25T00:00:00Z",
      "paidDate": "2026-02-25T15:30:00Z"
    },
    {
      "id": 102,
      "installmentNumber": 2,
      "principalAmount": 7914.24,
      "interestAmount": 1723.98,
      "totalAmount": 9638.22,
      "paidAmount": null,
      "status": "PENDING",
      "dueDate": "2026-04-25T00:00:00Z",
      "paidDate": null
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

### 3.3 Get Pending Repayments

**Endpoint**: `GET /api/v1/loans/{loanId}/repayments/pending`

**Purpose**: Quick retrieval of only pending (due) installments for a loan.

**Path Parameters**:
```
loanId: Numeric loan ID (Long)
```

**Response 200 (Success)**:
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
    },
    {
      "id": 103,
      "installmentNumber": 3,
      "totalAmount": 9638.22,
      "status": "PENDING",
      "dueDate": "2026-05-25T00:00:00Z"
    }
  ]
}
```

---

## Error Response Format (Standard)

All error responses follow this format:

```json
{
  "error": "<error-type>",
  "message": "<human-readable-message>",
  "details": ["<detail-1>", "<detail-2>"],
  "timestamp": "<ISO-8601-timestamp>",
  "path": "<request-path>",
  "status": "<HTTP-status-code>"
}
```

**Common HTTP Status Codes**:
| Status | Meaning |
|--------|---------|
| 200 | OK - Success |
| 201 | Created - Resource created |
| 202 | Accepted - Async operation accepted |
| 400 | Bad Request - Validation error |
| 401 | Unauthorized - Auth required |
| 403 | Forbidden - Permission denied |
| 404 | Not Found - Resource not found |
| 409 | Conflict - State conflict (e.g., already paid) |
| 500 | Internal Server Error |

---

## Request/Response Headers

**Request Headers**:
```
Content-Type: application/json
Accept: application/json
Authorization: Bearer <jwt-token> (if authentication required)
X-Correlation-ID: <uuid> (optional, for request tracing)
```

**Response Headers**:
```
Content-Type: application/json
X-Correlation-ID: <uuid> (echoes request ID for tracing)
X-RateLimit-Limit: 1000 (max requests per hour)
X-RateLimit-Remaining: 999 (requests remaining)
```

---

## Rate Limiting (Future Enhancement)

```
Limit: 1000 requests per hour per API key/user
Penalty: 429 Too Many Requests after limit exceeded
```

---

## OpenAPI 3.0 Specification

Auto-generated at: `GET http://localhost:8080/v3/api-docs`

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

---

**Version**: 1.0.0 | **Status**: Complete | **Last Updated**: 2026-02-25

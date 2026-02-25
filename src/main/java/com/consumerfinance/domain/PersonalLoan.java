package com.consumerfinance.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PersonalLoan entity represents a personal loan issued to a customer.
 * Contains core loan information including amount, duration, and interest rate.
 * Maintains ACID compliance for financial transactions.
 */
@Entity
@Table(name = "personal_loans", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String customerId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal annualInterestRate;

    @Column(nullable = false)
    private Integer loanTenureMonths;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyEMI;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInterestPayable;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal outstandingBalance;

    @Column(nullable = false)
    private Integer remainingTenure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime approvedAt;

    @Column
    private LocalDateTime rejectedAt;

    @Column
    private LocalDateTime closedAt;

    @Column(length = 500)
    private String approvalRemarks;

    @Column(length = 500)
    private String rejectionReason;

    // Relationship to LoanRepayment (one loan has many repayments)
    @OneToMany(mappedBy = "loan", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<LoanRepayment> repayments;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Loan Status enum - represents the lifecycle of a loan
     */
    public enum LoanStatus {
        PENDING,    // Initial state after application
        APPROVED,   // Credit decision approved
        ACTIVE,     // Approved and EMI schedule active
        CLOSED,     // Fully repaid or closed by admin
        REJECTED,   // Credit decision rejected
        DEFAULTED   // Critical delinquency
    }

}


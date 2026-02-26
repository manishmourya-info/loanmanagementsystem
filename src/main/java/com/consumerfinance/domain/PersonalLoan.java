package com.consumerfinance.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * PersonalLoan Entity (T009)
 * 
 * Represents a personal loan application and contract with financial precision using BigDecimal.
 * All monetary fields use DECIMAL(19,2) for precision without floating-point errors.
 * Uses optimistic locking (@Version) and pessimistic locking support for concurrent operations.
 */
@Entity
@Table(name = "personal_loans", indexes = {
    @Index(name = "idx_consumer_id", columnList = "consumer_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PersonalLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "loan_id")
    private UUID id;

    @NotNull(message = "Consumer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_loan_consumer"))
    private Consumer consumer;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "10000.00", message = "Principal amount must be at least 10,000")
    @DecimalMax(value = "50000000.00", message = "Principal amount cannot exceed 50,000,000")
    @Column(name = "principal", nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @NotNull(message = "Annual interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be at least 0.01%")
    @DecimalMax(value = "36.00", message = "Interest rate cannot exceed 36%")
    @Column(name = "annual_interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal annualInterestRate;

    @NotNull(message = "Tenure in months is required")
    @Min(value = 12, message = "Tenure must be at least 12 months")
    @Max(value = 360, message = "Tenure cannot exceed 360 months")
    @Column(name = "tenure_months", nullable = false)
    private Integer loanTenureMonths;

    @NotNull(message = "Monthly EMI is required")
    @Column(name = "monthly_emi", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyEMI;

    @Column(name = "total_interest_payable", precision = 19, scale = 2)
    private BigDecimal totalInterestPayable;

    @Column(name = "outstanding_balance", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Column(name = "remaining_tenure")
    @Builder.Default
    private Integer remainingTenure = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private LoanStatus status = LoanStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "approval_remarks", length = 500)
    private String approvalRemarks;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Version
    @Column(name = "version")
    private Long version;

    // Relationship to LoanRepayment (one loan has many repayments)
    @OneToMany(mappedBy = "loan", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<LoanRepayment> repayments;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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


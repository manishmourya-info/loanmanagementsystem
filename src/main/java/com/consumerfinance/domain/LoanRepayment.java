package com.consumerfinance.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LoanRepayment entity tracks all repayment transactions for a loan.
 * Records principal, interest, and total payment amounts.
 * Maintains audit trail of payment history with transaction references.
 */
@Entity
@Table(name = "loan_repayments", indexes = {
    @Index(name = "idx_loan_id", columnList = "loan_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_due_date", columnList = "due_date"),
    @Index(name = "idx_paid_date", columnList = "paid_date")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_loan_installment", columnNames = {"loan_id", "installment_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private PersonalLoan loan;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column
    private LocalDateTime paidDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal paidAmount;

    @Column(length = 50)
    private String paymentMode;

    @Column(length = 100)
    private String transactionReference;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Repayment Status enum - represents the payment state of an EMI
     */
    public enum RepaymentStatus {
        PENDING,        // EMI due but not paid
        PAID,           // Fully paid (paidAmount = totalAmount)
        PARTIALLY_PAID, // Partial payment made
        OVERDUE,        // Due date passed, still pending
        WAIVED          // Forgiven/written-off
    }

}


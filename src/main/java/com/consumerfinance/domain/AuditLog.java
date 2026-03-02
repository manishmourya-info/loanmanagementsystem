package com.consumerfinance.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuditLog Entity (T012)
 * 
 * Represents system audit trail for compliance and security.
 * Immutable record of all significant operations, especially financial transactions.
 * No update/delete operations exposed - append-only audit log.
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id")
    private UUID auditId;

    @NotBlank(message = "Action is required")
    @Column(name = "action", nullable = false, length = 100)
    private String action; // CONSUMER_CREATED, ACCOUNT_LINKED, LOAN_APPROVED, PAYMENT_PROCESSED, etc.

    @Column(name = "loan_id")
    private String loanId; // FK reference, stored as String for flexibility

    @NotBlank(message = "User ID is required")
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId; // System, LOAN_MANAGER, ADMIN, or anonymous

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount; // For financial operations

    @Column(name = "details", columnDefinition = "JSON")
    private String details; // JSON: {before, after, reason, etc.}

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AuditStatus status = AuditStatus.SUCCESS;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public enum AuditStatus {
        SUCCESS, FAILURE, PARTIAL
    }
}

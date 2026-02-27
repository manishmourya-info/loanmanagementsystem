package com.consumerfinance.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PrincipalAccount Entity (T008)
 * 
 * Represents the consumer's primary banking account used for loan disbursement and repayments.
 * Each Consumer can have exactly ONE principal account (enforced by UNIQUE constraint).
 */
@Entity
@Table(name = "principal_accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_consumer_principal_account", columnNames = "consumer_id"),
        @UniqueConstraint(name = "uk_account_number", columnNames = "account_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrincipalAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "principal_account_id")
    private UUID principalAccountId;

    @NotNull(message = "Consumer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_principal_account_consumer"))
    private Consumer consumer;

    @NotBlank(message = "Account number is required")
    @Size(min = 8, max = 34, message = "Account number must be between 8 and 34 characters (IBAN compliant)")
    @Column(name = "account_number", nullable = false, unique = true, length = 34)
    private String accountNumber;

    @NotBlank(message = "Account holder name is required")
    @Size(min = 2, max = 100, message = "Account holder name must be between 2 and 100 characters")
    @Column(name = "account_holder_name", nullable = false, length = 100)
    private String accountHolderName;

    @NotBlank(message = "Bank code is required")
    @Size(min = 2, max = 20, message = "Bank code must be between 2 and 20 characters")
    @Column(name = "bank_code", nullable = false, length = 20)
    private String bankCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "linked_date")
    private LocalDateTime linkedDate;

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        linkedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum VerificationStatus {
        PENDING, VERIFIED, FAILED, REJECTED
    }
}

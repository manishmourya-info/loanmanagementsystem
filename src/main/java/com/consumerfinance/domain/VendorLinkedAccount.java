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
 * VendorLinkedAccount Entity (T011)
 * 
 * Represents vendor's integrated account for transactions.
 * Each vendor can have max 5 linked accounts (validated in service layer).
 */
@Entity
@Table(name = "vendor_linked_accounts", indexes = {
        @Index(name = "idx_vendor_id", columnList = "vendor_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_account_number", columnList = "account_number", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorLinkedAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "vendor_account_id")
    private UUID vendorAccountId;

    @NotNull(message = "Vendor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vendor_linked_account"))
    private Vendor vendor;

    @NotBlank(message = "Account number is required")
    @Size(min = 8, max = 34, message = "Account number must be between 8 and 34 characters")
    @Column(name = "account_number", nullable = false, unique = true, length = 34)
    private String accountNumber;

    @NotBlank(message = "Account type is required")
    @Column(name = "account_type", nullable = false, length = 50)
    private String accountType; // SETTLEMENT, ESCROW, OPERATING, etc.

    @Column(name = "account_details", nullable = false, columnDefinition = "JSON")
    private String accountDetails; // JSON: {bankCode, ifscCode, accountName, etc.}

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.PENDING;

    @Column(name = "activation_date")
    private LocalDateTime activationDate;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AccountStatus {
        PENDING, ACTIVE, INACTIVE, SUSPENDED, CLOSED
    }
}

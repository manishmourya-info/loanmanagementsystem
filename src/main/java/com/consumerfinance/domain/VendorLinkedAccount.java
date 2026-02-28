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
@Table(name = "vendor_linked_accounts")
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

    @NotNull(message = "Principal account is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "principal_account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vendor_linked_principal_account"))
    private PrincipalAccount principalAccount;

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
}

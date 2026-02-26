package com.consumerfinance.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Consumer Entity (T007)
 * 
 * Represents individual customers in the loan platform.
 * Attributes: ConsumerId, Name, Email, Phone, Identity, Status, KYC status, Timestamps
 * Relationships: 1:1 with PrincipalAccount, 1:N with PersonalLoan
 */
@Entity
@Table(name = "consumers", indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_phone", columnList = "phone"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consumer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "consumer_id")
    private UUID consumerId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone must be in E.164 format (e.g., +14155552671)")
    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @NotBlank(message = "Identity type is required")
    @Column(name = "identity_type", nullable = false, length = 50)
    private String identityType; // AADHAR, PAN, DL, PASSPORT, etc.

    @NotBlank(message = "Identity number is required")
    @Column(name = "identity_number", nullable = false, length = 50)
    private String identityNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ConsumerStatus status = ConsumerStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    @Builder.Default
    private KYCStatus kycStatus = KYCStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;

    // Relationships
    @OneToOne(mappedBy = "consumer", cascade = CascadeType.ALL, orphanRemoval = true)
    private PrincipalAccount principalAccount;

    @OneToMany(mappedBy = "consumer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PersonalLoan> loans;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ConsumerStatus {
        ACTIVE, INACTIVE, SUSPENDED, CLOSED
    }

    public enum KYCStatus {
        PENDING, VERIFIED, REJECTED, EXPIRED
    }
}

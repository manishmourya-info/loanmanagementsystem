package com.consumerfinance.repository;

import com.consumerfinance.domain.VendorLinkedAccount;
import com.consumerfinance.domain.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for VendorLinkedAccount entity
 * T011: Vendor Linked Account repository
 * 
 * Provides database access operations for vendor's linked accounts
 * and principal account mappings.
 */
@Repository
public interface VendorLinkedAccountRepository extends JpaRepository<VendorLinkedAccount, UUID> {

    /**
     * Find all linked accounts for a specific vendor
     */
    List<VendorLinkedAccount> findByVendor(Vendor vendor);

    /**
     * Find all linked accounts for a vendor by vendor ID
     */
    @Query("SELECT va FROM VendorLinkedAccount va WHERE va.vendor.vendorId = :vendorId")
    List<VendorLinkedAccount> findByVendorId(UUID vendorId);

    /**
     * Find active linked accounts for a vendor
     */
    @Query("SELECT va FROM VendorLinkedAccount va WHERE va.vendor.vendorId = :vendorId AND va.status = 'ACTIVE'")
    List<VendorLinkedAccount> findActiveAccountsByVendorId(UUID vendorId);

    /**
     * Find a linked account by account number
     */
    Optional<VendorLinkedAccount> findByAccountNumber(String accountNumber);

    /**
     * Find linked accounts by status
     */
    List<VendorLinkedAccount> findByStatus(VendorLinkedAccount.AccountStatus status);

    /**
     * Count linked accounts for a vendor
     */
    long countByVendor(Vendor vendor);

    /**
     * Count linked accounts for a vendor by vendor ID
     */
    @Query("SELECT COUNT(va) FROM VendorLinkedAccount va WHERE va.vendor.vendorId = :vendorId")
    long countByVendorId(UUID vendorId);

    /**
     * Count active linked accounts for a vendor
     */
    @Query("SELECT COUNT(va) FROM VendorLinkedAccount va WHERE va.vendor.vendorId = :vendorId AND va.status = 'ACTIVE'")
    long countActiveAccountsByVendorId(UUID vendorId);
}

package com.consumerfinance.repository;

import com.consumerfinance.domain.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Vendor entity
 * T011: Vendor repository
 * 
 * Provides database access operations for vendors and their account mappings.
 */
@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    /**
     * Find vendor by registration number
     */
    Optional<Vendor> findByRegistrationNumber(String registrationNumber);

    /**
     * Find vendor by GST number
     */
    Optional<Vendor> findByGstNumber(String gstNumber);

    /**
     * Find vendor by contact email
     */
    Optional<Vendor> findByContactEmail(String contactEmail);

    /**
     * Find all vendors by status
     */
    List<Vendor> findByStatus(Vendor.VendorStatus status);

    /**
     * Find all active vendors
     */
    @Query("SELECT v FROM Vendor v WHERE v.status = 'ACTIVE'")
    List<Vendor> findAllActiveVendors();

    /**
     * Count vendors by status
     */
    long countByStatus(Vendor.VendorStatus status);
}

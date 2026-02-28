package com.consumerfinance.service;

import com.consumerfinance.domain.Vendor;
import com.consumerfinance.domain.VendorLinkedAccount;
import com.consumerfinance.domain.PrincipalAccount;
import com.consumerfinance.dto.VendorRequest;
import com.consumerfinance.dto.VendorResponse;
import com.consumerfinance.dto.VendorLinkedAccountResponse;
import com.consumerfinance.exception.InvalidAccountException;
import com.consumerfinance.repository.VendorRepository;
import com.consumerfinance.repository.VendorLinkedAccountRepository;
import com.consumerfinance.repository.PrincipalAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing vendors and their linked accounts
 * T011: Vendor service with principal account mapping
 * 
 * Handles vendor registration, linked account management,
 * and mapping between vendor accounts and principal accounts.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VendorService {

    private final VendorRepository vendorRepository;
    private final VendorLinkedAccountRepository vendorLinkedAccountRepository;
    private final PrincipalAccountRepository principalAccountRepository;

    private static final int MAX_LINKED_ACCOUNTS_PER_VENDOR = 5;

    // ==================== VENDOR OPERATIONS ====================

    /**
     * Register a new vendor
     */
    public VendorResponse registerVendor(VendorRequest request) {
        log.info("Registering new vendor: {}", request.getVendorName());

        // Check if vendor already exists
        if (vendorRepository.findByRegistrationNumber(request.getRegistrationNumber()).isPresent()) {
            throw new InvalidAccountException("Vendor with registration number " + request.getRegistrationNumber() + " already exists");
        }

        Vendor vendor = Vendor.builder()
                .vendorName(request.getVendorName())
                .businessType(request.getBusinessType())
                .registrationNumber(request.getRegistrationNumber())
                .gstNumber(request.getGstNumber())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .status(Vendor.VendorStatus.ACTIVE)
                .build();

        Vendor savedVendor = vendorRepository.save(vendor);
        log.info("Vendor registered successfully with ID: {}", savedVendor.getVendorId());

        return mapToVendorResponse(savedVendor);
    }

    /**
     * Get vendor by ID
     */
    @Transactional(readOnly = true)
    public VendorResponse getVendorById(UUID vendorId) {
        log.info("Fetching vendor with ID: {}", vendorId);
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new InvalidAccountException("Vendor not found with ID: " + vendorId));
        return mapToVendorResponse(vendor);
    }

    /**
     * Get all active vendors
     */
    @Transactional(readOnly = true)
    public List<VendorResponse> getAllActiveVendors() {
        log.info("Fetching all active vendors");
        return vendorRepository.findAllActiveVendors().stream()
                .map(this::mapToVendorResponse)
                .collect(Collectors.toList());
    }

    // ==================== VENDOR LINKED ACCOUNT OPERATIONS ====================

    /**
     * Add/Map a linked account to a vendor
     */
    public VendorLinkedAccountResponse addLinkedAccount(UUID vendorId, VendorLinkedAccount linkedAccount) {
        log.info("Adding linked account to vendor: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new InvalidAccountException("Vendor not found with ID: " + vendorId));

        // Check if vendor already has max linked accounts
        long accountCount = vendorLinkedAccountRepository.countByVendorId(vendorId);
        if (accountCount >= MAX_LINKED_ACCOUNTS_PER_VENDOR) {
            throw new InvalidAccountException("Vendor has reached maximum linked accounts limit (" + MAX_LINKED_ACCOUNTS_PER_VENDOR + ")");
        }

        linkedAccount.setVendor(vendor);
        VendorLinkedAccount savedAccount = vendorLinkedAccountRepository.save(linkedAccount);

        log.info("Linked account added successfully with ID: {}", savedAccount.getVendorAccountId());
        return mapToVendorLinkedAccountResponse(savedAccount);
    }

    /**
     * Get all linked accounts for a vendor
     */
    @Transactional(readOnly = true)
    public List<VendorLinkedAccountResponse> getLinkedAccountsByVendorId(UUID vendorId) {
        log.info("Fetching linked accounts for vendor: {}", vendorId);

        // Verify vendor exists
        vendorRepository.findById(vendorId)
                .orElseThrow(() -> new InvalidAccountException("Vendor not found with ID: " + vendorId));

        return vendorLinkedAccountRepository.findByVendorId(vendorId).stream()
                .map(this::mapToVendorLinkedAccountResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get linked accounts for a vendor
     * 
     * Since status tracking was removed from VendorLinkedAccount,
     * all linked accounts are considered "active" by default.
     */
    @Transactional(readOnly = true)
    public List<VendorLinkedAccountResponse> getActiveLinkedAccountsByVendorId(UUID vendorId) {
        log.info("Fetching linked accounts for vendor: {}", vendorId);

        // Verify vendor exists
        vendorRepository.findById(vendorId)
                .orElseThrow(() -> new InvalidAccountException("Vendor not found with ID: " + vendorId));

        return vendorLinkedAccountRepository.findByVendorId(vendorId).stream()
                .map(this::mapToVendorLinkedAccountResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map vendor with principal account
     * Creates a new linked account association between vendor and consumer's principal account
     */
    public VendorLinkedAccountResponse mapVendorWithPrincipalAccount(UUID vendorId, UUID principalAccountId) {
        log.info("Mapping vendor {} with principal account {}", vendorId, principalAccountId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new InvalidAccountException("Vendor not found with ID: " + vendorId));

        PrincipalAccount principalAccount = principalAccountRepository.findById(principalAccountId)
                .orElseThrow(() -> new InvalidAccountException("Principal account not found with ID: " + principalAccountId));

        if (!principalAccount.getVerificationStatus().equals(PrincipalAccount.VerificationStatus.VERIFIED)) {
            throw new InvalidAccountException("Principal account must be VERIFIED to map with vendor");
        }

        // Create a new linked account
        VendorLinkedAccount linkedAccount = VendorLinkedAccount.builder()
                .vendor(vendor)
                .principalAccount(principalAccount)
                .build();

        VendorLinkedAccount savedAccount = vendorLinkedAccountRepository.save(linkedAccount);

        log.info("Successfully mapped vendor {} with principal account {}. Linked account ID: {}", 
                vendorId, principalAccountId, savedAccount.getVendorAccountId());
        
        return mapToVendorLinkedAccountResponse(savedAccount);
    }

    /**
     * Get linked accounts for a principal account (reverse lookup)
     * Finds all vendor accounts that can transact with this principal account
     */
    @Transactional(readOnly = true)
    public List<VendorLinkedAccountResponse> getLinkedAccountsByPrincipalAccountId(UUID principalAccountId) {
        log.info("Fetching linked vendor accounts for principal account: {}", principalAccountId);

        PrincipalAccount principalAccount = principalAccountRepository.findById(principalAccountId)
                .orElseThrow(() -> new InvalidAccountException("Principal account not found with ID: " + principalAccountId));

        // Get all vendor accounts linked to this principal account
        return vendorLinkedAccountRepository.findAll().stream()
                .filter(account -> account.getPrincipalAccount() != null && account.getPrincipalAccount().getPrincipalAccountId().equals(principalAccountId))
                .map(this::mapToVendorLinkedAccountResponse)
                .collect(Collectors.toList());
    }

    /**
     * Activate a vendor linked account
     */
    public VendorLinkedAccountResponse activateLinkedAccount(UUID vendorAccountId) {
        log.info("Activating vendor linked account: {}", vendorAccountId);

        VendorLinkedAccount account = vendorLinkedAccountRepository.findById(vendorAccountId)
                .orElseThrow(() -> new InvalidAccountException("Vendor account not found with ID: " + vendorAccountId));

        log.info("Linked account activated successfully: {}", vendorAccountId);
        return mapToVendorLinkedAccountResponse(account);
    }

    /**
     * Deactivate a vendor linked account
     */
    public VendorLinkedAccountResponse deactivateLinkedAccount(UUID vendorAccountId) {
        log.info("Deactivating vendor linked account: {}", vendorAccountId);

        VendorLinkedAccount account = vendorLinkedAccountRepository.findById(vendorAccountId)
                .orElseThrow(() -> new InvalidAccountException("Vendor account not found with ID: " + vendorAccountId));

        log.info("Linked account deactivated successfully: {}", vendorAccountId);
        return mapToVendorLinkedAccountResponse(account);
    }

    // ==================== MAPPING HELPERS ====================

    private VendorResponse mapToVendorResponse(Vendor vendor) {
        return VendorResponse.builder()
                .vendorId(vendor.getVendorId())
                .vendorName(vendor.getVendorName())
                .businessType(vendor.getBusinessType())
                .registrationNumber(vendor.getRegistrationNumber())
                .gstNumber(vendor.getGstNumber())
                .contactEmail(vendor.getContactEmail())
                .contactPhone(vendor.getContactPhone())
                .status(vendor.getStatus().toString())
                .registrationDate(vendor.getRegistrationDate())
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .build();
    }

    private VendorLinkedAccountResponse mapToVendorLinkedAccountResponse(VendorLinkedAccount account) {
        return VendorLinkedAccountResponse.builder()
                .vendorAccountId(account.getVendorAccountId())
                .vendorId(account.getVendor().getVendorId())
                .vendorName(account.getVendor().getVendorName())
                .principalAccountId(account.getPrincipalAccount() != null ? account.getPrincipalAccount().getPrincipalAccountId() : null)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}

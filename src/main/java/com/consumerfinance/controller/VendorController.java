package com.consumerfinance.controller;

import com.consumerfinance.domain.VendorLinkedAccount;
import com.consumerfinance.dto.VendorRequest;
import com.consumerfinance.dto.VendorResponse;
import com.consumerfinance.dto.VendorLinkedAccountResponse;
import com.consumerfinance.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Vendor and Vendor Linked Account management
 * T011: Vendor management endpoints
 * 
 * Provides endpoints for:
 * - Vendor registration and retrieval
 * - Linked account management
 * - Mapping vendors with principal accounts
 * - Getting linked accounts by vendor or principal account
 */
@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vendor Management", description = "API for vendor registration, linked account management, and account mapping")
public class VendorController {

    private final VendorService vendorService;

    // ==================== VENDOR ENDPOINTS ====================

    /**
     * Register a new vendor
     * POST /api/v1/vendors/register
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new vendor", description = "Creates a new vendor account with business details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Vendor registered successfully",
                     content = @Content(schema = @Schema(implementation = VendorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "409", description = "Vendor already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<VendorResponse> registerVendor(@Valid @RequestBody VendorRequest request) {
        log.info("Request to register vendor: {}", request.getVendorName());
        VendorResponse response = vendorService.registerVendor(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get vendor by ID
     * GET /api/v1/vendors/{vendorId}
     */
    @GetMapping("/{vendorId}")
    @Operation(summary = "Get vendor details", description = "Retrieves details of a specific vendor by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vendor retrieved successfully",
                     content = @Content(schema = @Schema(implementation = VendorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Vendor not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<VendorResponse> getVendorById(@PathVariable UUID vendorId) {
        log.info("Request to fetch vendor with ID: {}", vendorId);
        VendorResponse response = vendorService.getVendorById(vendorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all active vendors
     * GET /api/v1/vendors/active
     */
    @GetMapping("/active")
    @Operation(summary = "Get all active vendors", description = "Retrieves a list of all active vendors")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active vendors retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<VendorResponse>> getAllActiveVendors() {
        log.info("Request to fetch all active vendors");
        List<VendorResponse> response = vendorService.getAllActiveVendors();
        return ResponseEntity.ok(response);
    }

    // ==================== VENDOR LINKED ACCOUNT ENDPOINTS ====================

    /**
     * Add a linked account to a vendor
     * POST /api/v1/vendors/{vendorId}/linked-accounts
     */
    @PostMapping("/{vendorId}/linked-accounts")
    @Operation(summary = "Add linked account to vendor", description = "Creates a new linked account for a vendor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Linked account added successfully",
                     content = @Content(schema = @Schema(implementation = VendorLinkedAccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Vendor not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<VendorLinkedAccountResponse> addLinkedAccount(
            @PathVariable UUID vendorId,
            @Valid @RequestBody VendorLinkedAccount linkedAccount) {
        log.info("Request to add linked account for vendor: {}", vendorId);
        VendorLinkedAccountResponse response = vendorService.addLinkedAccount(vendorId, linkedAccount);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all linked accounts for a vendor
     * GET /api/v1/vendors/{vendorId}/linked-accounts
     */
    @GetMapping("/{vendorId}/linked-accounts")
    @Operation(summary = "Get vendor linked accounts", description = "Retrieves all linked accounts for a specific vendor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Linked accounts retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Vendor not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<VendorLinkedAccountResponse>> getLinkedAccountsByVendor(@PathVariable UUID vendorId) {
        log.info("Request to fetch linked accounts for vendor: {}", vendorId);
        List<VendorLinkedAccountResponse> response = vendorService.getLinkedAccountsByVendorId(vendorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active linked accounts for a vendor
     * GET /api/v1/vendors/{vendorId}/linked-accounts/active
     */
    @GetMapping("/{vendorId}/linked-accounts/active")
    @Operation(summary = "Get active linked accounts", description = "Retrieves active linked accounts for a specific vendor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active linked accounts retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Vendor not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<VendorLinkedAccountResponse>> getActiveLinkedAccounts(@PathVariable UUID vendorId) {
        log.info("Request to fetch active linked accounts for vendor: {}", vendorId);
        List<VendorLinkedAccountResponse> response = vendorService.getActiveLinkedAccountsByVendorId(vendorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Map vendor linked account with principal account
     * POST /api/v1/vendors/map-principal-account
     */
    @PostMapping("/map-principal-account")
    @Operation(summary = "Map vendor with principal account", description = "Links a vendor account to a principal account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Vendor mapped successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid account IDs"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> mapVendorWithPrincipalAccount(
            @RequestParam UUID vendorAccountId,
            @RequestParam UUID principalAccountId) {
        log.info("Request to map vendor account {} with principal account {}", vendorAccountId, principalAccountId);
        vendorService.mapVendorWithPrincipalAccount(vendorAccountId, principalAccountId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get linked accounts by principal account
     * GET /api/v1/vendors/linked-accounts/principal/{principalAccountId}
     */
    @GetMapping("/linked-accounts/principal/{principalAccountId}")
    @Operation(summary = "Get linked accounts by principal account", description = "Retrieves all vendor linked accounts mapped to a principal account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Linked accounts retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Principal account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<VendorLinkedAccountResponse>> getLinkedAccountsByPrincipalAccount(
            @PathVariable UUID principalAccountId) {
        log.info("Request to fetch linked vendor accounts for principal account: {}", principalAccountId);
        List<VendorLinkedAccountResponse> response = vendorService.getLinkedAccountsByPrincipalAccountId(principalAccountId);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a vendor linked account
     * PUT /api/v1/vendors/linked-accounts/{vendorAccountId}/activate
     */
    @PutMapping("/linked-accounts/{vendorAccountId}/activate")
    @Operation(summary = "Activate linked account", description = "Activates a vendor linked account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Linked account activated successfully",
                     content = @Content(schema = @Schema(implementation = VendorLinkedAccountResponse.class))),
        @ApiResponse(responseCode = "404", description = "Linked account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<VendorLinkedAccountResponse> activateLinkedAccount(@PathVariable UUID vendorAccountId) {
        log.info("Request to activate vendor linked account: {}", vendorAccountId);
        VendorLinkedAccountResponse response = vendorService.activateLinkedAccount(vendorAccountId);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a vendor linked account
     * PUT /api/v1/vendors/linked-accounts/{vendorAccountId}/deactivate
     */
    @PutMapping("/linked-accounts/{vendorAccountId}/deactivate")
    @Operation(summary = "Deactivate linked account", description = "Deactivates a vendor linked account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Linked account deactivated successfully",
                     content = @Content(schema = @Schema(implementation = VendorLinkedAccountResponse.class))),
        @ApiResponse(responseCode = "404", description = "Linked account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<VendorLinkedAccountResponse> deactivateLinkedAccount(@PathVariable UUID vendorAccountId) {
        log.info("Request to deactivate vendor linked account: {}", vendorAccountId);
        VendorLinkedAccountResponse response = vendorService.deactivateLinkedAccount(vendorAccountId);
        return ResponseEntity.ok(response);
    }
}

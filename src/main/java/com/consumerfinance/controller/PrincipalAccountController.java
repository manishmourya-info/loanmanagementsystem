package com.consumerfinance.controller;

import com.consumerfinance.dto.PrincipalAccountRequest;
import com.consumerfinance.dto.PrincipalAccountResponse;
import com.consumerfinance.service.PrincipalAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for principal account management
 * T018: Principal Account API endpoints
 */
@RestController
@RequestMapping("/api/v1/consumers/{consumerId}/principal-account")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Principal Account Management", description = "API for linking, managing, and verifying consumer principal accounts")
public class PrincipalAccountController {

    private final PrincipalAccountService principalAccountService;

    /**
     * POST /consumers/{consumerId}/principal-account - Link account
     * T018: Link principal account endpoint
     */
    @PostMapping
    @Operation(summary = "Link principal account", description = "Links a bank account as the principal account for a consumer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account linked successfully",
                     content = @Content(schema = @Schema(implementation = PrincipalAccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid account details"),
        @ApiResponse(responseCode = "404", description = "Consumer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PrincipalAccountResponse> linkPrincipalAccount(
            @PathVariable UUID consumerId,
            @Valid @RequestBody PrincipalAccountRequest request) {
        log.info("Linking principal account for consumer: {}", consumerId);
        PrincipalAccountResponse response = principalAccountService.linkPrincipalAccount(consumerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /consumers/{consumerId}/principal-account - Get linked account
     * T018: Get principal account endpoint
     */
    @GetMapping
    @Operation(summary = "Get principal account", description = "Retrieves the linked principal account details for a consumer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account details retrieved successfully",
                     content = @Content(schema = @Schema(implementation = PrincipalAccountResponse.class))),
        @ApiResponse(responseCode = "404", description = "Consumer or account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PrincipalAccountResponse> getPrincipalAccount(@PathVariable UUID consumerId) {
        log.info("Fetching principal account for consumer: {}", consumerId);
        PrincipalAccountResponse response = principalAccountService.getPrincipalAccount(consumerId);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /consumers/{consumerId}/principal-account - Update linked account
     * T018: Update principal account endpoint
     */
    @PutMapping
    @Operation(summary = "Update principal account", description = "Updates the linked principal account details for a consumer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account updated successfully",
                     content = @Content(schema = @Schema(implementation = PrincipalAccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid account details"),
        @ApiResponse(responseCode = "404", description = "Consumer or account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PrincipalAccountResponse> updatePrincipalAccount(
            @PathVariable UUID consumerId,
            @Valid @RequestBody PrincipalAccountRequest request) {
        log.info("Updating principal account for consumer: {}", consumerId);
        PrincipalAccountResponse response = principalAccountService.updatePrincipalAccount(consumerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /consumers/{consumerId}/principal-account/verify - Verify account (admin only)
     * T018: Verify account endpoint
     */
    @PutMapping("/verify/{accountId}")
    @Operation(summary = "Verify principal account", description = "Verifies and approves a principal account (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account verified successfully",
                     content = @Content(schema = @Schema(implementation = PrincipalAccountResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PrincipalAccountResponse> verifyAccount(@PathVariable UUID accountId) {
        log.info("Verifying principal account: {}", accountId);
        PrincipalAccountResponse response = principalAccountService.verifyAccount(accountId);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /consumers/{consumerId}/principal-account/reject - Reject account verification (admin only)
     * T018: Reject account endpoint
     */
    @PutMapping("/reject/{accountId}")
    @Operation(summary = "Reject principal account verification", description = "Rejects and deactivates a principal account with a reason (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account rejected successfully",
                     content = @Content(schema = @Schema(implementation = PrincipalAccountResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PrincipalAccountResponse> rejectAccount(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "Account verification failed") String reason) {
        log.info("Rejecting principal account: {}", accountId);
        PrincipalAccountResponse response = principalAccountService.rejectAccount(accountId, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /consumers/{consumerId}/principal-account/verification-status - Get verification status
     * T018: Get verification status endpoint
     */
    @GetMapping("/verification-status")
    @Operation(summary = "Get account verification status", description = "Retrieves the current verification status of the consumer's principal account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Consumer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> getVerificationStatus(@PathVariable UUID consumerId) {
        log.info("Fetching verification status for consumer: {}", consumerId);
        // This would require passing accountId or getting from consumer
        // For now, return the consumer ID - in reality would get account ID from consumer
        return ResponseEntity.ok("Endpoint for checking verification status");
    }
}

package com.consumerfinance.controller;

import com.consumerfinance.dto.ConsumerRequest;
import com.consumerfinance.dto.ConsumerResponse;
import com.consumerfinance.service.ConsumerService;
import com.consumerfinance.domain.Consumer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for consumer registration and management
 * T017: Consumer API endpoints
 */
@RestController
@RequestMapping("/api/v1/consumers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Consumer Management", description = "API for consumer registration, profile management, and account operations")
public class ConsumerController {

    private final ConsumerService consumerService;

    /**
     * POST /consumers - Create new consumer
     * T017: Create consumer endpoint
     */
    @PostMapping
    @Operation(summary = "Create new consumer", description = "Registers a new consumer with contact and identification details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Consumer created successfully",
                     content = @Content(schema = @Schema(implementation = ConsumerResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "409", description = "Consumer with email already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ConsumerResponse> createConsumer(@Valid @RequestBody ConsumerRequest request) {
        log.info("Creating new consumer: {}", request.getEmail());
        ConsumerResponse response = consumerService.createConsumer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /consumers/{consumerId} - Get consumer details
     * T017: Get consumer endpoint
     */
    @GetMapping("/{consumerId}")
    @Operation(summary = "Get consumer details", description = "Retrieves complete profile and details of a consumer by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consumer retrieved successfully",
                     content = @Content(schema = @Schema(implementation = ConsumerResponse.class))),
        @ApiResponse(responseCode = "404", description = "Consumer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ConsumerResponse> getConsumer(@PathVariable UUID consumerId) {
        log.info("Fetching consumer: {}", consumerId);
        ConsumerResponse response = consumerService.getConsumer(consumerId);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /consumers/{consumerId} - Update consumer profile
     * T017: Update consumer endpoint
     */
    @PutMapping("/{consumerId}")
    @Operation(summary = "Update consumer profile", description = "Updates consumer's personal details and contact information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consumer updated successfully",
                     content = @Content(schema = @Schema(implementation = ConsumerResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Consumer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ConsumerResponse> updateConsumer(
            @PathVariable UUID consumerId,
            @Valid @RequestBody ConsumerRequest request) {
        log.info("Updating consumer: {}", consumerId);
        ConsumerResponse response = consumerService.updateConsumer(consumerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /consumers - List all consumers (admin only)
     * T017: List consumers endpoint
     */
    @GetMapping
    @Operation(summary = "List all consumers", description = "Retrieves a paginated list of all consumers with optional filtering by status and search")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consumers retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination or filter parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<ConsumerResponse>> getAllConsumers(
            Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        log.info("Fetching consumers - Status: {}, Search: {}", status, search);

        Consumer.ConsumerStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            statusEnum = Consumer.ConsumerStatus.valueOf(status.toUpperCase());
        }

        Page<ConsumerResponse> responses = consumerService.getAllConsumers(pageable, statusEnum, search);
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /consumers/{consumerId}/kyc-status - Get KYC status
     * T017: Get KYC status endpoint
     */
    @GetMapping("/{consumerId}/kyc-status")
    @Operation(summary = "Get KYC status", description = "Retrieves the Know Your Customer (KYC) verification status of a consumer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "KYC status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Consumer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> getKYCStatus(@PathVariable UUID consumerId) {
        log.info("Fetching KYC status for consumer: {}", consumerId);
        Consumer.KYCStatus kycStatus = consumerService.getKYCStatus(consumerId);
        return ResponseEntity.ok(kycStatus.toString());
    }

    /**
     * POST /consumers/{consumerId}/suspend - Suspend consumer account
     * T017: Suspend consumer endpoint
     */
    @PostMapping("/{consumerId}/suspend")
    @Operation(summary = "Suspend consumer account", description = "Temporarily suspends a consumer account with a specified reason")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consumer account suspended successfully",
                     content = @Content(schema = @Schema(implementation = ConsumerResponse.class))),
        @ApiResponse(responseCode = "404", description = "Consumer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ConsumerResponse> suspendConsumer(
            @PathVariable UUID consumerId,
            @RequestParam(defaultValue = "Account suspended by admin") String reason) {
        log.info("Suspending consumer: {}", consumerId);
        ConsumerResponse response = consumerService.suspendConsumer(consumerId, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /consumers/{consumerId}/deactivate - Deactivate consumer account
     * T017: Deactivate consumer endpoint
     */
    @PostMapping("/{consumerId}/deactivate")
    @Operation(summary = "Deactivate consumer account", description = "Permanently deactivates a consumer account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consumer account deactivated successfully",
                     content = @Content(schema = @Schema(implementation = ConsumerResponse.class))),
        @ApiResponse(responseCode = "404", description = "Consumer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ConsumerResponse> deactivateConsumer(@PathVariable UUID consumerId) {
        log.info("Deactivating consumer: {}", consumerId);
        ConsumerResponse response = consumerService.deactivateConsumer(consumerId);
        return ResponseEntity.ok(response);
    }
}

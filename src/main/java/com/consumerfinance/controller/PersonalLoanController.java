package com.consumerfinance.controller;

import com.consumerfinance.dto.CreateLoanRequest;
import com.consumerfinance.dto.LoanResponse;
import com.consumerfinance.service.PersonalLoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for personal loan operations.
 * Provides endpoints for loan creation, retrieval, approval, rejection, disbursement, and management.
 * Phases 4-6: T022-T028
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/loans")
@Tag(name = "Personal Loans", description = "API for managing personal loans")
public class PersonalLoanController {

    private final PersonalLoanService loanService;

    public PersonalLoanController(PersonalLoanService loanService) {
        this.loanService = loanService;
    }

    /**
     * Create a new personal loan.
     * T022: Implement POST /loans endpoint
     *
     * @param request the loan creation request
     * @return the created loan details with 201 status
     */
    @PostMapping
    @Operation(summary = "Create a new personal loan", 
               description = "Creates a new personal loan with automatic EMI calculation and repayment schedule generation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Loan created successfully",
                     content = @Content(schema = @Schema(implementation = LoanResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "409", description = "Consumer not eligible or already has active loan"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        log.info("REST: POST /api/v1/loans - Creating loan for customer: {}", request.getCustomerId());
        LoanResponse response = loanService.createLoan(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get loan details by ID.
     * T026: Implement GET /loans/{id} endpoint
     *
     * @param loanId the loan ID (UUID)
     * @return the loan details
     */
    @GetMapping("/{loanId}")
    @Operation(summary = "Get loan details by ID",
               description = "Retrieves complete details of a personal loan including EMI and repayment information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan details retrieved successfully",
                     content = @Content(schema = @Schema(implementation = LoanResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loan not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoanResponse> getLoan(
            @PathVariable
            @Parameter(description = "Unique loan identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID loanId) {
        log.info("REST: GET /api/v1/loans/{} - Retrieving loan details", loanId);
        LoanResponse response = loanService.getLoan(loanId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all loans for a consumer.
     * T027: Implement endpoint for consumer loans
     *
     * @param consumerId the consumer ID (UUID)
     * @return list of loans for the consumer
     */
    @GetMapping("/consumer/{consumerId}")
    @Operation(summary = "Get all loans for a consumer",
               description = "Retrieves all personal loans (active and inactive) for a specific consumer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loans retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Consumer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<LoanResponse>> getLoansByConsumerId(
            @PathVariable
            @Parameter(description = "Unique consumer identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID consumerId) {
        log.info("REST: GET /api/v1/loans/consumer/{} - Retrieving all loans", consumerId);
        List<LoanResponse> response = loanService.getConsumerLoans(consumerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all loans for a customer by customer ID string.
     *
     * @param customerId the customer ID
     * @return list of loans for the customer
     */
    @GetMapping("/by-customer/{customerId}")
    @Operation(summary = "Get all loans for a customer",
               description = "Retrieves all personal loans (active and inactive) for a specific customer ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loans retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<LoanResponse>> getLoansByCustomerId(
            @PathVariable
            @Parameter(description = "Unique customer identifier", example = "CUST123456")
            String customerId) {
        log.info("REST: GET /api/v1/loans/by-customer/{} - Retrieving all loans", customerId);
        List<LoanResponse> response = loanService.getLoansByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active loans for a customer.
     *
     * @param customerId the customer ID
     * @return list of active loans
     */
    @GetMapping("/by-customer/{customerId}/active")
    @Operation(summary = "Get active loans for a customer",
               description = "Retrieves only active personal loans for a specific customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active loans retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<LoanResponse>> getActiveLoansByCustomerId(
            @PathVariable
            @Parameter(description = "Unique customer identifier", example = "CUST123456")
            String customerId) {
        log.info("REST: GET /api/v1/loans/by-customer/{}/active - Retrieving active loans", customerId);
        List<LoanResponse> response = loanService.getActiveLoansByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Approve a pending loan.
     * T024: Implement PUT /loans/{id}/approve endpoint
     *
     * @param loanId the loan ID to approve
     * @return the approved loan details
     */
    @PutMapping("/{loanId}/approve")
    @Operation(summary = "Approve a pending loan",
               description = "Approves a loan that is in PENDING status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan approved successfully",
                     content = @Content(schema = @Schema(implementation = LoanResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loan not found"),
        @ApiResponse(responseCode = "409", description = "Invalid operation - loan is not in PENDING status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoanResponse> approveLoan(
            @PathVariable
            @Parameter(description = "Unique loan identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID loanId,
            @RequestParam(required = false, defaultValue = "Approved")
            String remarks) {
        log.info("REST: PUT /api/v1/loans/{}/approve - Approving loan", loanId);
        LoanResponse response = loanService.approveLoan(loanId, remarks);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject a pending loan.
     * T024: Implement PUT /loans/{id}/reject endpoint
     *
     * @param loanId the loan ID to reject
     * @param reason the rejection reason
     * @return the rejected loan details
     */
    @PutMapping("/{loanId}/reject")
    @Operation(summary = "Reject a pending loan",
               description = "Rejects a loan that is in PENDING status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan rejected successfully",
                     content = @Content(schema = @Schema(implementation = LoanResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loan not found"),
        @ApiResponse(responseCode = "409", description = "Invalid operation - loan is not in PENDING status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoanResponse> rejectLoan(
            @PathVariable
            @Parameter(description = "Unique loan identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID loanId,
            @RequestParam(required = false, defaultValue = "Rejected")
            String reason) {
        log.info("REST: PUT /api/v1/loans/{}/reject - Rejecting loan", loanId);
        LoanResponse response = loanService.rejectLoan(loanId, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Disburse an approved loan and generate repayment schedule.
     * T025: Implement PUT /loans/{id}/disburse endpoint
     *
     * @param loanId the loan ID to disburse
     * @return the disbursed loan details
     */
    @PutMapping("/{loanId}/disburse")
    @Operation(summary = "Disburse an approved loan",
               description = "Disburses a loan that is in APPROVED status and generates repayment schedule")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan disbursed successfully",
                     content = @Content(schema = @Schema(implementation = LoanResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loan not found"),
        @ApiResponse(responseCode = "409", description = "Invalid operation - loan is not in APPROVED status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoanResponse> disburseLoan(
            @PathVariable
            @Parameter(description = "Unique loan identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID loanId) {
        log.info("REST: PUT /api/v1/loans/{}/disburse - Disbursing loan", loanId);
        LoanResponse response = loanService.disburseLoan(loanId);
        return ResponseEntity.ok(response);
    }

    /**
     * Close a fully repaid loan.
     *
     * @param loanId the loan ID to close
     * @return the closed loan details
     */
    @PutMapping("/{loanId}/close")
    @Operation(summary = "Close a personal loan",
               description = "Marks a loan as CLOSED (requires zero outstanding balance)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan closed successfully",
                     content = @Content(schema = @Schema(implementation = LoanResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loan not found"),
        @ApiResponse(responseCode = "409", description = "Invalid operation - loan cannot be closed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoanResponse> closeLoan(
            @PathVariable
            @Parameter(description = "Unique loan identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID loanId) {
        log.info("REST: PUT /api/v1/loans/{}/close - Closing loan", loanId);
        LoanResponse response = loanService.closeLoan(loanId);
        return ResponseEntity.ok(response);
    }

}


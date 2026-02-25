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
import java.util.List;

/**
 * REST Controller for personal loan operations.
 * Provides endpoints for loan creation, retrieval, and management.
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
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        log.info("REST: POST /api/v1/loans - Creating loan for customer: {}", request.getCustomerId());
        LoanResponse response = loanService.createLoan(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get loan details by ID.
     *
     * @param loanId the loan ID
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
            @Parameter(description = "Unique loan identifier", example = "1")
            Long loanId) {
        log.info("REST: GET /api/v1/loans/{} - Retrieving loan details", loanId);
        LoanResponse response = loanService.getLoan(loanId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all loans for a customer.
     *
     * @param customerId the customer ID
     * @return list of loans for the customer
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all loans for a customer",
               description = "Retrieves all personal loans (active and inactive) for a specific customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loans retrieved successfully",
                     content = @Content(schema = @Schema(implementation = LoanResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<LoanResponse>> getLoansByCustomerId(
            @PathVariable
            @Parameter(description = "Unique customer identifier", example = "CUST123456")
            String customerId) {
        log.info("REST: GET /api/v1/loans/customer/{} - Retrieving all loans", customerId);
        List<LoanResponse> response = loanService.getLoansByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active loans for a customer.
     *
     * @param customerId the customer ID
     * @return list of active loans
     */
    @GetMapping("/customer/{customerId}/active")
    @Operation(summary = "Get active loans for a customer",
               description = "Retrieves only active personal loans for a specific customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active loans retrieved successfully",
                     content = @Content(schema = @Schema(implementation = LoanResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<LoanResponse>> getActiveLoansByCustomerId(
            @PathVariable
            @Parameter(description = "Unique customer identifier", example = "CUST123456")
            String customerId) {
        log.info("REST: GET /api/v1/loans/customer/{}/active - Retrieving active loans", customerId);
        List<LoanResponse> response = loanService.getActiveLoansByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Close a loan.
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
            @Parameter(description = "Unique loan identifier", example = "1")
            Long loanId) {
        log.info("REST: PUT /api/v1/loans/{}/close - Closing loan", loanId);
        LoanResponse response = loanService.closeLoan(loanId);
        return ResponseEntity.ok(response);
    }

}

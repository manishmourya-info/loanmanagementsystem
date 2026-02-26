package com.consumerfinance.controller;

import com.consumerfinance.dto.RepaymentResponse;
import com.consumerfinance.service.LoanRepaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for loan repayment operations.
 * Provides endpoints for processing payments and retrieving repayment schedules.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/repayments")
@Tag(name = "Loan Repayments", description = "API for managing loan repayments and payments")
public class LoanRepaymentController {

    private final LoanRepaymentService repaymentService;

    public LoanRepaymentController(LoanRepaymentService repaymentService) {
        this.repaymentService = repaymentService;
    }

    /**
     * Process a loan repayment.
     *
     * @param loanId the loan ID
     * @param installmentNumber the installment number
     * @param amountPaid the amount being paid
     * @return the updated repayment details
     */
    @PostMapping("/{loanId}/installment/{installmentNumber}/pay")
    @Operation(summary = "Process loan repayment",
               description = "Processes a payment for a specific loan installment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Repayment processed successfully",
                     content = @Content(schema = @Schema(implementation = RepaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters or invalid repayment"),
        @ApiResponse(responseCode = "404", description = "Loan or repayment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RepaymentResponse> processRepayment(
            @PathVariable
            @Parameter(description = "Unique loan identifier", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID loanId,
            @PathVariable
            @Parameter(description = "Installment number", example = "1")
            Integer installmentNumber,
            @RequestParam
            @Parameter(description = "Amount being paid", example = "9638.22")
            BigDecimal amountPaid) {
        log.info("REST: POST /api/v1/repayments/{}/installment/{}/pay - Processing payment of {}",
                loanId, installmentNumber, amountPaid);
        RepaymentResponse response = repaymentService.processRepayment(loanId, installmentNumber, amountPaid);
        return ResponseEntity.ok(response);
    }

    /**
     * Get repayment details.
     *
     * @param loanId the loan ID
     * @param installmentNumber the installment number
     * @return the repayment details
     */
    @GetMapping("/{loanId}/installment/{installmentNumber}")
    @Operation(summary = "Get repayment details",
               description = "Retrieves details of a specific installment payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Repayment details retrieved successfully",
                     content = @Content(schema = @Schema(implementation = RepaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loan or repayment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RepaymentResponse> getRepayment(
            @PathVariable
            @Parameter(description = "Unique loan identifier", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID loanId,
            @PathVariable
            @Parameter(description = "Installment number", example = "1")
            Integer installmentNumber) {
        log.info("REST: GET /api/v1/repayments/{}/installment/{} - Retrieving repayment details",
                loanId, installmentNumber);
        RepaymentResponse response = repaymentService.getRepayment(loanId, installmentNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all repayments for a loan.
     *
     * @param loanId the loan ID
     * @return list of repayments
     */
    @GetMapping("/{loanId}")
    @Operation(summary = "Get all repayments for a loan",
               description = "Retrieves the complete repayment schedule for a loan")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Repayments retrieved successfully",
                     content = @Content(schema = @Schema(implementation = RepaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loan not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<RepaymentResponse>> getRepaymentsByLoanId(
            @PathVariable
            @Parameter(description = "Unique loan identifier", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID loanId) {
        log.info("REST: GET /api/v1/repayments/{} - Retrieving all repayments", loanId);
        List<RepaymentResponse> response = repaymentService.getRepaymentsByLoanId(loanId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending repayments for a loan.
     *
     * @param loanId the loan ID
     * @return list of pending repayments
     */
    @GetMapping("/{loanId}/pending")
    @Operation(summary = "Get pending repayments for a loan",
               description = "Retrieves only the pending (unpaid) installments for a loan")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pending repayments retrieved successfully",
                     content = @Content(schema = @Schema(implementation = RepaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loan not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<RepaymentResponse>> getPendingRepaymentsByLoanId(
            @PathVariable
            @Parameter(description = "Unique loan identifier", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID loanId) {
        log.info("REST: GET /api/v1/repayments/{}/pending - Retrieving pending repayments", loanId);
        List<RepaymentResponse> response = repaymentService.getPendingRepaymentsByLoanId(loanId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all overdue repayments.
     *
     * @return list of overdue repayments
     */
    @GetMapping("/overdue/list")
    @Operation(summary = "Get all overdue repayments",
               description = "Retrieves all overdue installments across all loans")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Overdue repayments retrieved successfully",
                     content = @Content(schema = @Schema(implementation = RepaymentResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<RepaymentResponse>> getOverdueRepayments() {
        log.info("REST: GET /api/v1/repayments/overdue/list - Retrieving overdue repayments");
        List<RepaymentResponse> response = repaymentService.getOverdueRepayments();
        return ResponseEntity.ok(response);
    }

}

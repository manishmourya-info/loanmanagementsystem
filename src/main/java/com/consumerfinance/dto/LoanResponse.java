package com.consumerfinance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for personal loan details.
 * Phase 4-6: T022-T028, T029-T034, T035-T038
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Personal loan details response")
public class LoanResponse {

    @Schema(description = "Unique loan identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Customer ID", example = "CUST123456")
    private String customerId;

    @Schema(description = "Loan principal amount", example = "500000")
    private BigDecimal principalAmount;

    @Schema(description = "Annual interest rate", example = "10.5")
    private BigDecimal annualInterestRate;

    @Schema(description = "Total loan tenure in months", example = "60")
    private Integer loanTenureMonths;

    @Schema(description = "Monthly EMI amount", example = "9638.22")
    private BigDecimal monthlyEMI;

    @Schema(description = "Total interest payable", example = "78293.2")
    private BigDecimal totalInterestPayable;

    @Schema(description = "Outstanding balance", example = "425000")
    private BigDecimal outstandingBalance;

    @Schema(description = "Remaining tenure in months", example = "50")
    private Integer remainingTenure;

    @Schema(description = "Current loan status", example = "ACTIVE")
    private String status;

    @Schema(description = "Loan creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Loan update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Loan approval timestamp")
    private LocalDateTime approvedAt;

    @Schema(description = "Loan rejection timestamp")
    private LocalDateTime rejectedAt;

    @Schema(description = "Loan closure timestamp (if closed)")
    private LocalDateTime closedAt;

    @Schema(description = "Approval remarks", example = "Approved with conditions")
    private String approvalRemarks;

    @Schema(description = "Rejection reason", example = "Insufficient income")
    private String rejectionReason;

}


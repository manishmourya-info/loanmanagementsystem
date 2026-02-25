package com.consumerfinance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Response DTO for EMI calculation results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "EMI calculation result")
public class EMICalculationResponse {

    @Schema(description = "Monthly EMI amount", example = "9638.22")
    private BigDecimal monthlyEMI;

    @Schema(description = "Total amount to be paid", example = "578293.2")
    private BigDecimal totalAmount;

    @Schema(description = "Total interest to be paid", example = "78293.2")
    private BigDecimal totalInterest;

    @Schema(description = "Principal amount", example = "500000")
    private BigDecimal principal;

    @Schema(description = "Annual interest rate", example = "10.5")
    private BigDecimal annualInterestRate;

    @Schema(description = "Loan tenure in months", example = "60")
    private Integer tenureMonths;

}

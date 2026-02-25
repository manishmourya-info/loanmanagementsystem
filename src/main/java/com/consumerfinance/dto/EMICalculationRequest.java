package com.consumerfinance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Request DTO for EMI calculation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "EMI calculation request parameters")
public class EMICalculationRequest {

    @Schema(description = "Principal loan amount", example = "500000")
    private BigDecimal principalAmount;

    @Schema(description = "Annual interest rate in percentage", example = "10.5")
    private BigDecimal annualInterestRate;

    @Schema(description = "Loan tenure in months", example = "60")
    private Integer tenureMonths;

}

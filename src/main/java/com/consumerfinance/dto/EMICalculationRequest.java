package com.consumerfinance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Principal amount is required")
    @Min(value = 1, message = "Principal amount must be greater than zero")
    @Schema(description = "Principal loan amount", example = "500000")
    private BigDecimal principalAmount;

    @NotNull(message = "Annual interest rate is required")
    @Min(value = 0, message = "Annual interest rate must be greater than or equal to zero")
    @Schema(description = "Annual interest rate in percentage", example = "10.5")
    private BigDecimal annualInterestRate;

    @NotNull(message = "Tenure in months is required")
    @Min(value = 1, message = "Tenure must be at least 1 month")
    @Schema(description = "Loan tenure in months", example = "60")
    private Integer tenureMonths;

}

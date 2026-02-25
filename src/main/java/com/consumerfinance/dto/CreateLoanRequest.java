package com.consumerfinance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Request DTO for creating a new personal loan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new personal loan")
public class CreateLoanRequest {

    @NotBlank(message = "Customer ID is required")
    @Schema(description = "Unique customer identifier", example = "CUST123456")
    private String customerId;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000", inclusive = true, message = "Principal amount must be at least 1000")
    @DecimalMax(value = "10000000", inclusive = true, message = "Principal amount cannot exceed 10000000")
    @Schema(description = "Loan principal amount", example = "500000")
    private BigDecimal principalAmount;

    @NotNull(message = "Annual interest rate is required")
    @DecimalMin(value = "1", inclusive = true, message = "Interest rate must be at least 1%")
    @DecimalMax(value = "25", inclusive = true, message = "Interest rate cannot exceed 25%")
    @Schema(description = "Annual interest rate in percentage", example = "10.5")
    private BigDecimal annualInterestRate;

    @NotNull(message = "Loan tenure is required")
    @Min(value = 6, message = "Minimum loan tenure is 6 months")
    @Max(value = 360, message = "Maximum loan tenure is 360 months")
    @Schema(description = "Loan tenure in months", example = "60")
    private Integer loanTenureMonths;

}

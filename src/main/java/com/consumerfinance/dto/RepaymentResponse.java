package com.consumerfinance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for loan repayment details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Loan repayment transaction details")
public class RepaymentResponse {

    @Schema(description = "Repayment transaction ID", example = "1")
    private Long id;

    @Schema(description = "Associated loan ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String loanId;

    @Schema(description = "Installment number", example = "1")
    private Integer installmentNumber;

    @Schema(description = "Principal portion of the payment", example = "7808.33")
    private BigDecimal principalAmount;

    @Schema(description = "Interest portion of the payment", example = "1829.89")
    private BigDecimal interestAmount;

    @Schema(description = "Total amount due", example = "9638.22")
    private BigDecimal totalAmount;

    @Schema(description = "Amount actually paid", example = "9638.22")
    private BigDecimal paidAmount;

    @Schema(description = "Repayment status", example = "PAID")
    private String status;

    @Schema(description = "Due date for the installment")
    private LocalDateTime dueDate;

    @Schema(description = "Date when payment was made")
    private LocalDateTime paidDate;

    @Schema(description = "Payment method", example = "ONLINE")
    private String paymentMode;

    @Schema(description = "Payment reference or receipt number")
    private String transactionReference;

}

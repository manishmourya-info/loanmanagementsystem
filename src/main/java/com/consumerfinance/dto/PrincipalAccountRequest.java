package com.consumerfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for linking a principal account
 * T016: Principal Account request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalAccountRequest {

    @NotBlank(message = "Account number is required")
    @JsonProperty("accountNumber")
    private String accountNumber; // IBAN format

    @NotBlank(message = "Account holder name is required")
    @Size(min = 2, max = 100, message = "Account holder name must be between 2 and 100 characters")
    @JsonProperty("accountHolderName")
    private String accountHolderName;

    @NotBlank(message = "Bank code is required")
    @Size(min = 4, max = 10, message = "Bank code must be between 4 and 10 characters")
    @JsonProperty("bankCode")
    private String bankCode;
}

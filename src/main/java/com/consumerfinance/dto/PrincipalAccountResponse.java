package com.consumerfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for principal account
 * T016: Principal Account response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalAccountResponse {

    @JsonProperty("principalAccountId")
    private String principalAccountId; // UUID as string

    @JsonProperty("consumerId")
    private String consumerId; // UUID as string

    @JsonProperty("accountNumber")
    private String accountNumber; // Masked for security

    @JsonProperty("accountHolderName")
    private String accountHolderName;

    @JsonProperty("bankCode")
    private String bankCode;

    @JsonProperty("verificationStatus")
    private String verificationStatus; // PENDING, VERIFIED, FAILED, REJECTED

    @JsonProperty("linkedDate")
    private LocalDateTime linkedDate;

    @JsonProperty("verifiedDate")
    private LocalDateTime verifiedDate;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}

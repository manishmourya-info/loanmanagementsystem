package com.consumerfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for vendor linked account
 * T011: Vendor linked account response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorLinkedAccountResponse {

    @JsonProperty("vendorAccountId")
    private java.util.UUID vendorAccountId;

    @JsonProperty("vendorId")
    private java.util.UUID vendorId;

    @JsonProperty("vendorName")
    private String vendorName;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("accountDetails")
    private String accountDetails; // JSON string

    @JsonProperty("status")
    private String status;

    @JsonProperty("activationDate")
    private LocalDateTime activationDate;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}

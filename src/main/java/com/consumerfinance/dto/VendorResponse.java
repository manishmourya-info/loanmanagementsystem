package com.consumerfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for vendor
 * T011: Vendor response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponse {

    @JsonProperty("vendorId")
    private java.util.UUID vendorId;

    @JsonProperty("vendorName")
    private String vendorName;

    @JsonProperty("businessType")
    private String businessType;

    @JsonProperty("registrationNumber")
    private String registrationNumber;

    @JsonProperty("gstNumber")
    private String gstNumber;

    @JsonProperty("contactEmail")
    private String contactEmail;

    @JsonProperty("contactPhone")
    private String contactPhone;

    @JsonProperty("status")
    private String status;

    @JsonProperty("registrationDate")
    private LocalDateTime registrationDate;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}

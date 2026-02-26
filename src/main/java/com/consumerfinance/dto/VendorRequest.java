package com.consumerfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for vendor registration
 * T011: Vendor request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorRequest {

    @NotBlank(message = "Vendor name is required")
    @Size(min = 2, max = 100, message = "Vendor name must be between 2 and 100 characters")
    @JsonProperty("vendorName")
    private String vendorName;

    @NotBlank(message = "Business type is required")
    @JsonProperty("businessType")
    private String businessType;

    @NotBlank(message = "Registration number is required")
    @JsonProperty("registrationNumber")
    private String registrationNumber;

    @JsonProperty("gstNumber")
    private String gstNumber;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    @JsonProperty("contactEmail")
    private String contactEmail;

    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone must be in E.164 format")
    @JsonProperty("contactPhone")
    private String contactPhone;
}

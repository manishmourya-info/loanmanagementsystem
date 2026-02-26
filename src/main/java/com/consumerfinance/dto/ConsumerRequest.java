package com.consumerfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for creating/updating a consumer
 * T014/T015: Consumer request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone must be in E.164 format (+[country code][number])")
    @JsonProperty("phone")
    private String phone;

    @NotBlank(message = "Identity type is required")
    @JsonProperty("identityType")
    private String identityType; // AADHAAR, PAN, DRIVING_LICENSE, PASSPORT

    @NotBlank(message = "Identity number is required")
    @Size(min = 8, max = 20, message = "Identity number must be between 8 and 20 characters")
    @JsonProperty("identityNumber")
    private String identityNumber;
}

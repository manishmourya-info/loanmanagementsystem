package com.consumerfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for consumer
 * T014/T015: Consumer response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerResponse {

    @JsonProperty("consumerId")
    private String consumerId; // UUID as string

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("identityType")
    private String identityType;

    @JsonProperty("identityNumber")
    private String identityNumber;

    @JsonProperty("status")
    private String status; // ACTIVE, INACTIVE, SUSPENDED, CLOSED

    @JsonProperty("kycStatus")
    private String kycStatus; // PENDING, VERIFIED, REJECTED, EXPIRED

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}

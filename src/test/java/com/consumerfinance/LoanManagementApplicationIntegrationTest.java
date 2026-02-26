package com.consumerfinance;

import com.consumerfinance.dto.EMICalculationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Loan Management System.
 * Tests complete workflows including EMI calculation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Loan Management System Integration Tests")
@WithMockUser(username = "testuser", roles = {"USER"})
class LoanManagementApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private EMICalculationRequest emiRequest;

    @BeforeEach
    void setUp() {
        emiRequest = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .tenureMonths(60)
                .build();
    }

    @Test
    @DisplayName("Should calculate EMI via REST API")
    void testCalculateEMIViaAPI() throws Exception {
        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emiRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyEMI").exists())
                .andExpect(jsonPath("$.totalInterest").exists())
                .andExpect(jsonPath("$.tenureMonths").value(60));
    }

    @Test
    @DisplayName("Should return OK status for health check")
    void testHealthCheckEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Should calculate EMI with different interest rates")
    void testCalculateEMIWithDifferentRates() throws Exception {
        EMICalculationRequest request = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(300000))
                .annualInterestRate(BigDecimal.valueOf(9.0))
                .tenureMonths(36)
                .build();

        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyEMI").exists())
                .andExpect(jsonPath("$.principal").value(300000));
    }

    @Test
    @DisplayName("Should calculate EMI for longer tenure")
    void testCalculateEMIForLongerTenure() throws Exception {
        EMICalculationRequest request = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(1000000))
                .annualInterestRate(BigDecimal.valueOf(8.5))
                .tenureMonths(120)
                .build();

        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyEMI").exists())
                .andExpect(jsonPath("$.totalAmount").exists());
    }

    @Test
    @DisplayName("Should validate EMI with zero principal")
    void testCalculateEMIWithZeroPrincipal() throws Exception {
        EMICalculationRequest request = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.ZERO)
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .tenureMonths(60)
                .build();

        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate EMI with negative principal")
    void testCalculateEMIWithNegativePrincipal() throws Exception {
        EMICalculationRequest request = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(-100000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .tenureMonths(60)
                .build();

        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate EMI with zero tenure")
    void testCalculateEMIWithZeroTenure() throws Exception {
        EMICalculationRequest request = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .tenureMonths(0)
                .build();

        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate EMI with negative tenure")
    void testCalculateEMIWithNegativeTenure() throws Exception {
        EMICalculationRequest request = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .tenureMonths(-12)
                .build();

        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle EMI with no interest")
    void testCalculateEMIWithZeroInterestRate() throws Exception {
        EMICalculationRequest request = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.ZERO)
                .tenureMonths(60)
                .build();

        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyEMI").exists());
    }

    @Test
    @DisplayName("Should handle EMI with high interest rate")
    void testCalculateEMIWithHighInterestRate() throws Exception {
        EMICalculationRequest request = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(300000))
                .annualInterestRate(BigDecimal.valueOf(24.0))
                .tenureMonths(36)
                .build();

        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyEMI").exists())
                .andExpect(jsonPath("$.totalInterest").exists());
    }

    @Test
    @DisplayName("GET /api/v1/health/jvm should return JVM metrics")
    void testGetJvmMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/health/jvm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("GET /api/v1/health/database should return database status")
    void testGetDatabaseHealth() throws Exception {
        mockMvc.perform(get("/api/v1/health/database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("GET /api/v1/health/liveness should return UP status")
    void testLivenessProbe() throws Exception {
        mockMvc.perform(get("/api/v1/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/v1/health/readiness should return UP status")
    void testReadinessProbe() throws Exception {
        mockMvc.perform(get("/api/v1/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}

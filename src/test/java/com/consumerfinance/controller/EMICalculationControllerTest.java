package com.consumerfinance.controller;

import com.consumerfinance.config.SecurityConfig;
import com.consumerfinance.dto.EMICalculationRequest;
import com.consumerfinance.dto.EMICalculationResponse;
import com.consumerfinance.service.EMICalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for EMICalculationController.
 * Tests REST endpoint for EMI calculation.
 */
@WebMvcTest(EMICalculationController.class)
@Import(SecurityConfig.class)
@DisplayName("EMI Calculation Controller Tests")
@WithMockUser(username = "testuser", roles = {"USER"})
class EMICalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EMICalculationService emiCalculationService;

    @Autowired
    private ObjectMapper objectMapper;

    private EMICalculationRequest validRequest;
    private EMICalculationResponse mockResponse;

    @BeforeEach
    void setUp() {
        validRequest = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .tenureMonths(60)
                .build();

        mockResponse = EMICalculationResponse.builder()
                .monthlyEMI(BigDecimal.valueOf(10746.95))
                .totalAmount(BigDecimal.valueOf(644817.00))
                .totalInterest(BigDecimal.valueOf(144817.00))
                .principal(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .tenureMonths(60)
                .build();
    }

    @Test
    @DisplayName("Should calculate EMI and return 200 OK")
    void testCalculateEMI() throws Exception {
        // Arrange
        when(emiCalculationService.calculateEMI(any(EMICalculationRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyEMI").exists())
                .andExpect(jsonPath("$.totalInterest").exists())
                .andExpect(jsonPath("$.tenureMonths").value(60))
                .andExpect(jsonPath("$.principal").value(500000))
                .andExpect(jsonPath("$.annualInterestRate").value(10.5));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for invalid input")
    void testCalculateEMIWithInvalidInput() throws Exception {
        // Arrange
        EMICalculationRequest invalidRequest = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.ZERO)
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .tenureMonths(60)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for missing required fields")
    void testCalculateEMIWithMissingFields() throws Exception {
        // Arrange
        String jsonWithMissingFields = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithMissingFields))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should calculate EMI for different tenure")
    void testCalculateEMIWithDifferentTenure() throws Exception {
        // Arrange
        EMICalculationRequest request = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(300000))
                .annualInterestRate(BigDecimal.valueOf(9.0))
                .tenureMonths(36)
                .build();

        EMICalculationResponse response = EMICalculationResponse.builder()
                .monthlyEMI(BigDecimal.valueOf(9000.00))
                .totalAmount(BigDecimal.valueOf(324000.00))
                .totalInterest(BigDecimal.valueOf(24000.00))
                .principal(BigDecimal.valueOf(300000))
                .annualInterestRate(BigDecimal.valueOf(9.0))
                .tenureMonths(36)
                .build();

        when(emiCalculationService.calculateEMI(any(EMICalculationRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenureMonths").value(36))
                .andExpect(jsonPath("$.principal").value(300000))
                .andExpect(jsonPath("$.monthlyEMI").exists());
    }

    @Test
    @DisplayName("Should calculate EMI for high principal amount")
    void testCalculateEMIWithHighPrincipal() throws Exception {
        // Arrange
        EMICalculationRequest request = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(1000000))
                .annualInterestRate(BigDecimal.valueOf(8.5))
                .tenureMonths(120)
                .build();

        EMICalculationResponse response = EMICalculationResponse.builder()
                .monthlyEMI(BigDecimal.valueOf(12000.00))
                .totalAmount(BigDecimal.valueOf(1440000.00))
                .totalInterest(BigDecimal.valueOf(440000.00))
                .principal(BigDecimal.valueOf(1000000))
                .annualInterestRate(BigDecimal.valueOf(8.5))
                .tenureMonths(120)
                .build();

        when(emiCalculationService.calculateEMI(any(EMICalculationRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(1000000))
                .andExpect(jsonPath("$.tenureMonths").value(120));
    }

    @Test
    @DisplayName("Should calculate EMI for low interest rate")
    void testCalculateEMIWithLowInterestRate() throws Exception {
        // Arrange
        EMICalculationRequest request = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(400000))
                .annualInterestRate(BigDecimal.valueOf(4.5))
                .tenureMonths(60)
                .build();

        EMICalculationResponse response = EMICalculationResponse.builder()
                .monthlyEMI(BigDecimal.valueOf(7000.00))
                .totalAmount(BigDecimal.valueOf(420000.00))
                .totalInterest(BigDecimal.valueOf(20000.00))
                .principal(BigDecimal.valueOf(400000))
                .annualInterestRate(BigDecimal.valueOf(4.5))
                .tenureMonths(60)
                .build();

        when(emiCalculationService.calculateEMI(any(EMICalculationRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annualInterestRate").value(4.5));
    }

    @Test
    @DisplayName("Should return appropriate error for negative interest rate")
    void testCalculateEMIWithNegativeInterestRate() throws Exception {
        // Arrange
        EMICalculationRequest request = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(-5.0))
                .tenureMonths(60)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

}

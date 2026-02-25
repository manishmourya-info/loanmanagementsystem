package com.consumerfinance.controller;

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
import org.springframework.http.MediaType;
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
@DisplayName("EMI Calculation Controller Tests")
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
                .monthlyEMI(BigDecimal.valueOf(9638.22))
                .totalAmount(BigDecimal.valueOf(578293.20))
                .totalInterest(BigDecimal.valueOf(78293.20))
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
                .andExpect(jsonPath("$.monthlyEMI").value(9638.22))
                .andExpect(jsonPath("$.totalInterest").value(78293.20))
                .andExpect(jsonPath("$.tenureMonths").value(60));
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

}

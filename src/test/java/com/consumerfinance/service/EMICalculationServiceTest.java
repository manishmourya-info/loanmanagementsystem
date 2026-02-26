package com.consumerfinance.service;

import com.consumerfinance.dto.EMICalculationRequest;
import com.consumerfinance.dto.EMICalculationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EMICalculationService.
 * Tests EMI calculation accuracy using amortization formula.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EMI Calculation Service Tests")
class EMICalculationServiceTest {

    @InjectMocks
    private EMICalculationService emiCalculationService;

    private EMICalculationRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .tenureMonths(60)
                .build();
    }

    @Test
    @DisplayName("Should calculate EMI correctly for standard loan parameters")
    void testCalculateEMIWithStandardParameters() {
        // Act
        EMICalculationResponse response = emiCalculationService.calculateEMI(validRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.getMonthlyEMI().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.getTotalInterest().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.getTotalAmount().compareTo(response.getPrincipal()) > 0);
        
        // Verify approximate EMI (should be around 10746.95 for 500000 @ 10.5% for 60 months)
        BigDecimal expectedEMI = BigDecimal.valueOf(10746.95);
        assertTrue(response.getMonthlyEMI().compareTo(expectedEMI.subtract(BigDecimal.valueOf(100))) > 0);
        assertTrue(response.getMonthlyEMI().compareTo(expectedEMI.add(BigDecimal.valueOf(100))) < 0);
    }

    @Test
    @DisplayName("Should calculate total interest correctly")
    void testCalculateTotalInterest() {
        // Act
        EMICalculationResponse response = emiCalculationService.calculateEMI(validRequest);

        // Assert
        BigDecimal expectedTotalInterest = response.getTotalAmount().subtract(response.getPrincipal());
        assertEquals(expectedTotalInterest, response.getTotalInterest());
    }

    @Test
    @DisplayName("Should throw exception for zero principal")
    void testCalculateEMIWithZeroPrincipal() {
        // Arrange
        validRequest.setPrincipalAmount(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> emiCalculationService.calculateEMI(validRequest));
    }

    @Test
    @DisplayName("Should throw exception for negative principal")
    void testCalculateEMIWithNegativePrincipal() {
        // Arrange
        validRequest.setPrincipalAmount(BigDecimal.valueOf(-50000));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> emiCalculationService.calculateEMI(validRequest));
    }

    @Test
    @DisplayName("Should throw exception for zero tenure")
    void testCalculateEMIWithZeroTenure() {
        // Arrange
        validRequest.setTenureMonths(0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> emiCalculationService.calculateEMI(validRequest));
    }

    @Test
    @DisplayName("Should throw exception for negative tenure")
    void testCalculateEMIWithNegativeTenure() {
        // Arrange
        validRequest.setTenureMonths(-12);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> emiCalculationService.calculateEMI(validRequest));
    }

    @Test
    @DisplayName("Should calculate EMI with zero interest rate")
    void testCalculateEMIWithZeroInterest() {
        // Arrange
        validRequest.setAnnualInterestRate(BigDecimal.ZERO);

        // Act
        EMICalculationResponse response = emiCalculationService.calculateEMI(validRequest);

        // Assert
        BigDecimal expectedEMI = BigDecimal.valueOf(500000).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        assertEquals(expectedEMI, response.getMonthlyEMI());
        // Total interest should be zero (or very close to zero accounting for rounding)
        assertTrue(response.getTotalInterest().compareTo(BigDecimal.valueOf(0.5)) < 0, 
                   "Total interest should be nearly zero, got: " + response.getTotalInterest());
    }

    @Test
    @DisplayName("Should calculate EMI for short tenure loan")
    void testCalculateEMIForShortTenure() {
        // Arrange
        validRequest.setTenureMonths(12);

        // Act
        EMICalculationResponse response = emiCalculationService.calculateEMI(validRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.getMonthlyEMI().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.getTotalAmount().compareTo(BigDecimal.valueOf(500000)) > 0);
    }

    @Test
    @DisplayName("Should calculate EMI for long tenure loan")
    void testCalculateEMIForLongTenure() {
        // Arrange
        validRequest.setTenureMonths(360);

        // Act
        EMICalculationResponse response = emiCalculationService.calculateEMI(validRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.getMonthlyEMI().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.getMonthlyEMI().compareTo(BigDecimal.valueOf(500000).divide(BigDecimal.valueOf(360), 2, java.math.RoundingMode.HALF_UP)) > 0);
    }

    @Test
    @DisplayName("Should calculate EMI for high interest rate")
    void testCalculateEMIForHighInterestRate() {
        // Arrange
        validRequest.setAnnualInterestRate(BigDecimal.valueOf(20.0));

        // Act
        EMICalculationResponse response = emiCalculationService.calculateEMI(validRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.getTotalInterest().compareTo(BigDecimal.ZERO) > 0);
    }

}

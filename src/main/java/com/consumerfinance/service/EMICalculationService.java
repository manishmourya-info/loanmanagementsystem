package com.consumerfinance.service;

import com.consumerfinance.dto.EMICalculationRequest;
import com.consumerfinance.dto.EMICalculationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for EMI (Equated Monthly Installment) calculations.
 * Uses standard amortization formula for loan calculations.
 */
@Slf4j
@Service
public class EMICalculationService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Calculate EMI using the formula: EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
     * Where:
     *   P = Principal amount
     *   r = Monthly interest rate (annual rate / 12 / 100)
     *   n = Number of months
     *
     * @param request contains principal, annual interest rate, and tenure
     * @return EMI calculation result with breakdown
     */
    public EMICalculationResponse calculateEMI(EMICalculationRequest request) {
        log.info("Calculating EMI for Principal: {}, Rate: {}, Tenure: {} months",
                request.getPrincipalAmount(), request.getAnnualInterestRate(), request.getTenureMonths());

        BigDecimal principal = request.getPrincipalAmount();
        BigDecimal monthlyRate = getMonthlyRate(request.getAnnualInterestRate());
        int tenureMonths = request.getTenureMonths();

        // Validate inputs
        validateEMIInput(principal, monthlyRate, tenureMonths);

        // Calculate EMI using amortization formula
        BigDecimal emi = calculateMonthlyEMI(principal, monthlyRate, tenureMonths);
        BigDecimal totalAmount = emi.multiply(BigDecimal.valueOf(tenureMonths)).setScale(SCALE, ROUNDING_MODE);
        BigDecimal totalInterest = totalAmount.subtract(principal).setScale(SCALE, ROUNDING_MODE);

        log.info("EMI Calculation Result - Monthly EMI: {}, Total Interest: {}", emi, totalInterest);

        return EMICalculationResponse.builder()
                .monthlyEMI(emi)
                .totalAmount(totalAmount)
                .totalInterest(totalInterest)
                .principal(principal)
                .annualInterestRate(request.getAnnualInterestRate())
                .tenureMonths(tenureMonths)
                .build();
    }

    /**
     * Calculate monthly EMI using the standard amortization formula.
     *
     * @param principal the principal amount
     * @param monthlyRate the monthly interest rate as a decimal (e.g., 0.01 for 1%)
     * @param tenureMonths the number of months
     * @return the monthly EMI amount
     */
    private BigDecimal calculateMonthlyEMI(BigDecimal principal, BigDecimal monthlyRate, int tenureMonths) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            // If no interest, EMI is simply principal divided by tenure
            return principal.divide(BigDecimal.valueOf(tenureMonths), SCALE, ROUNDING_MODE);
        }

        // (1 + r)
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);

        // (1 + r)^n
        BigDecimal onePlusRPowerN = onePlusR.pow(tenureMonths);

        // (1 + r)^n - 1
        BigDecimal numeratorPart = onePlusRPowerN.subtract(BigDecimal.ONE);

        // r * (1 + r)^n
        BigDecimal numerator = monthlyRate.multiply(onePlusRPowerN);

        // r * (1 + r)^n / ((1 + r)^n - 1)
        BigDecimal factor = numerator.divide(numeratorPart, 10, ROUNDING_MODE);

        // EMI = Principal * factor
        return principal.multiply(factor).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate the monthly interest rate from annual rate.
     *
     * @param annualRate the annual interest rate as a percentage (e.g., 10.5)
     * @return the monthly rate as a decimal (e.g., 0.00875 for 10.5% annual)
     */
    private BigDecimal getMonthlyRate(BigDecimal annualRate) {
        return annualRate.divide(BigDecimal.valueOf(12).multiply(BigDecimal.valueOf(100)), 10, ROUNDING_MODE);
    }

    /**
     * Validate EMI calculation inputs.
     *
     * @param principal the principal amount
     * @param monthlyRate the monthly interest rate
     * @param tenureMonths the number of months
     * @throws IllegalArgumentException if inputs are invalid
     */
    private void validateEMIInput(BigDecimal principal, BigDecimal monthlyRate, int tenureMonths) {
        if (principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal amount must be greater than zero");
        }
        if (tenureMonths <= 0) {
            throw new IllegalArgumentException("Tenure must be greater than zero");
        }
        if (monthlyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
    }

}

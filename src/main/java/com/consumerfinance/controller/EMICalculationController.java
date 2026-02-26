package com.consumerfinance.controller;

import com.consumerfinance.dto.EMICalculationRequest;
import com.consumerfinance.dto.EMICalculationResponse;
import com.consumerfinance.service.EMICalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for EMI calculation operations.
 * Provides endpoints for calculating Equated Monthly Installment amounts with amortization details.
 * Phase 5: T029-T034
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/emi")
@Tag(name = "EMI Calculation", description = "API for calculating EMI (Equated Monthly Installment) with amortization schedule")
public class EMICalculationController {

    private final EMICalculationService emiCalculationService;

    public EMICalculationController(EMICalculationService emiCalculationService) {
        this.emiCalculationService = emiCalculationService;
    }

    /**
     * Calculate EMI for given loan parameters with detailed amortization schedule.
     * T030: Implement POST /emi/calculate endpoint
     *
     * @param request the EMI calculation request containing principal, annual interest rate, and tenure
     * @return the EMI calculation result with detailed breakdown
     */
    @PostMapping("/calculate")
    @Operation(summary = "Calculate EMI with Amortization",
               description = "Calculates the monthly EMI and provides total interest for given loan parameters using standard amortization formula: EMI = P × r × (1+r)^n / ((1+r)^n - 1)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMI calculated successfully",
                     content = @Content(schema = @Schema(implementation = EMICalculationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters (invalid amounts or tenure)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<EMICalculationResponse> calculateEMI(@Valid @RequestBody EMICalculationRequest request) {
        log.info("REST: POST /api/v1/emi/calculate - Calculating EMI for Principal: {}, Rate: {}, Tenure: {} months",
                request.getPrincipalAmount(), request.getAnnualInterestRate(), request.getTenureMonths());
        EMICalculationResponse response = emiCalculationService.calculateEMI(request);
        log.info("EMI Calculation completed - Monthly EMI: {}, Total Interest: {}", 
                response.getMonthlyEMI(), response.getTotalInterest());
        return ResponseEntity.ok(response);
    }

}


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
 * Provides endpoints for calculating Equated Monthly Installment amounts.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/emi")
@Tag(name = "EMI Calculation", description = "API for calculating EMI (Equated Monthly Installment)")
public class EMICalculationController {

    private final EMICalculationService emiCalculationService;

    public EMICalculationController(EMICalculationService emiCalculationService) {
        this.emiCalculationService = emiCalculationService;
    }

    /**
     * Calculate EMI for given loan parameters.
     *
     * @param request the EMI calculation request
     * @return the EMI calculation result
     */
    @PostMapping("/calculate")
    @Operation(summary = "Calculate EMI",
               description = "Calculates the monthly EMI and total interest for given loan parameters using standard amortization formula")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMI calculated successfully",
                     content = @Content(schema = @Schema(implementation = EMICalculationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<EMICalculationResponse> calculateEMI(@Valid @RequestBody EMICalculationRequest request) {
        log.info("REST: POST /api/v1/emi/calculate - Calculating EMI for Principal: {}, Rate: {}, Tenure: {}",
                request.getPrincipalAmount(), request.getAnnualInterestRate(), request.getTenureMonths());
        EMICalculationResponse response = emiCalculationService.calculateEMI(request);
        return ResponseEntity.ok(response);
    }

}

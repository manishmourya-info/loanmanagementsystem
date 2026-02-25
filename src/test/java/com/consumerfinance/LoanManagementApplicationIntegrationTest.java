package com.consumerfinance;

import com.consumerfinance.dto.CreateLoanRequest;
import com.consumerfinance.dto.EMICalculationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Loan Management System.
 * Tests complete workflows including EMI calculation and loan creation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Loan Management System Integration Tests")
class LoanManagementApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private EMICalculationRequest emiRequest;
    private CreateLoanRequest loanRequest;

    @BeforeEach
    void setUp() {
        emiRequest = EMICalculationRequest.builder()
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .tenureMonths(60)
                .build();

        loanRequest = CreateLoanRequest.builder()
                .customerId("CUST123456")
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .loanTenureMonths(60)
                .build();
    }

    @Test
    @DisplayName("Should calculate EMI via REST API")
    void testCalculateEMIViaAPI() throws Exception {
        mockMvc.perform(post("/api/v1/emi/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emiRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyEMI").exists())
                .andExpect(jsonPath("$.totalInterest").exists())
                .andExpect(jsonPath("$.tenureMonths").value(60));
    }

    @Test
    @DisplayName("Should create a personal loan and generate repayment schedule")
    void testCreateLoanWithRepaymentSchedule() throws Exception {
        // Create loan
        String response = mockMvc.perform(post("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId").value("CUST123456"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.monthlyEMI").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract loan ID from response
        Long loanId = objectMapper.readTree(response).get("id").asLong();

        // Retrieve loan details
        mockMvc.perform(get("/api/v1/loans/" + loanId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanId))
                .andExpect(jsonPath("$.principalAmount").value(500000));

        // Retrieve repayment schedule
        mockMvc.perform(get("/api/v1/repayments/" + loanId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].installmentNumber").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("Should retrieve all loans for a customer")
    void testGetLoansByCustomerId() throws Exception {
        // Create a loan first
        mockMvc.perform(post("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isCreated());

        // Retrieve loans for customer
        mockMvc.perform(get("/api/v1/loans/customer/CUST123456"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].customerId").value("CUST123456"));
    }

    @Test
    @DisplayName("Should process loan repayment")
    void testProcessLoanRepayment() throws Exception {
        // Create a loan first
        String response = mockMvc.perform(post("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long loanId = objectMapper.readTree(response).get("id").asLong();

        // Process payment for first installment
        mockMvc.perform(post("/api/v1/repayments/{}/installment/{}/pay", loanId, 1)
                .param("amountPaid", "9638.22"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.installmentNumber").value(1));
    }

    @Test
    @DisplayName("Should retrieve pending repayments for a loan")
    void testGetPendingRepayments() throws Exception {
        // Create a loan first
        String response = mockMvc.perform(post("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long loanId = objectMapper.readTree(response).get("id").asLong();

        // Get pending repayments
        mockMvc.perform(get("/api/v1/repayments/{}/pending", loanId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return 404 for non-existent loan")
    void testGetNonExistentLoan() throws Exception {
        mockMvc.perform(get("/api/v1/loans/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Should validate loan creation request parameters")
    void testCreateLoanWithInvalidParameters() throws Exception {
        // Create request with invalid principal
        CreateLoanRequest invalidRequest = CreateLoanRequest.builder()
                .customerId("CUST123456")
                .principalAmount(BigDecimal.valueOf(500))  // Below minimum
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .loanTenureMonths(60)
                .build();

        mockMvc.perform(post("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

}

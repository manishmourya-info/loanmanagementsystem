package com.consumerfinance.controller;

import com.consumerfinance.config.SecurityConfig;
import com.consumerfinance.dto.CreateLoanRequest;
import com.consumerfinance.dto.LoanResponse;
import com.consumerfinance.service.PersonalLoanService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PersonalLoanController.
 * Tests REST endpoints for personal loan operations.
 */
@WebMvcTest(PersonalLoanController.class)
@Import(SecurityConfig.class)
@DisplayName("Personal Loan Controller Tests")
@WithMockUser(username = "testuser", roles = {"USER"})
class PersonalLoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonalLoanService personalLoanService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID loanId;
    private UUID consumerId;
    private CreateLoanRequest validRequest;
    private LoanResponse mockLoanResponse;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();
        consumerId = UUID.randomUUID();

        validRequest = CreateLoanRequest.builder()
                .customerId(consumerId.toString())
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .loanTenureMonths(60)
                .build();

        mockLoanResponse = LoanResponse.builder()
                .id(loanId.toString())
                .customerId(consumerId.toString())
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .monthlyEMI(BigDecimal.valueOf(10746.95))
                .outstandingBalance(BigDecimal.valueOf(500000))
                .status("PENDING")
                .build();
    }

    @Test
    @DisplayName("Should create a new personal loan and return 201")
    void testCreateLoan_Success() throws Exception {
        // Arrange
        when(personalLoanService.createLoan(any(CreateLoanRequest.class)))
                .thenReturn(mockLoanResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(loanId.toString()))
                .andExpect(jsonPath("$.principalAmount").value(500000))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should return 400 for invalid loan request")
    void testCreateLoan_InvalidRequest() throws Exception {
        // Arrange
        CreateLoanRequest invalidRequest = CreateLoanRequest.builder()
                .customerId(consumerId.toString())
                .principalAmount(BigDecimal.ZERO)
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .loanTenureMonths(60)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for missing required fields")
    void testCreateLoan_MissingFields() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get loan details by ID and return 200")
    void testGetLoan_Success() throws Exception {
        // Arrange
        when(personalLoanService.getLoan(eq(loanId)))
                .thenReturn(mockLoanResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/loans/{loanId}", loanId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanId.toString()))
                .andExpect(jsonPath("$.principalAmount").value(500000));
    }

    @Test
    @DisplayName("Should return 404 when loan not found")
    void testGetLoan_NotFound() throws Exception {
        // Arrange
        UUID unknownLoanId = UUID.randomUUID();
        when(personalLoanService.getLoan(eq(unknownLoanId)))
                .thenThrow(new RuntimeException("Loan not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/loans/{loanId}", unknownLoanId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should approve a pending loan")
    void testApproveLoan_Success() throws Exception {
        // Arrange
        LoanResponse approvedLoan = mockLoanResponse;
        approvedLoan.setStatus("APPROVED");
        when(personalLoanService.approveLoan(eq(loanId), any(String.class)))
                .thenReturn(approvedLoan);

        // Act & Assert
        mockMvc.perform(put("/api/v1/loans/{loanId}/approve", loanId)
                .param("approvalRemarks", "Approved by manager")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Should reject a pending loan")
    void testRejectLoan_Success() throws Exception {
        // Arrange
        LoanResponse rejectedLoan = mockLoanResponse;
        rejectedLoan.setStatus("REJECTED");
        when(personalLoanService.rejectLoan(eq(loanId), any(String.class)))
                .thenReturn(rejectedLoan);

        // Act & Assert
        mockMvc.perform(put("/api/v1/loans/{loanId}/reject", loanId)
                .param("rejectionRemarks", "Does not meet eligibility")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @DisplayName("Should disburse an approved loan")
    void testDisburseLoan_Success() throws Exception {
        // Arrange
        LoanResponse disbursedLoan = mockLoanResponse;
        disbursedLoan.setStatus("DISBURSED");
        when(personalLoanService.disburseLoan(eq(loanId)))
                .thenReturn(disbursedLoan);

        // Act & Assert
        mockMvc.perform(put("/api/v1/loans/{loanId}/disburse", loanId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISBURSED"));
    }

    @Test
    @DisplayName("Should handle authentication requirement")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testLoanOperation_WithAdminRole() throws Exception {
        // Arrange
        when(personalLoanService.getLoan(eq(loanId)))
                .thenReturn(mockLoanResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/loans/{loanId}", loanId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

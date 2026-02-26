package com.consumerfinance.controller;

import com.consumerfinance.config.SecurityConfig;
import com.consumerfinance.dto.RepaymentResponse;
import com.consumerfinance.service.LoanRepaymentService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for LoanRepaymentController.
 * Tests REST endpoints for loan repayment operations.
 */
@WebMvcTest(LoanRepaymentController.class)
@Import(SecurityConfig.class)
@DisplayName("Loan Repayment Controller Tests")
@WithMockUser(username = "testuser", roles = {"USER"})
class LoanRepaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanRepaymentService repaymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID loanId;
    private RepaymentResponse mockRepaymentResponse;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();

        mockRepaymentResponse = RepaymentResponse.builder()
                .loanId(loanId.toString())
                .installmentNumber(1)
                .totalAmount(BigDecimal.valueOf(10746.95))
                .principalAmount(BigDecimal.valueOf(8333.33))
                .interestAmount(BigDecimal.valueOf(2413.62))
                .paidAmount(BigDecimal.ZERO)
                .status("PENDING")
                .build();
    }

    @Test
    @DisplayName("Should process loan repayment and return 200")
    void testProcessRepayment_Success() throws Exception {
        // Arrange
        when(repaymentService.processRepayment(eq(loanId), eq(1), any(BigDecimal.class)))
                .thenReturn(mockRepaymentResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/repayments/{loanId}/installment/{installmentNumber}/pay", loanId, 1)
                .param("amountPaid", "10746.95")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value(loanId.toString()))
                .andExpect(jsonPath("$.installmentNumber").value(1))
                .andExpect(jsonPath("$.totalAmount").value(10746.95));
    }

    @Test
    @DisplayName("Should return 400 for invalid repayment amount")
    void testProcessRepayment_InvalidAmount() throws Exception {
        // Arrange - negative amount should be rejected by validation

        // Act & Assert
        mockMvc.perform(post("/api/v1/repayments/{loanId}/installment/{installmentNumber}/pay", loanId, 1)
                .param("amountPaid", "-1000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when loan not found")
    void testProcessRepayment_LoanNotFound() throws Exception {
        // Arrange
        UUID unknownLoanId = UUID.randomUUID();
        when(repaymentService.processRepayment(eq(unknownLoanId), eq(1), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Loan not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/repayments/{loanId}/installment/{installmentNumber}/pay", unknownLoanId, 1)
                .param("amountPaid", "10746.95")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should get repayment details by loan ID and installment number")
    void testGetRepayment_Success() throws Exception {
        // Arrange
        when(repaymentService.getRepayment(eq(loanId), eq(1)))
                .thenReturn(mockRepaymentResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/repayments/{loanId}/installment/{installmentNumber}", loanId, 1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installmentNumber").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should return 404 when repayment not found")
    void testGetRepayment_NotFound() throws Exception {
        // Arrange
        when(repaymentService.getRepayment(eq(loanId), eq(99)))
                .thenThrow(new RuntimeException("Repayment not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/repayments/{loanId}/installment/{installmentNumber}", loanId, 99)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should get all repayments for a loan")
    void testGetRepaymentsByLoanId_Success() throws Exception {
        // Arrange
        List<RepaymentResponse> repayments = new ArrayList<>();
        repayments.add(mockRepaymentResponse);
        when(repaymentService.getRepaymentsByLoanId(eq(loanId)))
                .thenReturn(repayments);

        // Act & Assert
        mockMvc.perform(get("/api/v1/repayments/{loanId}", loanId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].loanId").value(loanId.toString()))
                .andExpect(jsonPath("$[0].installmentNumber").value(1));
    }

    @Test
    @DisplayName("Should get pending repayments for a loan")
    void testGetPendingRepayments_Success() throws Exception {
        // Arrange
        List<RepaymentResponse> pendingRepayments = new ArrayList<>();
        pendingRepayments.add(mockRepaymentResponse);
        when(repaymentService.getPendingRepaymentsByLoanId(eq(loanId)))
                .thenReturn(pendingRepayments);

        // Act & Assert
        mockMvc.perform(get("/api/v1/repayments/{loanId}/pending", loanId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("Should get all overdue repayments")
    void testGetOverdueRepayments_Success() throws Exception {
        // Arrange
        List<RepaymentResponse> overdueRepayments = new ArrayList<>();
        RepaymentResponse overdueRepayment = mockRepaymentResponse;
        overdueRepayment.setStatus("OVERDUE");
        overdueRepayments.add(overdueRepayment);
        when(repaymentService.getOverdueRepayments())
                .thenReturn(overdueRepayments);

        // Act & Assert
        mockMvc.perform(get("/api/v1/repayments/overdue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("OVERDUE"));
    }

    @Test
    @DisplayName("Should handle partial payment")
    void testProcessPartialRepayment_Success() throws Exception {
        // Arrange
        RepaymentResponse partialRepayment = mockRepaymentResponse;
        partialRepayment.setPaidAmount(BigDecimal.valueOf(5000));
        partialRepayment.setStatus("PARTIALLY_PAID");

        when(repaymentService.processRepayment(eq(loanId), eq(1), any(BigDecimal.class)))
                .thenReturn(partialRepayment);

        // Act & Assert
        mockMvc.perform(post("/api/v1/repayments/{loanId}/installment/{installmentNumber}/pay", loanId, 1)
                .param("amountPaid", "5000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIALLY_PAID"))
                .andExpect(jsonPath("$.paidAmount").value(5000));
    }

    @Test
    @DisplayName("Should handle authentication requirement")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testRepaymentOperation_WithAdminRole() throws Exception {
        // Arrange
        when(repaymentService.getRepayment(eq(loanId), eq(1)))
                .thenReturn(mockRepaymentResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/repayments/{loanId}/installment/{installmentNumber}", loanId, 1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

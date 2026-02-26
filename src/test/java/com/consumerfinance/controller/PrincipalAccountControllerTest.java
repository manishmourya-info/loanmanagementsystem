package com.consumerfinance.controller;

import com.consumerfinance.config.SecurityConfig;
import com.consumerfinance.dto.PrincipalAccountRequest;
import com.consumerfinance.dto.PrincipalAccountResponse;
import com.consumerfinance.service.PrincipalAccountService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PrincipalAccountController.
 * Tests REST endpoints for principal account management.
 */
@WebMvcTest(PrincipalAccountController.class)
@Import(SecurityConfig.class)
@DisplayName("Principal Account Controller Tests")
@WithMockUser(username = "testuser", roles = {"USER"})
class PrincipalAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrincipalAccountService principalAccountService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID consumerId;
    private UUID accountId;
    private PrincipalAccountRequest validRequest;
    private PrincipalAccountResponse mockResponse;

    @BeforeEach
    void setUp() {
        consumerId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        validRequest = PrincipalAccountRequest.builder()
                .accountNumber("DE75512108001234567890")
                .accountHolderName("John Doe")
                .bankCode("BANK123")
                .build();

        mockResponse = PrincipalAccountResponse.builder()
                .principalAccountId(accountId.toString())
                .consumerId(consumerId.toString())
                .accountNumber("DE75512108001234567890")
                .accountHolderName("John Doe")
                .bankCode("BANK123")
                .verificationStatus("PENDING")
                .build();
    }

    @Test
    @DisplayName("Should link principal account and return 201")
    void testLinkPrincipalAccount_Success() throws Exception {
        // Arrange
        when(principalAccountService.linkPrincipalAccount(eq(consumerId), any(PrincipalAccountRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers/{consumerId}/principal-account", consumerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.principalAccountId").value(accountId.toString()))
                .andExpect(jsonPath("$.accountHolderName").value("John Doe"))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));
    }

    @Test
    @DisplayName("Should return 400 for invalid account request")
    void testLinkPrincipalAccount_InvalidRequest() throws Exception {
        // Arrange
        PrincipalAccountRequest invalidRequest = PrincipalAccountRequest.builder()
                .accountNumber("INVALID")
                .accountHolderName("")
                .bankCode("BANK123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers/{consumerId}/principal-account", consumerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for missing required fields")
    void testLinkPrincipalAccount_MissingFields() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers/{consumerId}/principal-account", consumerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when consumer not found")
    void testLinkPrincipalAccount_ConsumerNotFound() throws Exception {
        // Arrange
        UUID unknownConsumerId = UUID.randomUUID();
        when(principalAccountService.linkPrincipalAccount(eq(unknownConsumerId), any(PrincipalAccountRequest.class)))
                .thenThrow(new RuntimeException("Consumer not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers/{consumerId}/principal-account", unknownConsumerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should get principal account details and return 200")
    void testGetPrincipalAccount_Success() throws Exception {
        // Arrange
        when(principalAccountService.getPrincipalAccount(eq(consumerId)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/consumers/{consumerId}/principal-account", consumerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principalAccountId").value(accountId.toString()))
                .andExpect(jsonPath("$.accountHolderName").value("John Doe"));
    }

    @Test
    @DisplayName("Should return 404 when account not found")
    void testGetPrincipalAccount_NotFound() throws Exception {
        // Arrange
        UUID unknownConsumerId = UUID.randomUUID();
        when(principalAccountService.getPrincipalAccount(eq(unknownConsumerId)))
                .thenThrow(new RuntimeException("Account not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/consumers/{consumerId}/principal-account", unknownConsumerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should update principal account and return 200")
    void testUpdatePrincipalAccount_Success() throws Exception {
        // Arrange
        PrincipalAccountRequest updateRequest = PrincipalAccountRequest.builder()
                .accountNumber("DE75512108009876543210")
                .accountHolderName("Jane Doe")
                .bankCode("BANK456")
                .build();

        PrincipalAccountResponse updatedResponse = PrincipalAccountResponse.builder()
                .principalAccountId(accountId.toString())
                .consumerId(consumerId.toString())
                .accountNumber("DE75512108009876543210")
                .accountHolderName("Jane Doe")
                .bankCode("BANK456")
                .verificationStatus("PENDING")
                .build();

        when(principalAccountService.updatePrincipalAccount(eq(consumerId), any(PrincipalAccountRequest.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/consumers/{consumerId}/principal-account", consumerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountHolderName").value("Jane Doe"));
    }

    @Test
    @DisplayName("Should verify principal account with admin role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testVerifyPrincipalAccount_Success() throws Exception {
        // Arrange
        PrincipalAccountResponse verifiedResponse = mockResponse;
        verifiedResponse.setVerificationStatus("VERIFIED");

        when(principalAccountService.verifyAccount(eq(accountId)))
                .thenReturn(verifiedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/consumers/{consumerId}/principal-account/verify/{accountId}", consumerId, accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
    }

    @Test
    @DisplayName("Should reject verification with admin role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testRejectVerification_Success() throws Exception {
        // Arrange
        PrincipalAccountResponse rejectedResponse = mockResponse;
        rejectedResponse.setVerificationStatus("REJECTED");

        when(principalAccountService.rejectAccount(eq(accountId), any(String.class)))
                .thenReturn(rejectedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/consumers/{consumerId}/principal-account/reject/{accountId}", consumerId, accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("REJECTED"));
    }

    @Test
    @DisplayName("Should handle authentication requirement")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testPrincipalAccountOperation_WithAdminRole() throws Exception {
        // Arrange
        when(principalAccountService.getPrincipalAccount(eq(consumerId)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/consumers/{consumerId}/principal-account", consumerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

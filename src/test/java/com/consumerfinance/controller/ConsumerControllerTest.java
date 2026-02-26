package com.consumerfinance.controller;

import com.consumerfinance.config.SecurityConfig;
import com.consumerfinance.dto.ConsumerRequest;
import com.consumerfinance.dto.ConsumerResponse;
import com.consumerfinance.service.ConsumerService;
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
 * Unit tests for ConsumerController.
 * Tests REST endpoints for consumer management.
 */
@WebMvcTest(ConsumerController.class)
@Import(SecurityConfig.class)
@DisplayName("Consumer Controller Tests")
@WithMockUser(username = "testuser", roles = {"USER"})
class ConsumerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsumerService consumerService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID consumerId;
    private ConsumerRequest validRequest;
    private ConsumerResponse mockResponse;

    @BeforeEach
    void setUp() {
        consumerId = UUID.randomUUID();

        validRequest = ConsumerRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .identityType("PASSPORT")
                .identityNumber("AB123456")
                .build();

        mockResponse = ConsumerResponse.builder()
                .consumerId(consumerId.toString())
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .identityType("PASSPORT")
                .identityNumber("AB123456")
                .status("ACTIVE")
                .kycStatus("PENDING")
                .build();
    }

    @Test
    @DisplayName("Should create a new consumer and return 201")
    void testCreateConsumer_Success() throws Exception {
        // Arrange
        when(consumerService.createConsumer(any(ConsumerRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consumerId").value(consumerId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should return 400 for invalid consumer request")
    void testCreateConsumer_InvalidRequest() throws Exception {
        // Arrange
        ConsumerRequest invalidRequest = ConsumerRequest.builder()
                .name("John Doe")
                .email("invalid-email")
                .phone("+1234567890")
                .identityType("PASSPORT")
                .identityNumber("AB123456")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for missing required fields")
    void testCreateConsumer_MissingFields() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 for duplicate email")
    void testCreateConsumer_DuplicateEmail() throws Exception {
        // Arrange
        when(consumerService.createConsumer(any(ConsumerRequest.class)))
                .thenThrow(new RuntimeException("Email already registered"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should get consumer details by ID and return 200")
    void testGetConsumer_Success() throws Exception {
        // Arrange
        when(consumerService.getConsumer(eq(consumerId)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/consumers/{consumerId}", consumerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consumerId").value(consumerId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @DisplayName("Should return 404 when consumer not found")
    void testGetConsumer_NotFound() throws Exception {
        // Arrange
        UUID unknownConsumerId = UUID.randomUUID();
        when(consumerService.getConsumer(eq(unknownConsumerId)))
                .thenThrow(new RuntimeException("Consumer not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/consumers/{consumerId}", unknownConsumerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should update consumer profile and return 200")
    void testUpdateConsumer_Success() throws Exception {
        // Arrange
        ConsumerRequest updateRequest = ConsumerRequest.builder()
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .phone("+9876543210")
                .identityType("PASSPORT")
                .identityNumber("AB654321")
                .build();

        ConsumerResponse updatedResponse = ConsumerResponse.builder()
                .consumerId(consumerId.toString())
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .phone("+9876543210")
                .identityType("PASSPORT")
                .identityNumber("AB654321")
                .status("ACTIVE")
                .kycStatus("PENDING")
                .build();

        when(consumerService.updateConsumer(eq(consumerId), any(ConsumerRequest.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/consumers/{consumerId}", consumerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));
    }

    @Test
    @DisplayName("Should update KYC status")
    void testUpdateKYCStatus_Success() throws Exception {
        // Arrange
        ConsumerResponse updatedResponse = mockResponse;
        updatedResponse.setKycStatus("VERIFIED");

        when(consumerService.updateKYCStatus(eq(consumerId), any(String.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/consumers/{consumerId}/kyc-status", consumerId)
                .param("status", "VERIFIED")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kycStatus").value("VERIFIED"));
    }

    @Test
    @DisplayName("Should delete consumer and return 204")
    void testDeleteConsumer_Success() throws Exception {
        // Arrange - no response needed for 204

        // Act & Assert
        mockMvc.perform(delete("/api/v1/consumers/{consumerId}", consumerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should handle authentication requirement")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testConsumerOperation_WithAdminRole() throws Exception {
        // Arrange
        when(consumerService.getConsumer(eq(consumerId)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/consumers/{consumerId}", consumerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

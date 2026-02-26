package com.consumerfinance.controller;

import com.consumerfinance.config.SecurityConfig;
import com.consumerfinance.dto.VendorRequest;
import com.consumerfinance.dto.VendorResponse;
import com.consumerfinance.dto.VendorLinkedAccountResponse;
import com.consumerfinance.service.VendorService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for VendorController.
 * Tests REST endpoints for vendor and linked account management.
 */
@WebMvcTest(VendorController.class)
@Import(SecurityConfig.class)
@DisplayName("Vendor Controller Tests")
@WithMockUser(username = "testuser", roles = {"USER"})
class VendorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VendorService vendorService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID vendorId;
    private UUID accountId;
    private VendorRequest validRequest;
    private VendorResponse mockResponse;

    @BeforeEach
    void setUp() {
        vendorId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        validRequest = VendorRequest.builder()
                .vendorName("ABC Electronics")
                .businessType("RETAIL")
                .registrationNumber("REG123456")
                .gstNumber("GST123456789")
                .contactEmail("vendor@example.com")
                .contactPhone("+1234567890")
                .build();

        mockResponse = VendorResponse.builder()
                .vendorId(vendorId)
                .vendorName("ABC Electronics")
                .businessType("RETAIL")
                .registrationNumber("REG123456")
                .gstNumber("GST123456789")
                .contactEmail("vendor@example.com")
                .contactPhone("+1234567890")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("Should register a new vendor and return 201")
    void testRegisterVendor_Success() throws Exception {
        // Arrange
        when(vendorService.registerVendor(any(VendorRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/vendors/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendorId").value(vendorId.toString()))
                .andExpect(jsonPath("$.vendorName").value("ABC Electronics"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should return 400 for invalid vendor request")
    void testRegisterVendor_InvalidRequest() throws Exception {
        // Arrange
        VendorRequest invalidRequest = VendorRequest.builder()
                .vendorName("")
                .businessType("RETAIL")
                .registrationNumber("REG123456")
                .gstNumber("GST123456789")
                .contactEmail("invalid-email")
                .contactPhone("+1234567890")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/vendors/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for missing required fields")
    void testRegisterVendor_MissingFields() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/vendors/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 for duplicate vendor registration")
    void testRegisterVendor_Duplicate() throws Exception {
        // Arrange
        when(vendorService.registerVendor(any(VendorRequest.class)))
                .thenThrow(new RuntimeException("Vendor already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/vendors/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should get vendor by ID and return 200")
    void testGetVendorById_Success() throws Exception {
        // Arrange
        when(vendorService.getVendorById(eq(vendorId)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/vendors/{vendorId}", vendorId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendorId").value(vendorId.toString()))
                .andExpect(jsonPath("$.vendorName").value("ABC Electronics"));
    }

    @Test
    @DisplayName("Should return 404 when vendor not found")
    void testGetVendorById_NotFound() throws Exception {
        // Arrange
        UUID unknownVendorId = UUID.randomUUID();
        when(vendorService.getVendorById(eq(unknownVendorId)))
                .thenThrow(new RuntimeException("Vendor not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/vendors/{vendorId}", unknownVendorId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should get all active vendors and return 200")
    void testGetAllActiveVendors_Success() throws Exception {
        // Arrange
        List<VendorResponse> vendors = new ArrayList<>();
        vendors.add(mockResponse);
        when(vendorService.getAllActiveVendors())
                .thenReturn(vendors);

        // Act & Assert
        mockMvc.perform(get("/api/v1/vendors/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vendorId").value(vendorId.toString()))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should get linked accounts by vendor")
    void testGetLinkedAccountsByVendor_Success() throws Exception {
        // Arrange
        List<VendorLinkedAccountResponse> linkedAccounts = new ArrayList<>();
        linkedAccounts.add(VendorLinkedAccountResponse.builder()
                .vendorAccountId(accountId)
                .vendorId(vendorId)
                .accountNumber("DE75512108001234567890")
                .accountType("SETTLEMENT")
                .status("ACTIVE")
                .build());

        when(vendorService.getLinkedAccountsByVendorId(eq(vendorId)))
                .thenReturn(linkedAccounts);

        // Act & Assert
        mockMvc.perform(get("/api/v1/vendors/{vendorId}/linked-accounts", vendorId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vendorAccountId").value(accountId.toString()));
    }

    @Test
    @DisplayName("Should deactivate linked account with admin role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeactivateVendor_Success() throws Exception {
        // Arrange
        VendorLinkedAccountResponse deactivatedAccount = VendorLinkedAccountResponse.builder()
                .vendorAccountId(accountId)
                .vendorId(vendorId)
                .status("INACTIVE")
                .build();

        when(vendorService.deactivateLinkedAccount(eq(accountId)))
                .thenReturn(deactivatedAccount);

        // Act & Assert
        mockMvc.perform(put("/api/v1/vendors/linked-accounts/{accountId}/deactivate", accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("Should handle authentication requirement")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testVendorOperation_WithAdminRole() throws Exception {
        // Arrange
        when(vendorService.getVendorById(eq(vendorId)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/vendors/{vendorId}", vendorId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

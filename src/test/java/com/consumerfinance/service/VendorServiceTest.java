package com.consumerfinance.service;

import com.consumerfinance.domain.Vendor;
import com.consumerfinance.domain.VendorLinkedAccount;
import com.consumerfinance.domain.PrincipalAccount;
import com.consumerfinance.dto.VendorRequest;
import com.consumerfinance.dto.VendorResponse;
import com.consumerfinance.dto.VendorLinkedAccountResponse;
import com.consumerfinance.exception.InvalidAccountException;
import com.consumerfinance.repository.VendorRepository;
import com.consumerfinance.repository.VendorLinkedAccountRepository;
import com.consumerfinance.repository.PrincipalAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VendorService.
 * Tests vendor registration and linked account management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Vendor Service Tests")
class VendorServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private VendorLinkedAccountRepository vendorLinkedAccountRepository;

    @Mock
    private PrincipalAccountRepository principalAccountRepository;

    @InjectMocks
    private VendorService vendorService;

    private UUID vendorId;
    private UUID accountId;
    private VendorRequest validRequest;
    private Vendor mockVendor;

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

        mockVendor = Vendor.builder()
                .vendorId(vendorId)
                .vendorName("ABC Electronics")
                .businessType("RETAIL")
                .registrationNumber("REG123456")
                .gstNumber("GST123456789")
                .contactEmail("vendor@example.com")
                .contactPhone("+1234567890")
                .status(Vendor.VendorStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should register vendor successfully")
    void testRegisterVendor_Success() {
        // Arrange
        when(vendorRepository.findByRegistrationNumber("REG123456")).thenReturn(Optional.empty());
        when(vendorRepository.save(any(Vendor.class))).thenReturn(mockVendor);

        // Act
        VendorResponse response = vendorService.registerVendor(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(vendorId, response.getVendorId());
        assertEquals("ABC Electronics", response.getVendorName());
        assertEquals("ACTIVE", response.getStatus());
        verify(vendorRepository, times(1)).save(any(Vendor.class));
    }

    @Test
    @DisplayName("Should throw exception for duplicate vendor")
    void testRegisterVendor_Duplicate() {
        // Arrange
        when(vendorRepository.findByRegistrationNumber("REG123456"))
                .thenReturn(Optional.of(mockVendor));

        // Act & Assert
        assertThrows(InvalidAccountException.class, () -> vendorService.registerVendor(validRequest));
        verify(vendorRepository, never()).save(any(Vendor.class));
    }

    @Test
    @DisplayName("Should get vendor by ID successfully")
    void testGetVendorById_Success() {
        // Arrange
        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(mockVendor));

        // Act
        VendorResponse response = vendorService.getVendorById(vendorId);

        // Assert
        assertNotNull(response);
        assertEquals(vendorId, response.getVendorId());
        assertEquals("ABC Electronics", response.getVendorName());
    }

    @Test
    @DisplayName("Should throw exception when vendor not found")
    void testGetVendorById_NotFound() {
        // Arrange
        when(vendorRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidAccountException.class, () -> vendorService.getVendorById(vendorId));
    }

    @Test
    @DisplayName("Should get all active vendors")
    void testGetAllActiveVendors_Success() {
        // Arrange
        when(vendorRepository.findAllActiveVendors()).thenReturn(Arrays.asList(mockVendor));

        // Act
        List<VendorResponse> responses = vendorService.getAllActiveVendors();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("ABC Electronics", responses.get(0).getVendorName());
        verify(vendorRepository, times(1)).findAllActiveVendors();
    }

    @Test
    @DisplayName("Should add linked account to vendor successfully")
    void testAddLinkedAccount_Success() {
        // Arrange
        VendorLinkedAccount linkedAccount = VendorLinkedAccount.builder()
                .vendor(mockVendor)
                .accountNumber("DE75512108001234567890")
                .accountType("SETTLEMENT")
                .accountDetails("{\"bankCode\":\"BANK123\"}")
                .build();

        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(mockVendor));
        when(vendorLinkedAccountRepository.countByVendorId(vendorId)).thenReturn(0L);
        when(vendorLinkedAccountRepository.findByAccountNumber("DE75512108001234567890")).thenReturn(Optional.empty());
        when(vendorLinkedAccountRepository.save(any(VendorLinkedAccount.class)))
                .thenReturn(linkedAccount);

        // Act
        VendorLinkedAccountResponse response = vendorService.addLinkedAccount(vendorId, linkedAccount);

        // Assert
        assertNotNull(response);
        verify(vendorLinkedAccountRepository, times(1)).save(any(VendorLinkedAccount.class));
    }

    @Test
    @DisplayName("Should get linked accounts by vendor")
    void testGetLinkedAccountsByVendor_Success() {
        // Arrange
        VendorLinkedAccount linkedAccount = VendorLinkedAccount.builder()
                .vendor(mockVendor)
                .accountNumber("DE75512108001234567890")
                .accountType("SETTLEMENT")
                .status(VendorLinkedAccount.AccountStatus.ACTIVE)
                .build();

        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(mockVendor));
        when(vendorLinkedAccountRepository.findByVendorId(vendorId))
                .thenReturn(Arrays.asList(linkedAccount));

        // Act
        List<VendorLinkedAccountResponse> responses = vendorService.getLinkedAccountsByVendorId(vendorId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(vendorLinkedAccountRepository, times(1)).findByVendorId(vendorId);
    }

    @Test
    @DisplayName("Should deactivate vendor successfully")
    void testDeactivateVendor_Success() {
        // Arrange
        UUID linkedAccountId = UUID.randomUUID();
        VendorLinkedAccount linkedAccount = VendorLinkedAccount.builder()
                .vendorAccountId(linkedAccountId)
                .vendor(mockVendor)
                .accountNumber("DE75512108001234567890")
                .accountType("SETTLEMENT")
                .status(VendorLinkedAccount.AccountStatus.ACTIVE)
                .build();

        when(vendorLinkedAccountRepository.findById(linkedAccountId))
                .thenReturn(Optional.of(linkedAccount));

        linkedAccount.setStatus(VendorLinkedAccount.AccountStatus.INACTIVE);
        when(vendorLinkedAccountRepository.save(any(VendorLinkedAccount.class)))
                .thenReturn(linkedAccount);

        // Act
        VendorLinkedAccountResponse response = vendorService.deactivateLinkedAccount(linkedAccountId);

        // Assert
        assertNotNull(response);
        verify(vendorLinkedAccountRepository, times(1)).save(any(VendorLinkedAccount.class));
    }

    @Test
    @DisplayName("Should get linked accounts by principal account")
    void testGetLinkedAccountsByPrincipalAccount_Success() {
        // Arrange
        UUID principalAccountId = UUID.randomUUID();
        PrincipalAccount principalAccount = PrincipalAccount.builder()
                .principalAccountId(principalAccountId)
                .accountNumber("DE75512108001234567890")
                .build();

        VendorLinkedAccount linkedAccount = VendorLinkedAccount.builder()
                .vendor(mockVendor)
                .status(VendorLinkedAccount.AccountStatus.ACTIVE)
                .build();

        when(principalAccountRepository.findById(principalAccountId))
                .thenReturn(Optional.of(principalAccount));
        when(vendorLinkedAccountRepository.findByStatus(VendorLinkedAccount.AccountStatus.ACTIVE))
                .thenReturn(Arrays.asList(linkedAccount));

        // Act
        List<VendorLinkedAccountResponse> responses = vendorService.getLinkedAccountsByPrincipalAccountId(principalAccountId);

        // Assert
        assertNotNull(responses);
    }

    @Test
    @DisplayName("Should activate vendor successfully")
    void testActivateVendor_Success() {
        // Arrange
        UUID linkedAccountId = UUID.randomUUID();
        VendorLinkedAccount linkedAccount = VendorLinkedAccount.builder()
                .vendorAccountId(linkedAccountId)
                .vendor(mockVendor)
                .accountNumber("DE75512108001234567890")
                .accountType("SETTLEMENT")
                .status(VendorLinkedAccount.AccountStatus.PENDING)
                .build();

        when(vendorLinkedAccountRepository.findById(linkedAccountId))
                .thenReturn(Optional.of(linkedAccount));
        
        // Create a separate object to return from save with all fields including vendor
        VendorLinkedAccount savedAccount = VendorLinkedAccount.builder()
                .vendorAccountId(linkedAccountId)
                .vendor(mockVendor)
                .accountNumber("DE75512108001234567890")
                .accountType("SETTLEMENT")
                .status(VendorLinkedAccount.AccountStatus.ACTIVE)
                .build();
        
        when(vendorLinkedAccountRepository.save(any(VendorLinkedAccount.class)))
                .thenReturn(savedAccount);

        // Act
        VendorLinkedAccountResponse response = vendorService.activateLinkedAccount(linkedAccountId);

        // Assert
        assertNotNull(response);
        verify(vendorLinkedAccountRepository, times(1)).save(any(VendorLinkedAccount.class));
    }

    @Test
    @DisplayName("Should deactivate linked account")
    void testDeactivateLinkedAccount_Success() {
        // Arrange
        UUID linkedAccountId = UUID.randomUUID();
        VendorLinkedAccount linkedAccount = VendorLinkedAccount.builder()
                .vendorAccountId(linkedAccountId)
                .vendor(mockVendor)
                .accountNumber("DE75512108001234567890")
                .accountType("SETTLEMENT")
                .status(VendorLinkedAccount.AccountStatus.ACTIVE)
                .build();

        VendorLinkedAccount deactivatedAccount = VendorLinkedAccount.builder()
                .vendorAccountId(linkedAccountId)
                .vendor(mockVendor)
                .accountNumber("DE75512108001234567890")
                .accountType("SETTLEMENT")
                .status(VendorLinkedAccount.AccountStatus.INACTIVE)
                .build();

        when(vendorLinkedAccountRepository.findById(linkedAccountId))
                .thenReturn(Optional.of(linkedAccount));
        when(vendorLinkedAccountRepository.save(any(VendorLinkedAccount.class)))
                .thenReturn(deactivatedAccount);

        // Act
        VendorLinkedAccountResponse response = vendorService.deactivateLinkedAccount(linkedAccountId);

        // Assert
        assertNotNull(response);
        verify(vendorLinkedAccountRepository, times(1)).save(any(VendorLinkedAccount.class));
    }
}

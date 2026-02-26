package com.consumerfinance.service;

import com.consumerfinance.domain.PrincipalAccount;
import com.consumerfinance.domain.Consumer;
import com.consumerfinance.dto.PrincipalAccountRequest;
import com.consumerfinance.dto.PrincipalAccountResponse;
import com.consumerfinance.exception.ConsumerNotFoundException;
import com.consumerfinance.exception.InvalidAccountException;
import com.consumerfinance.repository.PrincipalAccountRepository;
import com.consumerfinance.repository.ConsumerRepository;
import com.consumerfinance.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PrincipalAccountService.
 * Tests account linking, verification, and management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Principal Account Service Tests")
class PrincipalAccountServiceTest {

    @Mock
    private PrincipalAccountRepository accountRepository;

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private PrincipalAccountService principalAccountService;

    private UUID consumerId;
    private UUID accountId;
    private Consumer mockConsumer;
    private PrincipalAccountRequest validRequest;
    private PrincipalAccount mockAccount;

    @BeforeEach
    void setUp() {
        consumerId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        mockConsumer = Consumer.builder()
                .consumerId(consumerId)
                .name("John Doe")
                .email("john@example.com")
                .status(Consumer.ConsumerStatus.ACTIVE)
                .build();

        validRequest = PrincipalAccountRequest.builder()
                .accountNumber("DE75512108001234567890")
                .accountHolderName("John Doe")
                .bankCode("BANK123")
                .build();

        mockAccount = PrincipalAccount.builder()
                .principalAccountId(accountId)
                .consumer(mockConsumer)
                .accountNumber("DE75512108001234567890")
                .accountHolderName("John Doe")
                .bankCode("BANK123")
                .verificationStatus(PrincipalAccount.VerificationStatus.PENDING)
                .linkedDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should link principal account successfully")
    void testLinkPrincipalAccount_Success() {
        // Arrange
        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));
        when(accountRepository.findByConsumer(mockConsumer)).thenReturn(Optional.empty());
        when(accountRepository.save(any(PrincipalAccount.class))).thenReturn(mockAccount);

        // Act
        PrincipalAccountResponse response = principalAccountService.linkPrincipalAccount(consumerId, validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(accountId.toString(), response.getPrincipalAccountId());
        assertEquals("John Doe", response.getAccountHolderName());
        assertEquals("PENDING", response.getVerificationStatus());
        verify(accountRepository, times(1)).save(any(PrincipalAccount.class));
    }

    @Test
    @DisplayName("Should throw exception when consumer not found")
    void testLinkPrincipalAccount_ConsumerNotFound() {
        // Arrange
        when(consumerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConsumerNotFoundException.class, () -> 
            principalAccountService.linkPrincipalAccount(consumerId, validRequest));
        verify(accountRepository, never()).save(any(PrincipalAccount.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid IBAN")
    void testLinkPrincipalAccount_InvalidIBAN() {
        // Arrange
        PrincipalAccountRequest invalidRequest = PrincipalAccountRequest.builder()
                .accountNumber("INVALID")
                .accountHolderName("John Doe")
                .bankCode("BANK123")
                .build();

        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));

        // Act & Assert
        assertThrows(InvalidAccountException.class, () -> 
            principalAccountService.linkPrincipalAccount(consumerId, invalidRequest));
    }

    @Test
    @DisplayName("Should replace existing account when linking new one")
    void testLinkPrincipalAccount_ReplaceExisting() {
        // Arrange
        PrincipalAccount existingAccount = mockAccount;
        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));
        when(accountRepository.findByConsumer(mockConsumer)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(PrincipalAccount.class))).thenReturn(mockAccount);

        // Act
        PrincipalAccountResponse response = principalAccountService.linkPrincipalAccount(consumerId, validRequest);

        // Assert
        assertNotNull(response);
        verify(accountRepository, times(1)).delete(existingAccount);
        verify(accountRepository, times(1)).save(any(PrincipalAccount.class));
    }

    @Test
    @DisplayName("Should get principal account successfully")
    void testGetPrincipalAccount_Success() {
        // Arrange
        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));
        when(accountRepository.findByConsumer(mockConsumer)).thenReturn(Optional.of(mockAccount));

        // Act
        PrincipalAccountResponse response = principalAccountService.getPrincipalAccount(consumerId);

        // Assert
        assertNotNull(response);
        assertEquals(accountId.toString(), response.getPrincipalAccountId());
        assertEquals("John Doe", response.getAccountHolderName());
    }

    @Test
    @DisplayName("Should throw exception when account not found")
    void testGetPrincipalAccount_NotFound() {
        // Arrange
        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));
        when(accountRepository.findByConsumer(mockConsumer)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidAccountException.class, () -> 
            principalAccountService.getPrincipalAccount(consumerId));
    }

    @Test
    @DisplayName("Should update principal account successfully")
    void testUpdatePrincipalAccount_Success() {
        // Arrange
        PrincipalAccountRequest updateRequest = PrincipalAccountRequest.builder()
                .accountNumber("DE75512108009876543210")
                .accountHolderName("John Doe")
                .bankCode("BANK456")
                .build();

        PrincipalAccount updatedAccount = mockAccount;
        updatedAccount.setAccountNumber("DE75512108009876543210");
        updatedAccount.setBankCode("BANK456");

        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));
        when(accountRepository.findByConsumer(mockConsumer)).thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any(PrincipalAccount.class))).thenReturn(updatedAccount);

        // Act
        PrincipalAccountResponse response = principalAccountService.updatePrincipalAccount(consumerId, updateRequest);

        // Assert
        assertNotNull(response);
        verify(accountRepository, times(1)).save(any(PrincipalAccount.class));
    }

    @Test
    @DisplayName("Should verify account successfully with admin role")
    void testVerifyAccount_Success() {
        // Arrange
        mockAccount.setVerificationStatus(PrincipalAccount.VerificationStatus.VERIFIED);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any(PrincipalAccount.class))).thenReturn(mockAccount);

        // Act
        PrincipalAccountResponse response = principalAccountService.verifyAccount(accountId);

        // Assert
        assertNotNull(response);
        assertEquals("VERIFIED", response.getVerificationStatus());
        verify(accountRepository, times(1)).save(any(PrincipalAccount.class));
    }

    @Test
    @DisplayName("Should reject account successfully")
    void testRejectAccount_Success() {
        // Arrange
        mockAccount.setVerificationStatus(PrincipalAccount.VerificationStatus.REJECTED);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any(PrincipalAccount.class))).thenReturn(mockAccount);

        // Act
        PrincipalAccountResponse response = principalAccountService.rejectAccount(accountId, "Account verification failed");

        // Assert
        assertNotNull(response);
        assertEquals("REJECTED", response.getVerificationStatus());
        verify(accountRepository, times(1)).save(any(PrincipalAccount.class));
    }

    @Test
    @DisplayName("Should throw exception for name mismatch")
    void testLinkPrincipalAccount_NameMismatch() {
        // Arrange
        PrincipalAccountRequest mismatchRequest = PrincipalAccountRequest.builder()
                .accountNumber("DE75512108001234567890")
                .accountHolderName("Jane Smith")
                .bankCode("BANK123")
                .build();

        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));

        // Act & Assert
        assertThrows(InvalidAccountException.class, () -> 
            principalAccountService.linkPrincipalAccount(consumerId, mismatchRequest));
    }
}

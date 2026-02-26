package com.consumerfinance.service;

import com.consumerfinance.domain.Consumer;
import com.consumerfinance.dto.ConsumerRequest;
import com.consumerfinance.dto.ConsumerResponse;
import com.consumerfinance.exception.ConsumerNotFoundException;
import com.consumerfinance.exception.DuplicateEmailException;
import com.consumerfinance.exception.DuplicatePhoneException;
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
 * Unit tests for ConsumerService.
 * Tests consumer registration, profile management, and KYC operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Consumer Service Tests")
class ConsumerServiceTest {

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private ConsumerService consumerService;

    private UUID consumerId;
    private ConsumerRequest validRequest;
    private Consumer mockConsumer;

    @BeforeEach
    void setUp() {
        consumerId = UUID.randomUUID();

        validRequest = ConsumerRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .identityType("PASSPORT")
                .identityNumber("AB123456")
                .build();

        mockConsumer = Consumer.builder()
                .consumerId(consumerId)
                .name("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .identityType("PASSPORT")
                .identityNumber("AB123456")
                .status(Consumer.ConsumerStatus.ACTIVE)
                .kycStatus(Consumer.KYCStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create consumer successfully with valid request")
    void testCreateConsumer_Success() {
        // Arrange
        when(consumerRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(consumerRepository.findByPhone("+1234567890")).thenReturn(Optional.empty());
        when(consumerRepository.save(any(Consumer.class))).thenReturn(mockConsumer);

        // Act
        ConsumerResponse response = consumerService.createConsumer(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(consumerId.toString(), response.getConsumerId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        verify(consumerRepository, times(1)).save(any(Consumer.class));
    }

    @Test
    @DisplayName("Should throw exception for duplicate email")
    void testCreateConsumer_DuplicateEmail() {
        // Arrange
        when(consumerRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockConsumer));

        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> consumerService.createConsumer(validRequest));
        verify(consumerRepository, never()).save(any(Consumer.class));
    }

    @Test
    @DisplayName("Should throw exception for duplicate phone")
    void testCreateConsumer_DuplicatePhone() {
        // Arrange
        when(consumerRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(consumerRepository.findByPhone("+1234567890"))
                .thenReturn(Optional.of(mockConsumer));

        // Act & Assert
        assertThrows(DuplicatePhoneException.class, () -> consumerService.createConsumer(validRequest));
        verify(consumerRepository, never()).save(any(Consumer.class));
    }

    @Test
    @DisplayName("Should get consumer by ID successfully")
    void testGetConsumer_Success() {
        // Arrange
        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));

        // Act
        ConsumerResponse response = consumerService.getConsumer(consumerId);

        // Assert
        assertNotNull(response);
        assertEquals(consumerId.toString(), response.getConsumerId());
        assertEquals("John Doe", response.getName());
        verify(consumerRepository, times(1)).findById(consumerId);
    }

    @Test
    @DisplayName("Should throw exception when consumer not found")
    void testGetConsumer_NotFound() {
        // Arrange
        when(consumerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConsumerNotFoundException.class, () -> consumerService.getConsumer(consumerId));
    }

    @Test
    @DisplayName("Should update consumer profile successfully")
    void testUpdateConsumer_Success() {
        // Arrange
        ConsumerRequest updateRequest = ConsumerRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .phone("+9876543210")
                .identityType("PASSPORT")
                .identityNumber("AB654321")
                .build();

        Consumer updatedConsumer = Consumer.builder()
                .consumerId(consumerId)
                .name("Jane Doe")
                .email("jane@example.com")
                .phone("+9876543210")
                .status(Consumer.ConsumerStatus.ACTIVE)
                .kycStatus(Consumer.KYCStatus.PENDING)
                .build();

        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));
        when(consumerRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(consumerRepository.findByPhone("+9876543210")).thenReturn(Optional.empty());
        when(consumerRepository.save(any(Consumer.class))).thenReturn(updatedConsumer);

        // Act
        ConsumerResponse response = consumerService.updateConsumer(consumerId, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane@example.com", response.getEmail());
        verify(consumerRepository, times(1)).save(any(Consumer.class));
    }

    @Test
    @DisplayName("Should throw exception when updating with duplicate email")
    void testUpdateConsumer_DuplicateEmail() {
        // Arrange
        ConsumerRequest updateRequest = ConsumerRequest.builder()
                .name("Jane Doe")
                .email("existing@example.com")
                .phone("+1234567890")
                .identityType("PASSPORT")
                .identityNumber("AB654321")
                .build();

        Consumer existingConsumer = Consumer.builder()
                .consumerId(UUID.randomUUID())
                .email("existing@example.com")
                .build();

        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));
        when(consumerRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingConsumer));

        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> consumerService.updateConsumer(consumerId, updateRequest));
    }

    @Test
    @DisplayName("Should update KYC status successfully")
    void testUpdateKYCStatus_Success() {
        // Arrange
        mockConsumer.setKycStatus(Consumer.KYCStatus.VERIFIED);
        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));
        when(consumerRepository.save(any(Consumer.class))).thenReturn(mockConsumer);

        // Act
        ConsumerResponse response = consumerService.updateKYCStatus(consumerId, "VERIFIED");

        // Assert
        assertNotNull(response);
        assertEquals("VERIFIED", response.getKycStatus());
        verify(consumerRepository, times(1)).save(any(Consumer.class));
    }

    @Test
    @DisplayName("Should delete consumer successfully")
    void testDeleteConsumer_Success() {
        // Arrange
        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));

        // Act
        consumerService.deleteConsumer(consumerId);

        // Assert
        verify(consumerRepository, times(1)).delete(mockConsumer);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent consumer")
    void testDeleteConsumer_NotFound() {
        // Arrange
        when(consumerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConsumerNotFoundException.class, () -> consumerService.deleteConsumer(consumerId));
    }

    @Test
    @DisplayName("Should update consumer status successfully")
    void testUpdateConsumerStatus_Success() {
        // Arrange
        mockConsumer.setStatus(Consumer.ConsumerStatus.INACTIVE);
        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));
        when(consumerRepository.save(any(Consumer.class))).thenReturn(mockConsumer);

        // Act
        ConsumerResponse response = consumerService.updateConsumerStatus(consumerId, "INACTIVE");

        // Assert
        assertNotNull(response);
        assertEquals("INACTIVE", response.getStatus());
        verify(consumerRepository, times(1)).save(any(Consumer.class));
    }
}

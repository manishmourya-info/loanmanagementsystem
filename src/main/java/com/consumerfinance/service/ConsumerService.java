package com.consumerfinance.service;

import com.consumerfinance.domain.Consumer;
import com.consumerfinance.dto.ConsumerRequest;
import com.consumerfinance.dto.ConsumerResponse;
import com.consumerfinance.exception.DuplicateEmailException;
import com.consumerfinance.exception.DuplicatePhoneException;
import com.consumerfinance.exception.ConsumerNotFoundException;
import com.consumerfinance.repository.ConsumerRepository;
import com.consumerfinance.domain.AuditLog;
import com.consumerfinance.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for consumer management and onboarding
 * Handles consumer registration, profile management, and KYC updates
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsumerService {

    private final ConsumerRepository consumerRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Create a new consumer with KYC data
     * T015: Create Consumer with validation
     */
    public ConsumerResponse createConsumer(ConsumerRequest request) {
        log.info("Creating new consumer: {}", request.getEmail());

        // Check for duplicate email
        if (consumerRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Attempt to create consumer with duplicate email: {}", request.getEmail());
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }

        // Check for duplicate phone
        if (consumerRepository.findByPhone(request.getPhone()).isPresent()) {
            log.warn("Attempt to create consumer with duplicate phone: {}", request.getPhone());
            throw new DuplicatePhoneException("Phone already registered: " + request.getPhone());
        }

        // Create consumer entity
        Consumer consumer = Consumer.builder()
                .consumerId(UUID.randomUUID())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .identityType(request.getIdentityType())
                .identityNumber(request.getIdentityNumber())
                .status(Consumer.ConsumerStatus.ACTIVE)
                .kycStatus(Consumer.KYCStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Consumer saved = consumerRepository.save(consumer);
        log.info("Consumer created successfully: {}", saved.getConsumerId());

        // Create audit log
        createAuditLog("CONSUMER_CREATED", saved.getConsumerId().toString(), 
                      request.getName(), "Consumer registered successfully");

        return mapToConsumerResponse(saved);
    }

    /**
     * Get consumer by ID
     */
    @Transactional(readOnly = true)
    public ConsumerResponse getConsumer(UUID consumerId) {
        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));
        return mapToConsumerResponse(consumer);
    }

    /**
     * Update consumer profile
     * Resets KYC status to PENDING if identity changes
     */
    public ConsumerResponse updateConsumer(UUID consumerId, ConsumerRequest request) {
        log.info("Updating consumer: {}", consumerId);

        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));

        // Check for duplicate email if email changed
        if (!consumer.getEmail().equals(request.getEmail())) {
            if (consumerRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new DuplicateEmailException("Email already registered: " + request.getEmail());
            }
        }

        // Check for duplicate phone if phone changed
        if (!consumer.getPhone().equals(request.getPhone())) {
            if (consumerRepository.findByPhone(request.getPhone()).isPresent()) {
                throw new DuplicatePhoneException("Phone already registered: " + request.getPhone());
            }
        }

        // Update fields
        boolean identityChanged = !consumer.getIdentityType().equals(request.getIdentityType()) ||
                                !consumer.getIdentityNumber().equals(request.getIdentityNumber());

        consumer.setName(request.getName());
        consumer.setEmail(request.getEmail());
        consumer.setPhone(request.getPhone());
        consumer.setIdentityType(request.getIdentityType());
        consumer.setIdentityNumber(request.getIdentityNumber());

        // Reset KYC if identity changed
        if (identityChanged) {
            consumer.setKycStatus(Consumer.KYCStatus.PENDING);
            log.info("KYC status reset to PENDING for consumer {}", consumerId);
        }

        consumer.setUpdatedAt(LocalDateTime.now());
        Consumer updated = consumerRepository.save(consumer);

        createAuditLog("CONSUMER_UPDATED", consumerId.toString(),
                      updated.getName(), "Consumer profile updated");

        return mapToConsumerResponse(updated);
    }

    /**
     * Get all consumers with pagination and filtering
     */
    @Transactional(readOnly = true)
    public Page<ConsumerResponse> getAllConsumers(Pageable pageable, Consumer.ConsumerStatus status, String search) {
        Page<Consumer> consumers;

        if (status != null && search != null && !search.isEmpty()) {
            consumers = consumerRepository.findByStatusAndNameContainingOrStatusAndEmailContainingOrStatusAndPhoneContaining(
                    status, search, status, search, status, search, pageable);
        } else if (status != null) {
            consumers = consumerRepository.findByStatus(status, pageable);
        } else if (search != null && !search.isEmpty()) {
            consumers = consumerRepository.findByNameContainingOrEmailContainingOrPhoneContaining(search, search, search, pageable);
        } else {
            consumers = consumerRepository.findAll(pageable);
        }

        return consumers.map(this::mapToConsumerResponse);
    }

    /**
     * Get consumer's KYC status
     */
    @Transactional(readOnly = true)
    public Consumer.KYCStatus getKYCStatus(UUID consumerId) {
        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));
        return consumer.getKycStatus();
    }

    /**
     * Check if consumer has verified KYC and account for loan eligibility
     */
    @Transactional(readOnly = true)
    public boolean isLoanEligible(UUID consumerId) {
        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));

        return consumer.getKycStatus() == Consumer.KYCStatus.VERIFIED &&
               consumer.getStatus() == Consumer.ConsumerStatus.ACTIVE;
    }

    /**
     * Suspend consumer account
     */
    public ConsumerResponse suspendConsumer(UUID consumerId, String reason) {
        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));

        consumer.setStatus(Consumer.ConsumerStatus.SUSPENDED);
        consumer.setUpdatedAt(LocalDateTime.now());
        Consumer updated = consumerRepository.save(consumer);

        createAuditLog("CONSUMER_SUSPENDED", consumerId.toString(),
                      consumer.getName(), "Reason: " + reason);

        return mapToConsumerResponse(updated);
    }

    /**
     * Deactivate consumer account
     */
    public ConsumerResponse deactivateConsumer(UUID consumerId) {
        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));

        consumer.setStatus(Consumer.ConsumerStatus.INACTIVE);
        consumer.setUpdatedAt(LocalDateTime.now());
        Consumer updated = consumerRepository.save(consumer);

        createAuditLog("CONSUMER_DEACTIVATED", consumerId.toString(),
                      consumer.getName(), "Account deactivated");

        return mapToConsumerResponse(updated);
    }

    /**
     * Helper: Map Consumer entity to DTO
     */
    private ConsumerResponse mapToConsumerResponse(Consumer consumer) {
        return ConsumerResponse.builder()
                .consumerId(consumer.getConsumerId().toString())
                .name(consumer.getName())
                .email(consumer.getEmail())
                .phone(consumer.getPhone())
                .identityType(consumer.getIdentityType())
                .identityNumber(consumer.getIdentityNumber())
                .status(consumer.getStatus().toString())
                .kycStatus(consumer.getKycStatus().toString())
                .createdAt(consumer.getCreatedAt())
                .updatedAt(consumer.getUpdatedAt())
                .build();
    }

    /**
     * Update KYC status for a consumer
     */
    public ConsumerResponse updateKYCStatus(UUID consumerId, String kycStatus) {
        log.info("Updating KYC status for consumer: {}", consumerId);

        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));

        consumer.setKycStatus(Consumer.KYCStatus.valueOf(kycStatus.toUpperCase()));
        consumer.setUpdatedAt(LocalDateTime.now());

        Consumer updated = consumerRepository.save(consumer);
        log.info("KYC status updated for consumer: {}", consumerId);

        createAuditLog("KYC_STATUS_UPDATED", consumerId.toString(), consumer.getName(),
                      "KYC status changed to " + kycStatus);

        return mapToConsumerResponse(updated);
    }

    /**
     * Delete a consumer
     */
    public void deleteConsumer(UUID consumerId) {
        log.info("Deleting consumer: {}", consumerId);

        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));

        consumerRepository.delete(consumer);
        log.info("Consumer deleted: {}", consumerId);

        createAuditLog("CONSUMER_DELETED", consumerId.toString(), consumer.getName(),
                      "Consumer account deleted");
    }

    /**
     * Update consumer status (ACTIVE/INACTIVE)
     */
    public ConsumerResponse updateConsumerStatus(UUID consumerId, String status) {
        log.info("Updating consumer status: {}", consumerId);

        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));

        consumer.setStatus(Consumer.ConsumerStatus.valueOf(status.toUpperCase()));
        consumer.setUpdatedAt(LocalDateTime.now());

        Consumer updated = consumerRepository.save(consumer);
        log.info("Consumer status updated for consumer: {}", consumerId);

        createAuditLog("CONSUMER_STATUS_UPDATED", consumerId.toString(), consumer.getName(),
                      "Consumer status changed to " + status);

        return mapToConsumerResponse(updated);
    }

    /**
     * Helper: Create audit log entry
     */
    private void createAuditLog(String action, String consumerId, String consumerName, String details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .loanId(null) // Consumer audit doesn't have loan
                .userId("system") // Will be filled by security context in production
                .details(consumerName + " - " + details)
                .status(AuditLog.AuditStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }
}

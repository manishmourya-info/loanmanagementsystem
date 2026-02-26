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
import com.consumerfinance.domain.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for principal account (banking account) management
 * Handles linking, verification, and management of consumer's primary banking account
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrincipalAccountService {

    private final PrincipalAccountRepository accountRepository;
    private final ConsumerRepository consumerRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Link a principal account to a consumer
     * Only one principal account allowed per consumer (replaces if exists)
     * T016: Link Principal Account with verification
     */
    public PrincipalAccountResponse linkPrincipalAccount(UUID consumerId, PrincipalAccountRequest request) {
        log.info("Linking principal account for consumer: {}", consumerId);

        // Verify consumer exists
        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));

        // Validate account holder name matches consumer (80%+ fuzzy match)
        if (!nameFuzzyMatch(consumer.getName(), request.getAccountHolderName(), 0.80)) {
            log.warn("Account holder name doesn't match consumer name for: {}", consumerId);
            throw new InvalidAccountException(
                "Account holder name doesn't match consumer name. Expected: " + consumer.getName());
        }

        // Validate account number format (IBAN)
        if (!isValidIBAN(request.getAccountNumber())) {
            throw new InvalidAccountException("Invalid account number format (IBAN required)");
        }

        // Delete existing account if present (only one principal account per consumer)
        Optional<PrincipalAccount> existing = accountRepository.findByConsumer(consumer);
        if (existing.isPresent()) {
            log.info("Replacing existing principal account for consumer: {}", consumerId);
            accountRepository.delete(existing.get());
        }

        // Create new account
        PrincipalAccount account = PrincipalAccount.builder()
                .principalAccountId(UUID.randomUUID())
                .consumer(consumer)
                .accountNumber(request.getAccountNumber())
                .accountHolderName(request.getAccountHolderName())
                .bankCode(request.getBankCode())
                .verificationStatus(PrincipalAccount.VerificationStatus.PENDING)
                .linkedDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PrincipalAccount saved = accountRepository.save(account);
        log.info("Principal account linked successfully: {}", saved.getPrincipalAccountId());

        // Create audit log
        createAuditLog("ACCOUNT_LINKED", consumerId.toString(), consumer.getName(),
                      "Account " + maskAccountNumber(request.getAccountNumber()) + " linked");

        return mapToResponse(saved);
    }

    /**
     * Get principal account for a consumer
     */
    @Transactional(readOnly = true)
    public PrincipalAccountResponse getPrincipalAccount(UUID consumerId) {
        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));

        PrincipalAccount account = accountRepository.findByConsumer(consumer)
                .orElseThrow(() -> new InvalidAccountException("No principal account linked for consumer: " + consumerId));

        return mapToResponse(account);
    }

    /**
     * Update principal account
     * Resets verification status to PENDING
     */
    public PrincipalAccountResponse updatePrincipalAccount(UUID consumerId, PrincipalAccountRequest request) {
        log.info("Updating principal account for consumer: {}", consumerId);

        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));

        PrincipalAccount account = accountRepository.findByConsumer(consumer)
                .orElseThrow(() -> new InvalidAccountException("No principal account linked for consumer: " + consumerId));

        // Cannot update if consumer has ACTIVE loans (business rule)
        // This would be checked in PersonalLoanService

        // Update fields
        account.setAccountNumber(request.getAccountNumber());
        account.setAccountHolderName(request.getAccountHolderName());
        account.setBankCode(request.getBankCode());
        account.setVerificationStatus(PrincipalAccount.VerificationStatus.PENDING);
        account.setUpdatedAt(LocalDateTime.now());

        PrincipalAccount updated = accountRepository.save(account);
        log.info("Principal account updated: {}", updated.getPrincipalAccountId());

        createAuditLog("ACCOUNT_UPDATED", consumerId.toString(), consumer.getName(),
                      "Account updated and verification reset to PENDING");

        return mapToResponse(updated);
    }

    /**
     * Verify principal account (admin operation)
     * Sets status to VERIFIED
     */
    public PrincipalAccountResponse verifyAccount(UUID accountId) {
        log.info("Verifying principal account: {}", accountId);

        PrincipalAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new InvalidAccountException("Account not found: " + accountId));

        account.setVerificationStatus(PrincipalAccount.VerificationStatus.VERIFIED);
        account.setVerifiedDate(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        PrincipalAccount updated = accountRepository.save(account);
        log.info("Principal account verified: {}", accountId);

        createAuditLog("ACCOUNT_VERIFIED", account.getConsumer().getConsumerId().toString(),
                      account.getConsumer().getName(),
                      "Account " + maskAccountNumber(account.getAccountNumber()) + " verified");

        return mapToResponse(updated);
    }

    /**
     * Get account verification status
     */
    @Transactional(readOnly = true)
    public PrincipalAccount.VerificationStatus getVerificationStatus(UUID accountId) {
        PrincipalAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new InvalidAccountException("Account not found: " + accountId));
        return account.getVerificationStatus();
    }

    /**
     * Check if account is verified
     */
    @Transactional(readOnly = true)
    public boolean isAccountVerified(UUID consumerId) {
        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));

        Optional<PrincipalAccount> account = accountRepository.findByConsumer(consumer);
        return account.isPresent() && account.get().getVerificationStatus() == PrincipalAccount.VerificationStatus.VERIFIED;
    }

    /**
     * Reject account verification
     */
    public PrincipalAccountResponse rejectAccount(UUID accountId, String reason) {
        PrincipalAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new InvalidAccountException("Account not found: " + accountId));

        account.setVerificationStatus(PrincipalAccount.VerificationStatus.REJECTED);
        account.setUpdatedAt(LocalDateTime.now());
        PrincipalAccount updated = accountRepository.save(account);

        createAuditLog("ACCOUNT_REJECTED", account.getConsumer().getConsumerId().toString(),
                      account.getConsumer().getName(), "Rejection reason: " + reason);

        return mapToResponse(updated);
    }

    /**
     * Helper: 80%+ fuzzy match for name validation
     */
    private boolean nameFuzzyMatch(String consumerName, String accountHolderName, double threshold) {
        // Simple implementation - in production use Levenshtein distance or similar
        String name1 = consumerName.toLowerCase().replaceAll("[^a-z]", "");
        String name2 = accountHolderName.toLowerCase().replaceAll("[^a-z]", "");

        if (name1.isEmpty() || name2.isEmpty()) {
            return false;
        }

        // If one name is significantly contained in the other
        if (name1.contains(name2) || name2.contains(name1)) {
            return true;
        }

        // Calculate similarity (basic Levenshtein-like check)
        double similarity = calculateNameSimilarity(name1, name2);
        return similarity >= threshold;
    }

    /**
     * Helper: Calculate name similarity
     */
    private double calculateNameSimilarity(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;

        int distance = levenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLen;
    }

    /**
     * Helper: Levenshtein distance calculation
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Helper: Validate IBAN format (simplified check)
     */
    private boolean isValidIBAN(String iban) {
        if (iban == null || iban.length() < 15 || iban.length() > 34) {
            return false;
        }
        // IBAN starts with 2 letters and 2 digits
        return iban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]*$");
    }

    /**
     * Helper: Mask account number for logging
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    /**
     * Helper: Map to DTO response
     */
    private PrincipalAccountResponse mapToResponse(PrincipalAccount account) {
        return PrincipalAccountResponse.builder()
                .principalAccountId(account.getPrincipalAccountId().toString())
                .consumerId(account.getConsumer().getConsumerId().toString())
                .accountNumber(maskAccountNumber(account.getAccountNumber()))
                .accountHolderName(account.getAccountHolderName())
                .bankCode(account.getBankCode())
                .verificationStatus(account.getVerificationStatus().toString())
                .linkedDate(account.getLinkedDate())
                .verifiedDate(account.getVerifiedDate())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    /**
     * Helper: Create audit log
     */
    private void createAuditLog(String action, String consumerId, String consumerName, String details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .loanId(null)
                .userId("system")
                .details(consumerName + " - " + details)
                .status(AuditLog.AuditStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }
}

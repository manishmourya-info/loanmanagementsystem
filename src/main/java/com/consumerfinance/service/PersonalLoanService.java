package com.consumerfinance.service;

import com.consumerfinance.domain.LoanRepayment;
import com.consumerfinance.domain.PersonalLoan;
import com.consumerfinance.domain.Consumer;
import com.consumerfinance.domain.PrincipalAccount;
import com.consumerfinance.dto.CreateLoanRequest;
import com.consumerfinance.dto.LoanResponse;
import com.consumerfinance.dto.EMICalculationRequest;
import com.consumerfinance.repository.*;
import com.consumerfinance.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for personal loan management operations.
 * Handles loan creation, retrieval, approval, rejection, disbursement, and status management.
 * Phases 4-6: T022-T028, T029-T034, T035-T038
 */
@Slf4j
@Service
@Transactional
public class PersonalLoanService {

    private final PersonalLoanRepository loanRepository;
    private final LoanRepaymentRepository repaymentRepository;
    private final EMICalculationService emiCalculationService;
    private final ConsumerRepository consumerRepository;
    private final PrincipalAccountRepository principalAccountRepository;

    public PersonalLoanService(PersonalLoanRepository loanRepository,
                             LoanRepaymentRepository repaymentRepository,
                             EMICalculationService emiCalculationService,
                             ConsumerRepository consumerRepository,
                             PrincipalAccountRepository principalAccountRepository) {
        this.loanRepository = loanRepository;
        this.repaymentRepository = repaymentRepository;
        this.emiCalculationService = emiCalculationService;
        this.consumerRepository = consumerRepository;
        this.principalAccountRepository = principalAccountRepository;
    }

    /**
     * Create a new personal loan for a customer with validation, EMI calculation, and schedule generation.
     * T022: Create PersonalLoanService with createLoan (validate eligibility, constraints)
     *
     * @param request the loan creation request
     * @return the created loan details
     */
    public LoanResponse createLoan(CreateLoanRequest request) {
        log.info("Creating new personal loan for customer ID: {}, amount: {}", 
                request.getCustomerId(), request.getPrincipalAmount());

        Consumer consumer = consumerRepository.findById(UUID.fromString(request.getCustomerId()))
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + request.getCustomerId()));

        validateLoanEligibility(consumer, request);

        var emiRequest = EMICalculationRequest.builder()
                .principalAmount(request.getPrincipalAmount())
                .annualInterestRate(request.getAnnualInterestRate())
                .tenureMonths(request.getLoanTenureMonths())
                .build();
        
        var emiResponse = emiCalculationService.calculateEMI(emiRequest);

        PersonalLoan loan = PersonalLoan.builder()
                .consumer(consumer)
                .principalAmount(request.getPrincipalAmount())
                .annualInterestRate(request.getAnnualInterestRate())
                .loanTenureMonths(request.getLoanTenureMonths())
                .monthlyEMI(emiResponse.getMonthlyEMI())
                .totalInterestPayable(emiResponse.getTotalInterest())
                .outstandingBalance(request.getPrincipalAmount())
                .remainingTenure(request.getLoanTenureMonths())
                .status(PersonalLoan.LoanStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PersonalLoan savedLoan = loanRepository.save(loan);
        log.info("Personal loan created with ID: {}", savedLoan.getId());

        return mapToLoanResponse(savedLoan);
    }

    /**
     * Approve a pending personal loan.
     * T024: Implement approveLoan
     *
     * @param loanId the loan ID to approve
     * @param approvalRemarks approval remarks/conditions
     * @return the approved loan details
     */
    public LoanResponse approveLoan(UUID loanId, String approvalRemarks) {
        log.info("Approving loan: {}", loanId);

        PersonalLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + loanId));

        if (!PersonalLoan.LoanStatus.PENDING.equals(loan.getStatus())) {
            log.warn("Approval attempted on loan with status: {}", loan.getStatus());
            throw new InvalidLoanOperationException("Only pending loans can be approved");
        }

        loan.setStatus(PersonalLoan.LoanStatus.APPROVED);
        loan.setApprovedAt(LocalDateTime.now());
        loan.setApprovalRemarks(approvalRemarks);
        loan.setUpdatedAt(LocalDateTime.now());

        PersonalLoan updatedLoan = loanRepository.save(loan);
        log.info("Loan approved successfully: {}", loanId);

        return mapToLoanResponse(updatedLoan);
    }

    /**
     * Reject a pending personal loan.
     * T024: Implement rejectLoan
     *
     * @param loanId the loan ID to reject
     * @param rejectionReason reason for rejection
     * @return the rejected loan details
     */
    public LoanResponse rejectLoan(UUID loanId, String rejectionReason) {
        log.info("Rejecting loan: {}", loanId);

        PersonalLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + loanId));

        if (!PersonalLoan.LoanStatus.PENDING.equals(loan.getStatus())) {
            log.warn("Rejection attempted on loan with status: {}", loan.getStatus());
            throw new InvalidLoanOperationException("Only pending loans can be rejected");
        }

        loan.setStatus(PersonalLoan.LoanStatus.REJECTED);
        loan.setRejectedAt(LocalDateTime.now());
        loan.setRejectionReason(rejectionReason);
        loan.setUpdatedAt(LocalDateTime.now());

        PersonalLoan updatedLoan = loanRepository.save(loan);
        log.info("Loan rejected successfully: {}", loanId);

        return mapToLoanResponse(updatedLoan);
    }

    /**
     * Disburse an approved loan and generate repayment schedule.
     * T025: Implement disburseLoan (with schedule generation)
     *
     * @param loanId the loan ID to disburse
     * @return the disbursed loan details
     */
    public LoanResponse disburseLoan(UUID loanId) {
        log.info("Disbursing loan: {}", loanId);

        PersonalLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + loanId));

        if (!PersonalLoan.LoanStatus.APPROVED.equals(loan.getStatus())) {
            log.warn("Disbursement attempted on loan with status: {}", loan.getStatus());
            throw new InvalidLoanOperationException("Only approved loans can be disbursed");
        }

        generateRepaymentSchedule(loan);

        loan.setStatus(PersonalLoan.LoanStatus.ACTIVE);
        loan.setUpdatedAt(LocalDateTime.now());

        PersonalLoan updatedLoan = loanRepository.save(loan);
        log.info("Loan disbursed successfully with repayment schedule: {}", loanId);

        return mapToLoanResponse(updatedLoan);
    }

    /**
     * Retrieve loan details by loan ID.
     * T026: Implement getLoan
     *
     * @param loanId the loan ID
     * @return the loan details
     */
    @Transactional(readOnly = true)
    public LoanResponse getLoan(UUID loanId) {
        log.info("Retrieving loan: {}", loanId);
        PersonalLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + loanId));
        return mapToLoanResponse(loan);
    }

    /**
     * Get all loans for a customer.
     * T027: Implement getConsumerLoans
     *
     * @param consumerId the consumer ID
     * @return list of customer's loans
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> getConsumerLoans(UUID consumerId) {
        log.info("Retrieving loans for consumer: {}", consumerId);

        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found: " + consumerId));

        List<PersonalLoan> loans = loanRepository.findByConsumer(consumer);
        log.info("Retrieved {} loans for consumer: {}", loans.size(), consumerId);

        return loans.stream()
                .map(this::mapToLoanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all loans for a customer by customer ID string.
     *
     * @param customerId the customer ID
     * @return list of customer's loans
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByCustomerId(String customerId) {
        log.info("Retrieving all loans for customer: {}", customerId);
        UUID consumerId = UUID.fromString(customerId);
        return loanRepository.findByCustomerId(consumerId).stream()
                .map(this::mapToLoanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active loans for a customer.
     *
     * @param customerId the customer ID
     * @return list of active loans
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> getActiveLoansByCustomerId(String customerId) {
        log.info("Retrieving active loans for customer: {}", customerId);
        UUID consumerId = UUID.fromString(customerId);
        return loanRepository.findByCustomerIdAndStatus(consumerId, PersonalLoan.LoanStatus.ACTIVE).stream()
                .map(this::mapToLoanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Close a loan (mark as CLOSED).
     * T028: Implement closeLoan in LoanRepaymentService but also here for consistency
     *
     * @param loanId the loan ID to close
     * @return the closed loan details
     */
    public LoanResponse closeLoan(UUID loanId) {
        log.info("Closing loan with ID: {}", loanId);
        PersonalLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + loanId));

        if (!loan.getStatus().equals(PersonalLoan.LoanStatus.ACTIVE)) {
            throw new InvalidLoanOperationException("Can only close an ACTIVE loan");
        }

        if (loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new InvalidLoanOperationException("Cannot close loan with outstanding balance");
        }

        loan.setStatus(PersonalLoan.LoanStatus.CLOSED);
        loan.setClosedAt(LocalDateTime.now());
        PersonalLoan updatedLoan = loanRepository.save(loan);

        log.info("Loan with ID: {} has been closed", loanId);
        return mapToLoanResponse(updatedLoan);
    }

    /**
     * Validate loan eligibility constraints.
     * T022: Implement eligibility validation
     */
    private void validateLoanEligibility(Consumer consumer, CreateLoanRequest request) {
        log.debug("Validating loan eligibility for consumer: {}", consumer.getConsumerId());

        if (!Consumer.KYCStatus.VERIFIED.equals(consumer.getKycStatus())) {
            throw new InvalidLoanOperationException("Consumer KYC verification is required");
        }

        if (!Consumer.ConsumerStatus.ACTIVE.equals(consumer.getStatus())) {
            throw new InvalidLoanOperationException("Consumer account is not active");
        }

        PrincipalAccount principalAccount = principalAccountRepository.findByConsumer(consumer)
                .orElseThrow(() -> new InvalidLoanOperationException("Consumer must have a verified principal account"));

        List<PersonalLoan> activeLoans = loanRepository.findByConsumerAndStatus(
                consumer, PersonalLoan.LoanStatus.ACTIVE);
        
        if (!activeLoans.isEmpty()) {
            throw new InvalidLoanOperationException("Consumer cannot have more than one active loan");
        }

        if (request.getLoanTenureMonths() < 12) {
            throw new InvalidLoanOperationException("Loan tenure must be at least 12 months");
        }

        if (request.getLoanTenureMonths() > 360) {
            throw new InvalidLoanOperationException("Loan tenure cannot exceed 360 months");
        }

        log.debug("Loan eligibility validation passed for consumer: {}", consumer.getConsumerId());
    }

    /**
     * Generate repayment schedule for a loan.
     * T025: Implement RepaymentScheduleGenerator for generating monthly records
     */
    private void generateRepaymentSchedule(PersonalLoan loan) {
        log.info("Generating repayment schedule for loan: {}", loan.getId());

        BigDecimal monthlyEMI = loan.getMonthlyEMI();
        BigDecimal principalAmount = loan.getPrincipalAmount();
        BigDecimal annualRate = loan.getAnnualInterestRate();
        Integer months = loan.getLoanTenureMonths();

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = principalAmount;
        LocalDateTime dueDate = LocalDateTime.now().plusMonths(1).withDayOfMonth(1);

        for (int i = 1; i <= months; i++) {
            BigDecimal interestAmount = remainingBalance.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principalForMonth = monthlyEMI.subtract(interestAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            if (i == months) {
                principalForMonth = remainingBalance;
            }

            LoanRepayment repayment = LoanRepayment.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .principalAmount(principalForMonth)
                    .interestAmount(interestAmount)
                    .totalAmount(principalForMonth.add(interestAmount))
                    .status(LoanRepayment.RepaymentStatus.PENDING)
                    .dueDate(dueDate)
                    .createdAt(LocalDateTime.now())
                    .build();

            repaymentRepository.save(repayment);
            remainingBalance = remainingBalance.subtract(principalForMonth);
            dueDate = dueDate.plusMonths(1);
        }

        log.info("Repayment schedule generated with {} installments for loan: {}", months, loan.getId());
    }

    /**
     * Map PersonalLoan entity to LoanResponse DTO.
     */
    private LoanResponse mapToLoanResponse(PersonalLoan loan) {
        return LoanResponse.builder()
                .id(loan.getId().toString())
                .customerId(loan.getConsumer() != null ? loan.getConsumer().getConsumerId().toString() : null)
                .principalAmount(loan.getPrincipalAmount())
                .annualInterestRate(loan.getAnnualInterestRate())
                .loanTenureMonths(loan.getLoanTenureMonths())
                .monthlyEMI(loan.getMonthlyEMI())
                .totalInterestPayable(loan.getTotalInterestPayable())
                .outstandingBalance(loan.getOutstandingBalance())
                .remainingTenure(loan.getRemainingTenure())
                .status(loan.getStatus().toString())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .approvedAt(loan.getApprovedAt())
                .rejectedAt(loan.getRejectedAt())
                .closedAt(loan.getClosedAt())
                .approvalRemarks(loan.getApprovalRemarks())
                .rejectionReason(loan.getRejectionReason())
                .build();
    }

    /**
     * Get all loans for a consumer by consumer ID (alias for getConsumerLoans).
     *
     * @param consumerId the consumer ID
     * @return list of consumer's loans
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByConsumer(UUID consumerId) {
        return getConsumerLoans(consumerId);
    }

    /**
     * Get all pending loans across all consumers.
     *
     * @return list of pending loans
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> getPendingLoans() {
        log.info("Retrieving all pending loans");
        List<PersonalLoan> loans = loanRepository.findByStatus(PersonalLoan.LoanStatus.PENDING);
        return loans.stream()
                .map(this::mapToLoanResponse)
                .collect(Collectors.toList());
    }

}

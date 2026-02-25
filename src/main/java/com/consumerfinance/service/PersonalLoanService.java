package com.consumerfinance.service;

import com.consumerfinance.domain.LoanRepayment;
import com.consumerfinance.domain.PersonalLoan;
import com.consumerfinance.dto.CreateLoanRequest;
import com.consumerfinance.dto.LoanResponse;
import com.consumerfinance.dto.EMICalculationRequest;
import com.consumerfinance.repository.LoanRepaymentRepository;
import com.consumerfinance.repository.PersonalLoanRepository;
import com.consumerfinance.exception.LoanNotFoundException;
import com.consumerfinance.exception.InvalidLoanOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for personal loan management operations.
 * Handles loan creation, retrieval, and status management.
 */
@Slf4j
@Service
@Transactional
public class PersonalLoanService {

    private final PersonalLoanRepository loanRepository;
    private final LoanRepaymentRepository repaymentRepository;
    private final EMICalculationService emiCalculationService;

    public PersonalLoanService(PersonalLoanRepository loanRepository,
                             LoanRepaymentRepository repaymentRepository,
                             EMICalculationService emiCalculationService) {
        this.loanRepository = loanRepository;
        this.repaymentRepository = repaymentRepository;
        this.emiCalculationService = emiCalculationService;
    }

    /**
     * Create a new personal loan for a customer.
     * Generates repayment schedule and sets EMI.
     *
     * @param request the loan creation request
     * @return the created loan details
     */
    public LoanResponse createLoan(CreateLoanRequest request) {
        log.info("Creating new personal loan for customer: {}, amount: {}", 
                request.getCustomerId(), request.getPrincipalAmount());

        // Calculate EMI
        var emiRequest = EMICalculationRequest.builder()
                .principalAmount(request.getPrincipalAmount())
                .annualInterestRate(request.getAnnualInterestRate())
                .tenureMonths(request.getLoanTenureMonths())
                .build();
        
        var emiResponse = emiCalculationService.calculateEMI(emiRequest);

        // Create and save loan
        PersonalLoan loan = PersonalLoan.builder()
                .customerId(request.getCustomerId())
                .principalAmount(request.getPrincipalAmount())
                .annualInterestRate(request.getAnnualInterestRate())
                .loanTenureMonths(request.getLoanTenureMonths())
                .monthlyEMI(emiResponse.getMonthlyEMI())
                .totalInterestPayable(emiResponse.getTotalInterest())
                .outstandingBalance(request.getPrincipalAmount())
                .remainingTenure(request.getLoanTenureMonths())
                .status(PersonalLoan.LoanStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .approvedAt(LocalDateTime.now())
                .build();

        PersonalLoan savedLoan = loanRepository.save(loan);
        log.info("Personal loan created with ID: {}", savedLoan.getId());

        // Generate repayment schedule
        generateRepaymentSchedule(savedLoan);

        return mapToLoanResponse(savedLoan);
    }

    /**
     * Retrieve loan details by loan ID.
     *
     * @param loanId the loan ID
     * @return the loan details
     * @throws LoanNotFoundException if loan not found
     */
    public LoanResponse getLoan(Long loanId) {
        log.info("Retrieving loan with ID: {}", loanId);
        PersonalLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    log.error("Loan not found with ID: {}", loanId);
                    return new LoanNotFoundException("Loan not found with ID: " + loanId);
                });
        return mapToLoanResponse(loan);
    }

    /**
     * Get all loans for a customer.
     *
     * @param customerId the customer ID
     * @return list of customer's loans
     */
    public List<LoanResponse> getLoansByCustomerId(String customerId) {
        log.info("Retrieving all loans for customer: {}", customerId);
        return loanRepository.findByCustomerId(customerId).stream()
                .map(this::mapToLoanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active loans for a customer.
     *
     * @param customerId the customer ID
     * @return list of active loans
     */
    public List<LoanResponse> getActiveLoansByCustomerId(String customerId) {
        log.info("Retrieving active loans for customer: {}", customerId);
        return loanRepository.findByCustomerIdAndStatus(customerId, PersonalLoan.LoanStatus.ACTIVE).stream()
                .map(this::mapToLoanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Close a loan (mark as CLOSED).
     *
     * @param loanId the loan ID to close
     * @return the closed loan details
     * @throws LoanNotFoundException if loan not found
     * @throws InvalidLoanOperationException if loan cannot be closed
     */
    public LoanResponse closeLoan(Long loanId) {
        log.info("Closing loan with ID: {}", loanId);
        PersonalLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));

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
     * Generate repayment schedule for a loan.
     * Creates LoanRepayment entities for each installment.
     *
     * @param loan the personal loan
     */
    private void generateRepaymentSchedule(PersonalLoan loan) {
        log.info("Generating repayment schedule for loan ID: {}", loan.getId());
        
        LocalDateTime dueDate = loan.getApprovedAt().plusMonths(1).withDayOfMonth(1);
        BigDecimal remainingPrincipal = loan.getPrincipalAmount();
        BigDecimal monthlyRate = getMonthlyRate(loan.getAnnualInterestRate());

        for (int i = 1; i <= loan.getLoanTenureMonths(); i++) {
            // Calculate interest for this month
            BigDecimal interestAmount = remainingPrincipal.multiply(monthlyRate)
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            // Principal for this month = EMI - Interest
            BigDecimal principalAmount = loan.getMonthlyEMI().subtract(interestAmount)
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            // For last installment, adjust principal to clear remaining balance
            if (i == loan.getLoanTenureMonths()) {
                principalAmount = remainingPrincipal;
            }

            LoanRepayment repayment = LoanRepayment.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .principalAmount(principalAmount)
                    .interestAmount(interestAmount)
                    .totalAmount(loan.getMonthlyEMI())
                    .status(LoanRepayment.RepaymentStatus.PENDING)
                    .dueDate(dueDate)
                    .createdAt(LocalDateTime.now())
                    .build();

            repaymentRepository.save(repayment);
            remainingPrincipal = remainingPrincipal.subtract(principalAmount);
            dueDate = dueDate.plusMonths(1);
        }

        log.info("Repayment schedule generated with {} installments", loan.getLoanTenureMonths());
    }

    /**
     * Calculate monthly interest rate from annual rate.
     *
     * @param annualRate the annual interest rate as a percentage
     * @return the monthly rate as a decimal
     */
    private BigDecimal getMonthlyRate(BigDecimal annualRate) {
        return annualRate.divide(BigDecimal.valueOf(12).multiply(BigDecimal.valueOf(100)), 10, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Map PersonalLoan entity to LoanResponse DTO.
     *
     * @param loan the personal loan entity
     * @return the loan response DTO
     */
    private LoanResponse mapToLoanResponse(PersonalLoan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .customerId(loan.getCustomerId())
                .principalAmount(loan.getPrincipalAmount())
                .annualInterestRate(loan.getAnnualInterestRate())
                .loanTenureMonths(loan.getLoanTenureMonths())
                .monthlyEMI(loan.getMonthlyEMI())
                .totalInterestPayable(loan.getTotalInterestPayable())
                .outstandingBalance(loan.getOutstandingBalance())
                .remainingTenure(loan.getRemainingTenure())
                .status(loan.getStatus().toString())
                .createdAt(loan.getCreatedAt())
                .approvedAt(loan.getApprovedAt())
                .closedAt(loan.getClosedAt())
                .build();
    }

}

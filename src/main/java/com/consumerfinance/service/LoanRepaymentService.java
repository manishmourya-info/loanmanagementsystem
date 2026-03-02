package com.consumerfinance.service;

import com.consumerfinance.domain.LoanRepayment;
import com.consumerfinance.domain.PersonalLoan;
import com.consumerfinance.dto.RepaymentResponse;
import com.consumerfinance.repository.LoanRepaymentRepository;
import com.consumerfinance.repository.PersonalLoanRepository;
import com.consumerfinance.exception.LoanNotFoundException;
import com.consumerfinance.exception.InvalidRepaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for loan repayment management.
 * Handles payment processing and repayment schedule updates.
 */
@Slf4j
@Service
@Transactional
public class LoanRepaymentService {

    private final LoanRepaymentRepository repaymentRepository;
    private final PersonalLoanRepository loanRepository;

    public LoanRepaymentService(LoanRepaymentRepository repaymentRepository,
                              PersonalLoanRepository loanRepository) {
        this.repaymentRepository = repaymentRepository;
        this.loanRepository = loanRepository;
    }

    /**
     * Process a loan repayment/payment.
     *
     * @param loanId the loan ID
     * @param installmentNumber the installment number to pay
     * @param amountPaid the amount being paid
     * @return the updated repayment details
     * @throws LoanNotFoundException if loan not found
     * @throws InvalidRepaymentException if repayment cannot be processed
     */
    @Transactional
    public RepaymentResponse processRepayment(UUID loanId, Integer installmentNumber, BigDecimal amountPaid) {
        log.info("Processing repayment for Loan ID: {}, Installment: {}, Amount: {}", 
                loanId, installmentNumber, amountPaid);

        // Find loan
        PersonalLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));

        // Find repayment
        LoanRepayment repayment = repaymentRepository.findByLoanAndInstallmentNumber(loan, installmentNumber)
                .orElseThrow(() -> new InvalidRepaymentException(
                        "Repayment not found for Loan ID: " + loanId + ", Installment: " + installmentNumber));

        // Validate repayment can be processed
        validateRepaymentProcessing(repayment, amountPaid);

        // Update repayment status
        if (amountPaid.compareTo(repayment.getTotalAmount()) >= 0) {
            repayment.setStatus(LoanRepayment.RepaymentStatus.PAID);
        } else {
            repayment.setStatus(LoanRepayment.RepaymentStatus.PARTIALLY_PAID);
        }

        repayment.setPaidAmount(amountPaid);
        repayment.setPaidDate(LocalDateTime.now());
        LoanRepayment updatedRepayment = repaymentRepository.save(repayment);

        // Update loan outstanding balance
        loan.setOutstandingBalance(loan.getOutstandingBalance().subtract(amountPaid));
        
        // Update remaining tenure
        long pendingInstallments = repaymentRepository.countByLoanAndStatus(
                loan, LoanRepayment.RepaymentStatus.PENDING);
        loan.setRemainingTenure((int) pendingInstallments);
        
        loanRepository.save(loan);

        log.info("Repayment processed successfully. Outstanding balance: {}", loan.getOutstandingBalance());
        return mapToRepaymentResponse(updatedRepayment);
    }

    /**
     * Get repayment details by loan ID and installment number.
     *
     * @param loanId the loan ID
     * @param installmentNumber the installment number
     * @return the repayment details
     * @throws InvalidRepaymentException if repayment not found
     */
    public RepaymentResponse getRepayment(UUID loanId, Integer installmentNumber) {
        log.info("Retrieving repayment for Loan ID: {}, Installment: {}", loanId, installmentNumber);
        
        PersonalLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));
        
        LoanRepayment repayment = repaymentRepository.findByLoanAndInstallmentNumber(loan, installmentNumber)
                .orElseThrow(() -> new InvalidRepaymentException(
                        "Repayment not found for Loan ID: " + loanId + ", Installment: " + installmentNumber));
        return mapToRepaymentResponse(repayment);
    }

    /**
     * Get all repayments for a loan.
     *
     * @param loanId the loan ID
     * @return list of repayments for the loan
     * @throws LoanNotFoundException if loan not found
     */
    public List<RepaymentResponse> getRepaymentsByLoanId(UUID loanId) {
        log.info("Retrieving all repayments for Loan ID: {}", loanId);
        
        // Verify loan exists
        PersonalLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));

        return repaymentRepository.findByLoan(loan).stream()
                .map(this::mapToRepaymentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get pending repayments for a loan.
     *
     * @param loanId the loan ID
     * @return list of pending repayments
     */
    public List<RepaymentResponse> getPendingRepaymentsByLoanId(UUID loanId) {
        log.info("Retrieving pending repayments for Loan ID: {}", loanId);
        
        PersonalLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));
        
        return repaymentRepository.findByLoan(loan).stream()
                .filter(r -> r.getStatus().equals(LoanRepayment.RepaymentStatus.PENDING))
                .map(this::mapToRepaymentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get overdue repayments across all loans.
     *
     * @return list of overdue repayments
     */
    public List<RepaymentResponse> getOverdueRepayments() {
        log.info("Retrieving all overdue repayments");
        return repaymentRepository.findOverdueRepayments().stream()
                .map(this::mapToRepaymentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate that a repayment can be processed.
     *
     * @param repayment the repayment entity
     * @param amountPaid the amount being paid
     * @throws InvalidRepaymentException if repayment validation fails
     */
    private void validateRepaymentProcessing(LoanRepayment repayment, BigDecimal amountPaid) {
        if (amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRepaymentException("Payment amount must be greater than zero");
        }

        if (amountPaid.compareTo(repayment.getTotalAmount()) > 0) {
            log.warn("Payment amount {} exceeds due amount {}", amountPaid, repayment.getTotalAmount());
        }

        if (repayment.getStatus().equals(LoanRepayment.RepaymentStatus.PAID)) {
            throw new InvalidRepaymentException("Installment already paid");
        }
    }

    /**
     * Map LoanRepayment entity to RepaymentResponse DTO.
     *
     * @param repayment the loan repayment entity
     * @return the repayment response DTO
     */
    private RepaymentResponse mapToRepaymentResponse(LoanRepayment repayment) {
        return RepaymentResponse.builder()
                .id(repayment.getId().toString())
                .loanId(repayment.getLoan().getId().toString())
                .installmentNumber(repayment.getInstallmentNumber())
                .principalAmount(repayment.getPrincipalAmount())
                .interestAmount(repayment.getInterestAmount())
                .totalAmount(repayment.getTotalAmount())
                .paidAmount(repayment.getPaidAmount())
                .status(repayment.getStatus().toString())
                .dueDate(repayment.getDueDate())
                .paidDate(repayment.getPaidDate())
                .paymentMode(repayment.getPaymentMode())
                .transactionReference(repayment.getTransactionReference())
                .build();
    }

}

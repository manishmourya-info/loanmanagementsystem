package com.consumerfinance.repository;

import com.consumerfinance.domain.LoanRepayment;
import com.consumerfinance.domain.PersonalLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LoanRepayment entity.
 * Provides database access operations for loan repayments.
 */
@Repository
public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {

    /**
     * Find all repayments for a specific loan.
     */
    List<LoanRepayment> findByLoan(PersonalLoan loan);

    /**
     * Find repayment by loan and installment number.
     */
    Optional<LoanRepayment> findByLoanAndInstallmentNumber(PersonalLoan loan, Integer installmentNumber);

    /**
     * Find by loan ID (UUID) - using custom query
     */
    @Query("SELECT r FROM LoanRepayment r WHERE r.loan.id = :loanId")
    List<LoanRepayment> findByLoanId(java.util.UUID loanId);

    /**
     * Find repayment by loan ID (UUID) and installment number
     */
    @Query("SELECT r FROM LoanRepayment r WHERE r.loan.id = :loanId AND r.installmentNumber = :installmentNumber")
    Optional<LoanRepayment> findByLoanIdAndInstallmentNumber(java.util.UUID loanId, Integer installmentNumber);

    /**
     * Find all overdue repayments.
     */
    @Query("SELECT r FROM LoanRepayment r WHERE r.status = 'OVERDUE'")
    List<LoanRepayment> findOverdueRepayments();

    /**
     * Count pending repayments for a loan.
     */
    long countByLoanAndStatus(PersonalLoan loan, LoanRepayment.RepaymentStatus status);

    /**
     * Count pending repayments for a loan by ID (UUID)
     */
    @Query("SELECT COUNT(r) FROM LoanRepayment r WHERE r.loan.id = :loanId AND r.status = :status")
    long countByLoanIdAndStatus(java.util.UUID loanId, LoanRepayment.RepaymentStatus status);
}

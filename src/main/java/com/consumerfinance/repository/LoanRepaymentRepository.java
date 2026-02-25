package com.consumerfinance.repository;

import com.consumerfinance.domain.LoanRepayment;
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
     * @param loanId the loan ID
     * @return list of repayments for the loan
     */
    List<LoanRepayment> findByLoanId(Long loanId);

    /**
     * Find repayment by loan ID and installment number.
     * @param loanId the loan ID
     * @param installmentNumber the installment number
     * @return optional containing the repayment if found
     */
    Optional<LoanRepayment> findByLoanIdAndInstallmentNumber(Long loanId, Integer installmentNumber);

    /**
     * Find all overdue repayments.
     * @return list of overdue repayments
     */
    @Query("SELECT r FROM LoanRepayment r WHERE r.status = 'OVERDUE'")
    List<LoanRepayment> findOverdueRepayments();

    /**
     * Count pending repayments for a loan.
     * @param loanId the loan ID
     * @return count of pending repayments
     */
    long countByLoanIdAndStatus(Long loanId, LoanRepayment.RepaymentStatus status);

}

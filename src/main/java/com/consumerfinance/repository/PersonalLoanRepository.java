package com.consumerfinance.repository;

import com.consumerfinance.domain.PersonalLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PersonalLoan entity.
 * Provides database access operations for loans.
 */
@Repository
public interface PersonalLoanRepository extends JpaRepository<PersonalLoan, Long> {

    /**
     * Find all loans for a specific customer.
     * @param customerId the customer ID
     * @return list of loans for the customer
     */
    List<PersonalLoan> findByCustomerId(String customerId);

    /**
     * Find active loans for a customer.
     * @param customerId the customer ID
     * @param status the loan status
     * @return list of loans with specified status
     */
    List<PersonalLoan> findByCustomerIdAndStatus(String customerId, PersonalLoan.LoanStatus status);

}

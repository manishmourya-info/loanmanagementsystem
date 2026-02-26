package com.consumerfinance.repository;

import com.consumerfinance.domain.PersonalLoan;
import com.consumerfinance.domain.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PersonalLoan entity.
 * Provides database access operations for loans.
 */
@Repository
public interface PersonalLoanRepository extends JpaRepository<PersonalLoan, UUID> {

    /**
     * Find all loans for a specific customer by consumer ID (UUID).
     * @param consumerId the consumer ID
     * @return list of loans for the consumer
     */
    @Query("SELECT p FROM PersonalLoan p WHERE p.consumer.consumerId = :consumerId")
    List<PersonalLoan> findByCustomerId(UUID consumerId);

    /**
     * Find active loans for a consumer with specific status.
     * @param consumerId the consumer ID
     * @param status the loan status
     * @return list of loans with specified status
     */
    @Query("SELECT p FROM PersonalLoan p WHERE p.consumer.consumerId = :consumerId AND p.status = :status")
    List<PersonalLoan> findByCustomerIdAndStatus(UUID consumerId, PersonalLoan.LoanStatus status);

    /**
     * Find all loans for a specific consumer.
     * @param consumer the consumer entity
     * @return list of loans for the consumer
     */
    List<PersonalLoan> findByConsumer(Consumer consumer);

    /**
     * Find loans for a consumer with specific status.
     * @param consumer the consumer entity
     * @param status the loan status
     * @return list of loans with specified status
     */
    List<PersonalLoan> findByConsumerAndStatus(Consumer consumer, PersonalLoan.LoanStatus status);

    /**
     * Find all loans with a specific status across all consumers.
     * @param status the loan status
     * @return list of loans with specified status
     */
    List<PersonalLoan> findByStatus(PersonalLoan.LoanStatus status);

}


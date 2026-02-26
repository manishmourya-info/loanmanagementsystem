package com.consumerfinance.repository;

import com.consumerfinance.domain.PrincipalAccount;
import com.consumerfinance.domain.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PrincipalAccount entity
 * T016: Principal Account repository
 */
@Repository
public interface PrincipalAccountRepository extends JpaRepository<PrincipalAccount, UUID> {

    /**
     * Find principal account by consumer (one-to-one relationship)
     */
    Optional<PrincipalAccount> findByConsumer(Consumer consumer);

    /**
     * Find accounts by verification status
     */
    long countByVerificationStatus(PrincipalAccount.VerificationStatus status);
}

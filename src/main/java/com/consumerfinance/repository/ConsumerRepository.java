package com.consumerfinance.repository;

import com.consumerfinance.domain.Consumer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Consumer entity
 * T015: Consumer repository with custom queries
 */
@Repository
public interface ConsumerRepository extends JpaRepository<Consumer, UUID> {

    /**
     * Find consumer by email
     */
    Optional<Consumer> findByEmail(String email);

    /**
     * Find consumer by phone
     */
    Optional<Consumer> findByPhone(String phone);

    /**
     * Find consumers by status
     */
    Page<Consumer> findByStatus(Consumer.ConsumerStatus status, Pageable pageable);

    /**
     * Find consumers by name, email, or phone containing (for search)
     */
    Page<Consumer> findByNameContainingOrEmailContainingOrPhoneContaining(String name, String email, String phone, Pageable pageable);

    /**
     * Find consumers by status and search
     */
    Page<Consumer> findByStatusAndNameContainingOrStatusAndEmailContainingOrStatusAndPhoneContaining(
            Consumer.ConsumerStatus status1, String name,
            Consumer.ConsumerStatus status2, String email,
            Consumer.ConsumerStatus status3, String phone,
            Pageable pageable);

    /**
     * Count active consumers
     */
    long countByStatus(Consumer.ConsumerStatus status);
}

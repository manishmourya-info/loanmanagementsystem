package com.consumerfinance.repository;

import com.consumerfinance.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for AuditLog entity
 * T012: Audit log repository
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Find audit logs by loanId
     */
    List<AuditLog> findByLoanId(String loanId);

    /**
     * Find audit logs by action
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * Find recent audit logs
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * Find audit logs by loanId and action
     */
    List<AuditLog> findByLoanIdAndAction(String loanId, String action);
}

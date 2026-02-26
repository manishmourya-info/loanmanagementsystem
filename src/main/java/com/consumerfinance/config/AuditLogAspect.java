package com.consumerfinance.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Audit logging aspect for tracking consumer operations
 * T020: Audit logging via AOP
 */
@Aspect
@Component
@Slf4j
public class AuditLogAspect {

    /**
     * Custom annotation for marking methods that should be audited
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Auditable {
        String action();
    }

    /**
     * Log method execution after success
     */
    @After("@annotation(auditable)")
    public void auditMethodExecution(JoinPoint joinPoint, Auditable auditable) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String action = auditable.action();
            log.info("AUDIT: Action={}, Method={}, Args={}", action, methodName, joinPoint.getArgs());
        } catch (Exception e) {
            log.error("Error in audit logging", e);
        }
    }
}

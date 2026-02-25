package com.consumerfinance.service;

import com.consumerfinance.domain.LoanRepayment;
import com.consumerfinance.domain.PersonalLoan;
import com.consumerfinance.dto.CreateLoanRequest;
import com.consumerfinance.dto.LoanResponse;
import com.consumerfinance.exception.LoanNotFoundException;
import com.consumerfinance.exception.InvalidLoanOperationException;
import com.consumerfinance.repository.LoanRepaymentRepository;
import com.consumerfinance.repository.PersonalLoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PersonalLoanService.
 * Tests loan creation, retrieval, and lifecycle management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Personal Loan Service Tests")
class PersonalLoanServiceTest {

    @Mock
    private PersonalLoanRepository loanRepository;

    @Mock
    private LoanRepaymentRepository repaymentRepository;

    @Mock
    private EMICalculationService emiCalculationService;

    private PersonalLoanService loanService;
    private CreateLoanRequest createLoanRequest;
    private PersonalLoan mockLoan;

    @BeforeEach
    void setUp() {
        loanService = new PersonalLoanService(loanRepository, repaymentRepository, emiCalculationService);
        
        createLoanRequest = CreateLoanRequest.builder()
                .customerId("CUST123456")
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .loanTenureMonths(60)
                .build();

        mockLoan = PersonalLoan.builder()
                .id(1L)
                .customerId("CUST123456")
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .loanTenureMonths(60)
                .monthlyEMI(BigDecimal.valueOf(9638.22))
                .totalInterestPayable(BigDecimal.valueOf(78293.20))
                .outstandingBalance(BigDecimal.valueOf(500000))
                .remainingTenure(60)
                .status(PersonalLoan.LoanStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .approvedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create a new personal loan successfully")
    void testCreateLoanSuccessfully() {
        // Arrange
        when(emiCalculationService.calculateEMI(any())).thenReturn(
            new com.consumerfinance.dto.EMICalculationResponse(
                BigDecimal.valueOf(9638.22),
                BigDecimal.valueOf(578293.20),
                BigDecimal.valueOf(78293.20),
                BigDecimal.valueOf(500000),
                BigDecimal.valueOf(10.5),
                60
            )
        );
        when(loanRepository.save(any(PersonalLoan.class))).thenReturn(mockLoan);

        // Act
        LoanResponse response = loanService.createLoan(createLoanRequest);

        // Assert
        assertNotNull(response);
        assertEquals("CUST123456", response.getCustomerId());
        assertEquals(BigDecimal.valueOf(500000), response.getPrincipalAmount());
        assertEquals(BigDecimal.valueOf(9638.22), response.getMonthlyEMI());
        
        verify(loanRepository, times(1)).save(any(PersonalLoan.class));
    }

    @Test
    @DisplayName("Should retrieve loan by ID")
    void testGetLoanById() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(mockLoan));

        // Act
        LoanResponse response = loanService.getLoan(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("CUST123456", response.getCustomerId());
        
        verify(loanRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when loan not found")
    void testGetLoanNotFound() {
        // Arrange
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LoanNotFoundException.class, () -> loanService.getLoan(999L));
        verify(loanRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should close an active loan with zero balance")
    void testCloseLoanSuccessfully() {
        // Arrange
        PersonalLoan activeLoan = mockLoan.toBuilder()
                .outstandingBalance(BigDecimal.ZERO)
                .status(PersonalLoan.LoanStatus.ACTIVE)
                .build();
        
        PersonalLoan closedLoan = activeLoan.toBuilder()
                .status(PersonalLoan.LoanStatus.CLOSED)
                .closedAt(LocalDateTime.now())
                .build();

        when(loanRepository.findById(1L)).thenReturn(Optional.of(activeLoan));
        when(loanRepository.save(any(PersonalLoan.class))).thenReturn(closedLoan);

        // Act
        LoanResponse response = loanService.closeLoan(1L);

        // Assert
        assertNotNull(response);
        assertEquals("CLOSED", response.getStatus());
        assertNotNull(response.getClosedAt());
        
        verify(loanRepository, times(1)).findById(1L);
        verify(loanRepository, times(1)).save(any(PersonalLoan.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to close loan with outstanding balance")
    void testCloseLoanWithOutstandingBalance() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(mockLoan));

        // Act & Assert
        assertThrows(InvalidLoanOperationException.class, () -> loanService.closeLoan(1L));
    }

    @Test
    @DisplayName("Should throw exception when trying to close non-active loan")
    void testCloseLoanWithInactiveStatus() {
        // Arrange
        PersonalLoan closedLoan = mockLoan.toBuilder()
                .status(PersonalLoan.LoanStatus.CLOSED)
                .build();
        
        when(loanRepository.findById(1L)).thenReturn(Optional.of(closedLoan));

        // Act & Assert
        assertThrows(InvalidLoanOperationException.class, () -> loanService.closeLoan(1L));
    }

}

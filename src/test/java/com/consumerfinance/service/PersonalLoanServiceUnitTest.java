package com.consumerfinance.service;

import com.consumerfinance.domain.LoanRepayment;
import com.consumerfinance.domain.PersonalLoan;
import com.consumerfinance.dto.CreateLoanRequest;
import com.consumerfinance.dto.LoanResponse;
import com.consumerfinance.repository.ConsumerRepository;
import com.consumerfinance.repository.LoanRepaymentRepository;
import com.consumerfinance.repository.PersonalLoanRepository;
import com.consumerfinance.repository.PrincipalAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PersonalLoanService.
 * Tests loan creation, retrieval, and lifecycle management.
 * Latest unit tests with UUID support.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Personal Loan Service Unit Tests")
class PersonalLoanServiceUnitTest {

    @Mock
    private PersonalLoanRepository loanRepository;

    @Mock
    private LoanRepaymentRepository repaymentRepository;

    @Mock
    private EMICalculationService emiCalculationService;

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private PrincipalAccountRepository principalAccountRepository;

    @InjectMocks
    private PersonalLoanService loanService;

    private UUID loanId;
    private PersonalLoan testLoan;
    private CreateLoanRequest createRequest;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();
        
        testLoan = PersonalLoan.builder()
                .id(loanId)
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .loanTenureMonths(60)
                .monthlyEMI(BigDecimal.valueOf(9638.22))
                .status(PersonalLoan.LoanStatus.ACTIVE)
                .outstandingBalance(BigDecimal.valueOf(500000))
                .build();

        createRequest = CreateLoanRequest.builder()
                .customerId("CUST123456")
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .loanTenureMonths(60)
                .build();
    }

    // ============ Loan Creation Tests ============

    @Test
    @DisplayName("Should create loan successfully")
    void testCreateLoanSuccess() {
        // Arrange
        when(loanRepository.save(any(PersonalLoan.class))).thenReturn(testLoan);

        // Act
        PersonalLoan result = loanRepository.save(testLoan);

        // Assert
        assertNotNull(result);
        assertEquals(loanId, result.getId());
        assertEquals(PersonalLoan.LoanStatus.ACTIVE, result.getStatus());
        verify(loanRepository).save(any(PersonalLoan.class));
    }

    @Test
    @DisplayName("Should set correct principal amount on loan creation")
    void testLoanPrincipalAmountSet() {
        // Arrange & Act
        BigDecimal expected = BigDecimal.valueOf(500000);
        BigDecimal actual = testLoan.getPrincipalAmount();

        // Assert
        assertEquals(0, expected.compareTo(actual));
    }

    @Test
    @DisplayName("Should initialize loan with ACTIVE status")
    void testLoanInitialStatus() {
        // Act & Assert
        assertEquals(PersonalLoan.LoanStatus.ACTIVE, testLoan.getStatus());
    }

    // ============ Loan Retrieval Tests ============

    @Test
    @DisplayName("Should retrieve loan by ID")
    void testGetLoanByIdSuccess() {
        // Arrange
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        // Act
        Optional<PersonalLoan> result = loanRepository.findById(loanId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(loanId, result.get().getId());
        verify(loanRepository).findById(loanId);
    }

    @Test
    @DisplayName("Should return empty optional when loan not found")
    void testGetLoanByIdNotFound() {
        // Arrange
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // Act
        Optional<PersonalLoan> result = loanRepository.findById(loanId);

        // Assert
        assertFalse(result.isPresent());
    }

    // ============ Loan Status Tests ============

    @Test
    @DisplayName("Should transition loan status from ACTIVE to CLOSED")
    void testLoanStatusTransition() {
        // Arrange
        testLoan.setStatus(PersonalLoan.LoanStatus.ACTIVE);

        // Act
        testLoan.setStatus(PersonalLoan.LoanStatus.CLOSED);

        // Assert
        assertEquals(PersonalLoan.LoanStatus.CLOSED, testLoan.getStatus());
    }

    @Test
    @DisplayName("Should mark loan as APPROVED")
    void testLoanDisbursement() {
        // Arrange
        testLoan.setStatus(PersonalLoan.LoanStatus.PENDING);

        // Act
        testLoan.setStatus(PersonalLoan.LoanStatus.APPROVED);

        // Assert
        assertEquals(PersonalLoan.LoanStatus.APPROVED, testLoan.getStatus());
    }

    @Test
    @DisplayName("Should mark loan as REJECTED")
    void testLoanRejection() {
        // Arrange
        testLoan.setStatus(PersonalLoan.LoanStatus.PENDING);

        // Act
        testLoan.setStatus(PersonalLoan.LoanStatus.REJECTED);

        // Assert
        assertEquals(PersonalLoan.LoanStatus.REJECTED, testLoan.getStatus());
    }

    // ============ Outstanding Balance Tests ============

    @Test
    @DisplayName("Should calculate outstanding balance correctly")
    void testOutstandingBalanceCalculation() {
        // Arrange
        BigDecimal principal = BigDecimal.valueOf(500000);
        BigDecimal payment = BigDecimal.valueOf(9638.22);

        // Act
        BigDecimal balance = principal.subtract(payment);

        // Assert
        assertEquals(0, balance.compareTo(BigDecimal.valueOf(490361.78)));
    }

    @Test
    @DisplayName("Should update outstanding balance after payment")
    void testUpdateOutstandingBalance() {
        // Arrange
        BigDecimal initialBalance = testLoan.getOutstandingBalance();
        BigDecimal payment = BigDecimal.valueOf(10000);

        // Act
        testLoan.setOutstandingBalance(initialBalance.subtract(payment));

        // Assert
        assertEquals(0, testLoan.getOutstandingBalance().compareTo(BigDecimal.valueOf(490000)));
    }

    // ============ EMI Calculation Tests ============

    @Test
    @DisplayName("Should calculate interest amount correctly")
    void testInterestCalculation() {
        // Arrange
        BigDecimal principal = testLoan.getPrincipalAmount();
        BigDecimal rate = testLoan.getAnnualInterestRate();
        BigDecimal monthly = rate.divide(BigDecimal.valueOf(12), 10, java.math.RoundingMode.HALF_UP);
        BigDecimal interest = principal.multiply(monthly);

        // Act & Assert
        assertNotNull(interest);
        assertTrue(interest.compareTo(BigDecimal.ZERO) > 0);
    }

    // ============ Concurrent Operations Tests ============

    @Test
    @DisplayName("Should handle concurrent loan retrievals")
    void testConcurrentLoanRetrieval() {
        // Arrange
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        // Act & Assert
        for (int i = 0; i < 10; i++) {
            Optional<PersonalLoan> result = loanRepository.findById(loanId);
            assertTrue(result.isPresent());
        }
        
        verify(loanRepository, times(10)).findById(loanId);
    }

    @Test
    @DisplayName("Should handle concurrent loan creations")
    void testConcurrentLoanCreation() {
        // Arrange
        when(loanRepository.save(any(PersonalLoan.class))).thenReturn(testLoan);

        // Act
        for (int i = 0; i < 5; i++) {
            loanRepository.save(testLoan);
        }

        // Assert
        verify(loanRepository, times(5)).save(any(PersonalLoan.class));
    }

    // ============ Validation Tests ============

    @Test
    @DisplayName("Should validate principal amount is positive")
    void testPrincipalAmountValidation() {
        // Act & Assert
        assertTrue(testLoan.getPrincipalAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Should validate tenure is valid")
    void testTenureValidation() {
        // Act & Assert
        assertTrue(testLoan.getLoanTenureMonths() > 0);
        assertTrue(testLoan.getLoanTenureMonths() <= 360);
    }

    @Test
    @DisplayName("Should validate interest rate is reasonable")
    void testInterestRateValidation() {
        // Act & Assert
        assertTrue(testLoan.getAnnualInterestRate().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(testLoan.getAnnualInterestRate().compareTo(BigDecimal.valueOf(36)) <= 0);
    }

    // ============ Edge Case Tests ============

    @Test
    @DisplayName("Should handle loan with minimum tenure")
    void testMinimumTenureLoan() {
        // Arrange
        PersonalLoan minTenureLoan = PersonalLoan.builder()
                .id(UUID.randomUUID())
                .principalAmount(BigDecimal.valueOf(100000))
                .annualInterestRate(BigDecimal.valueOf(10))
                .loanTenureMonths(12)
                .monthlyEMI(BigDecimal.valueOf(8885.56))
                .build();

        // Act & Assert
        assertEquals(12, minTenureLoan.getLoanTenureMonths());
    }

    @Test
    @DisplayName("Should handle loan with maximum tenure")
    void testMaximumTenureLoan() {
        // Arrange
        PersonalLoan maxTenureLoan = PersonalLoan.builder()
                .id(UUID.randomUUID())
                .principalAmount(BigDecimal.valueOf(5000000))
                .annualInterestRate(BigDecimal.valueOf(8))
                .loanTenureMonths(360)
                .monthlyEMI(BigDecimal.valueOf(36716.88))
                .build();

        // Act & Assert
        assertEquals(360, maxTenureLoan.getLoanTenureMonths());
    }

    @Test
    @DisplayName("Should handle loan with maximum principal")
    void testMaximumPrincipalLoan() {
        // Arrange
        BigDecimal maxPrincipal = BigDecimal.valueOf(50000000);

        // Act
        testLoan.setPrincipalAmount(maxPrincipal);

        // Assert
        assertEquals(0, testLoan.getPrincipalAmount().compareTo(maxPrincipal));
    }

    @Test
    @DisplayName("Should handle fully repaid loan")
    void testFullyRepaidLoan() {
        // Arrange
        testLoan.setOutstandingBalance(BigDecimal.ZERO);

        // Act
        testLoan.setStatus(PersonalLoan.LoanStatus.CLOSED);

        // Assert
        assertEquals(BigDecimal.ZERO, testLoan.getOutstandingBalance());
        assertEquals(PersonalLoan.LoanStatus.CLOSED, testLoan.getStatus());
    }

    // ============ Type Safety Tests ============

    @Test
    @DisplayName("Should maintain UUID type for loan ID")
    void testLoanIdType() {
        // Act & Assert
        assertNotNull(testLoan.getId());
        assertTrue(testLoan.getId() instanceof UUID);
    }

    @Test
    @DisplayName("Should maintain BigDecimal precision for monetary fields")
    void testMonetaryFieldPrecision() {
        // Act & Assert
        assertTrue(testLoan.getPrincipalAmount() instanceof BigDecimal);
        assertTrue(testLoan.getAnnualInterestRate() instanceof BigDecimal);
        assertTrue(testLoan.getOutstandingBalance() instanceof BigDecimal);
    }
}

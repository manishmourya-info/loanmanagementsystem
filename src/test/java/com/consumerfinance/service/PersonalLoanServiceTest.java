package com.consumerfinance.service;

import com.consumerfinance.domain.Consumer;
import com.consumerfinance.domain.PersonalLoan;
import com.consumerfinance.domain.LoanRepayment;
import com.consumerfinance.domain.PrincipalAccount;
import com.consumerfinance.dto.CreateLoanRequest;
import com.consumerfinance.dto.LoanResponse;
import com.consumerfinance.dto.EMICalculationRequest;
import com.consumerfinance.dto.EMICalculationResponse;
import com.consumerfinance.exception.ConsumerNotFoundException;
import com.consumerfinance.exception.InvalidRepaymentException;
import com.consumerfinance.exception.InvalidLoanOperationException;
import com.consumerfinance.repository.PersonalLoanRepository;
import com.consumerfinance.repository.ConsumerRepository;
import com.consumerfinance.repository.LoanRepaymentRepository;
import com.consumerfinance.repository.PrincipalAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PersonalLoanService.
 * Tests loan creation, approval, rejection, and disbursement logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Personal Loan Service Tests")
class PersonalLoanServiceTest {

    @Mock
    private PersonalLoanRepository loanRepository;

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private LoanRepaymentRepository repaymentRepository;

    @Mock
    private EMICalculationService emiCalculationService;

    @Mock
    private PrincipalAccountRepository principalAccountRepository;

    @InjectMocks
    private PersonalLoanService personalLoanService;

    private UUID consumerId;
    private UUID loanId;
    private Consumer mockConsumer;
    private CreateLoanRequest loanRequest;

    @BeforeEach
    void setUp() {
        consumerId = UUID.randomUUID();
        loanId = UUID.randomUUID();

        mockConsumer = Consumer.builder()
                .consumerId(consumerId)
                .name("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(Consumer.ConsumerStatus.ACTIVE)
                .kycStatus(Consumer.KYCStatus.VERIFIED)
                .createdAt(LocalDateTime.now())
                .build();

        loanRequest = CreateLoanRequest.builder()
                .customerId(consumerId.toString())
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .loanTenureMonths(60)
                .build();
    }

    @Test
    @DisplayName("Should create loan successfully for eligible consumer")
    void testCreateLoan_Success() {
        // Arrange
        PrincipalAccount account = PrincipalAccount.builder()
                .consumer(mockConsumer)
                .verificationStatus(PrincipalAccount.VerificationStatus.VERIFIED)
                .build();

        EMICalculationResponse emiResponse = EMICalculationResponse.builder()
                .monthlyEMI(BigDecimal.valueOf(10746.95))
                .totalInterest(BigDecimal.valueOf(144817.00))
                .totalAmount(BigDecimal.valueOf(644817.00))
                .build();

        PersonalLoan expectedLoan = PersonalLoan.builder()
                .id(loanId)
                .consumer(mockConsumer)
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .monthlyEMI(BigDecimal.valueOf(10746.95))
                .status(PersonalLoan.LoanStatus.PENDING)
                .build();

        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));
        when(principalAccountRepository.findByConsumer(mockConsumer)).thenReturn(Optional.of(account));
        when(emiCalculationService.calculateEMI(any(EMICalculationRequest.class))).thenReturn(emiResponse);
        when(loanRepository.save(any(PersonalLoan.class))).thenReturn(expectedLoan);

        // Act
        LoanResponse response = personalLoanService.createLoan(loanRequest);

        // Assert
        assertNotNull(response);
        assertEquals(consumerId.toString(), response.getCustomerId());
        assertEquals(BigDecimal.valueOf(500000), response.getPrincipalAmount());
        verify(loanRepository, times(1)).save(any(PersonalLoan.class));
    }

    @Test
    @DisplayName("Should throw exception for non-existent consumer")
    void testCreateLoan_ConsumerNotFound() {
        // Arrange
        when(consumerRepository.findById(any(UUID.class)))
                .thenThrow(new ConsumerNotFoundException("Consumer not found"));

        // Act & Assert
        assertThrows(ConsumerNotFoundException.class, () -> personalLoanService.createLoan(loanRequest));
        verify(loanRepository, never()).save(any(PersonalLoan.class));
    }

    @Test
    @DisplayName("Should throw exception for consumer without verified account")
    void testCreateLoan_NoVerifiedAccount() {
        // Arrange
        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));
        when(principalAccountRepository.findByConsumer(mockConsumer)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidLoanOperationException.class, () -> personalLoanService.createLoan(loanRequest));
        verify(loanRepository, never()).save(any(PersonalLoan.class));
    }

    @Test
    @DisplayName("Should approve a pending loan successfully")
    void testApproveLoan_Success() {
        // Arrange
        PersonalLoan loan = PersonalLoan.builder()
                .id(loanId)
                .consumer(mockConsumer)
                .status(PersonalLoan.LoanStatus.PENDING)
                .build();

        PersonalLoan approvedLoan = PersonalLoan.builder()
                .id(loanId)
                .consumer(mockConsumer)
                .status(PersonalLoan.LoanStatus.APPROVED)
                .build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(PersonalLoan.class))).thenReturn(approvedLoan);

        // Act
        LoanResponse response = personalLoanService.approveLoan(loanId, "Approved by manager");

        // Assert
        assertNotNull(response);
        assertEquals("APPROVED", response.getStatus());
        verify(loanRepository, times(1)).save(any(PersonalLoan.class));
    }

    @Test
    @DisplayName("Should reject a pending loan successfully")
    void testRejectLoan_Success() {
        // Arrange
        PersonalLoan loan = PersonalLoan.builder()
                .id(loanId)
                .consumer(mockConsumer)
                .status(PersonalLoan.LoanStatus.PENDING)
                .build();

        PersonalLoan rejectedLoan = PersonalLoan.builder()
                .id(loanId)
                .consumer(mockConsumer)
                .status(PersonalLoan.LoanStatus.REJECTED)
                .build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(PersonalLoan.class))).thenReturn(rejectedLoan);

        // Act
        LoanResponse response = personalLoanService.rejectLoan(loanId, "Does not meet criteria");

        // Assert
        assertNotNull(response);
        assertEquals("REJECTED", response.getStatus());
        verify(loanRepository, times(1)).save(any(PersonalLoan.class));
    }

    @Test
    @DisplayName("Should disburse an approved loan successfully")
    void testDisburseLoan_Success() {
        // Arrange
        PersonalLoan loan = PersonalLoan.builder()
                .id(loanId)
                .consumer(mockConsumer)
                .status(PersonalLoan.LoanStatus.APPROVED)
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .loanTenureMonths(60)
                .monthlyEMI(BigDecimal.valueOf(9638.22))
                .totalInterestPayable(BigDecimal.valueOf(78293.2))
                .outstandingBalance(BigDecimal.valueOf(500000))
                .remainingTenure(60)
                .createdAt(LocalDateTime.now())
                .build();

        PersonalLoan disbursedLoan = PersonalLoan.builder()
                .id(loanId)
                .consumer(mockConsumer)
                .status(PersonalLoan.LoanStatus.ACTIVE)
                .principalAmount(BigDecimal.valueOf(500000))
                .annualInterestRate(BigDecimal.valueOf(10.5))
                .loanTenureMonths(60)
                .monthlyEMI(BigDecimal.valueOf(9638.22))
                .totalInterestPayable(BigDecimal.valueOf(78293.2))
                .outstandingBalance(BigDecimal.valueOf(500000))
                .remainingTenure(60)
                .updatedAt(LocalDateTime.now())
                .build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(PersonalLoan.class))).thenReturn(disbursedLoan);

        // Act
        LoanResponse response = personalLoanService.disburseLoan(loanId);

        // Assert
        assertNotNull(response);
        assertEquals("ACTIVE", response.getStatus());
        verify(loanRepository, times(1)).save(any(PersonalLoan.class));
    }

    @Test
    @DisplayName("Should get loan by ID successfully")
    void testGetLoan_Success() {
        // Arrange
        PersonalLoan loan = PersonalLoan.builder()
                .id(loanId)
                .consumer(mockConsumer)
                .status(PersonalLoan.LoanStatus.APPROVED)
                .build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        // Act
        LoanResponse response = personalLoanService.getLoan(loanId);

        // Assert
        assertNotNull(response);
        assertEquals(loanId.toString(), response.getId());
        verify(loanRepository, times(1)).findById(loanId);
    }

    @Test
    @DisplayName("Should throw exception when loan not found")
    void testGetLoan_NotFound() {
        // Arrange
        when(loanRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> personalLoanService.getLoan(loanId));
    }

    @Test
    @DisplayName("Should get all loans for consumer")
    void testGetLoansByConsumer_Success() {
        // Arrange
        PersonalLoan loan = PersonalLoan.builder()
                .id(loanId)
                .consumer(mockConsumer)
                .status(PersonalLoan.LoanStatus.APPROVED)
                .build();

        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(mockConsumer));
        when(loanRepository.findByConsumer(mockConsumer)).thenReturn(Arrays.asList(loan));

        // Act
        List<LoanResponse> responses = personalLoanService.getLoansByConsumer(consumerId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(loanRepository, times(1)).findByConsumer(mockConsumer);
    }

    @Test
    @DisplayName("Should get pending loans")
    void testGetPendingLoans_Success() {
        // Arrange
        PersonalLoan loan = PersonalLoan.builder()
                .id(loanId)
                .consumer(mockConsumer)
                .status(PersonalLoan.LoanStatus.PENDING)
                .build();

        when(loanRepository.findByStatus(PersonalLoan.LoanStatus.PENDING))
                .thenReturn(Arrays.asList(loan));

        // Act
        List<LoanResponse> responses = personalLoanService.getPendingLoans();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(loanRepository, times(1)).findByStatus(PersonalLoan.LoanStatus.PENDING);
    }
}

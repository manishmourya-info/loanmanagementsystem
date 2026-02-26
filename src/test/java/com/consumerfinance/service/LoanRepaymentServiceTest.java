package com.consumerfinance.service;

import com.consumerfinance.domain.LoanRepayment;
import com.consumerfinance.domain.PersonalLoan;
import com.consumerfinance.domain.Consumer;
import com.consumerfinance.dto.RepaymentResponse;
import com.consumerfinance.exception.InvalidRepaymentException;
import com.consumerfinance.exception.LoanNotFoundException;
import com.consumerfinance.repository.LoanRepaymentRepository;
import com.consumerfinance.repository.PersonalLoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoanRepaymentService.
 * Tests repayment processing and schedule management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Loan Repayment Service Tests")
class LoanRepaymentServiceTest {

    @Mock
    private LoanRepaymentRepository repaymentRepository;

    @Mock
    private PersonalLoanRepository loanRepository;

    @InjectMocks
    private LoanRepaymentService repaymentService;

    private UUID loanId;
    private UUID consumerId;
    private PersonalLoan mockLoan;
    private LoanRepayment mockRepayment;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();
        consumerId = UUID.randomUUID();

        Consumer consumer = Consumer.builder()
                .consumerId(consumerId)
                .name("John Doe")
                .email("john@example.com")
                .build();

        mockLoan = PersonalLoan.builder()
                .id(loanId)
                .consumer(consumer)
                .principalAmount(BigDecimal.valueOf(500000))
                .monthlyEMI(BigDecimal.valueOf(10746.95))
                .outstandingBalance(BigDecimal.valueOf(500000))
                .remainingTenure(60)
                .status(PersonalLoan.LoanStatus.APPROVED)
                .build();

        mockRepayment = LoanRepayment.builder()
                .loan(mockLoan)
                .installmentNumber(1)
                .principalAmount(BigDecimal.valueOf(8333.33))
                .interestAmount(BigDecimal.valueOf(2413.62))
                .totalAmount(BigDecimal.valueOf(10746.95))
                .paidAmount(BigDecimal.ZERO)
                .status(LoanRepayment.RepaymentStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should process repayment successfully for full amount")
    void testProcessRepayment_FullPayment() {
        // Arrange
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepository.findByLoanAndInstallmentNumber(mockLoan, 1))
                .thenReturn(Optional.of(mockRepayment));
        when(repaymentRepository.save(any(LoanRepayment.class))).thenReturn(mockRepayment);
        when(repaymentRepository.countByLoanAndStatus(mockLoan, LoanRepayment.RepaymentStatus.PENDING))
                .thenReturn(59L);

        // Act
        RepaymentResponse response = repaymentService.processRepayment(loanId, 1, BigDecimal.valueOf(10746.95));

        // Assert
        assertNotNull(response);
        verify(loanRepository, times(1)).save(any(PersonalLoan.class));
        verify(repaymentRepository, times(1)).save(any(LoanRepayment.class));
    }

    @Test
    @DisplayName("Should process partial repayment successfully")
    void testProcessRepayment_PartialPayment() {
        // Arrange
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepository.findByLoanAndInstallmentNumber(mockLoan, 1))
                .thenReturn(Optional.of(mockRepayment));

        LoanRepayment partialRepayment = mockRepayment;
        partialRepayment.setPaidAmount(BigDecimal.valueOf(5000));
        partialRepayment.setStatus(LoanRepayment.RepaymentStatus.PARTIALLY_PAID);

        when(repaymentRepository.save(any(LoanRepayment.class))).thenReturn(partialRepayment);
        when(repaymentRepository.countByLoanAndStatus(mockLoan, LoanRepayment.RepaymentStatus.PENDING))
                .thenReturn(60L);

        // Act
        RepaymentResponse response = repaymentService.processRepayment(loanId, 1, BigDecimal.valueOf(5000));

        // Assert
        assertNotNull(response);
        verify(loanRepository, times(1)).save(any(PersonalLoan.class));
        verify(repaymentRepository, times(1)).save(any(LoanRepayment.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid loan ID")
    void testProcessRepayment_LoanNotFound() {
        // Arrange
        when(loanRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LoanNotFoundException.class, () -> 
            repaymentService.processRepayment(loanId, 1, BigDecimal.valueOf(10746.95)));
    }

    @Test
    @DisplayName("Should throw exception for invalid repayment")
    void testProcessRepayment_RepaymentNotFound() {
        // Arrange
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepository.findByLoanAndInstallmentNumber(mockLoan, 99))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRepaymentException.class, () -> 
            repaymentService.processRepayment(loanId, 99, BigDecimal.valueOf(10746.95)));
    }

    @Test
    @DisplayName("Should get repayment details successfully")
    void testGetRepayment_Success() {
        // Arrange
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepository.findByLoanAndInstallmentNumber(mockLoan, 1))
                .thenReturn(Optional.of(mockRepayment));

        // Act
        RepaymentResponse response = repaymentService.getRepayment(loanId, 1);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getInstallmentNumber());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when repayment not found")
    void testGetRepayment_NotFound() {
        // Arrange
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepository.findByLoanAndInstallmentNumber(mockLoan, 1))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRepaymentException.class, () -> 
            repaymentService.getRepayment(loanId, 1));
    }

    @Test
    @DisplayName("Should get all repayments for loan")
    void testGetRepaymentsByLoanId_Success() {
        // Arrange
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepository.findByLoan(mockLoan))
                .thenReturn(Arrays.asList(mockRepayment));

        // Act
        List<RepaymentResponse> responses = repaymentService.getRepaymentsByLoanId(loanId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(repaymentRepository, times(1)).findByLoan(mockLoan);
    }

    @Test
    @DisplayName("Should get pending repayments only")
    void testGetPendingRepaymentsByLoanId_Success() {
        // Arrange
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepository.findByLoan(mockLoan))
                .thenReturn(Arrays.asList(mockRepayment));

        // Act
        List<RepaymentResponse> responses = repaymentService.getPendingRepaymentsByLoanId(loanId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(repaymentRepository, times(1)).findByLoan(mockLoan);
    }

    @Test
    @DisplayName("Should get overdue repayments")
    void testGetOverdueRepayments_Success() {
        // Arrange
        LoanRepayment overdueRepayment = LoanRepayment.builder()
                .loan(mockLoan)
                .installmentNumber(1)
                .status(LoanRepayment.RepaymentStatus.OVERDUE)
                .build();

        when(repaymentRepository.findOverdueRepayments()).thenReturn(Arrays.asList(overdueRepayment));

        // Act
        List<RepaymentResponse> responses = repaymentService.getOverdueRepayments();

        // Assert
        assertNotNull(responses);
        verify(repaymentRepository, times(1)).findOverdueRepayments();
    }

    @Test
    @DisplayName("Should throw exception when loan not found for repayments")
    void testGetRepaymentsByLoanId_LoanNotFound() {
        // Arrange
        when(loanRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LoanNotFoundException.class, () -> 
            repaymentService.getRepaymentsByLoanId(loanId));
    }
}

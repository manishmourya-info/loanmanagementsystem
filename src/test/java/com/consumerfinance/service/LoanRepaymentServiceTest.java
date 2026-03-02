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
import static org.mockito.Mockito.*;

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
                .phone("+1234567890")
                .identityType("PAN")
                .identityNumber("ABCDE1234F")
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
                .id(UUID.randomUUID()) // ✅ FIX
                .loan(mockLoan)
                .installmentNumber(1)
                .principalAmount(BigDecimal.valueOf(8333.33))
                .interestAmount(BigDecimal.valueOf(2413.62))
                .totalAmount(BigDecimal.valueOf(10746.95))
                .paidAmount(BigDecimal.ZERO)
                .dueDate(LocalDateTime.now().plusDays(5))
                .status(LoanRepayment.RepaymentStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should process full repayment successfully")
    void testProcessRepayment_FullPayment() {

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepository.findByLoanAndInstallmentNumber(mockLoan, 1))
                .thenReturn(Optional.of(mockRepayment));
        when(repaymentRepository.save(any())).thenReturn(mockRepayment);
        when(repaymentRepository.countByLoanAndStatus(mockLoan, LoanRepayment.RepaymentStatus.PENDING))
                .thenReturn(59L);

        RepaymentResponse response =
                repaymentService.processRepayment(loanId, 1, BigDecimal.valueOf(10746.95));

        assertNotNull(response);
        verify(loanRepository).save(any());
        verify(repaymentRepository).save(any());
    }

    @Test
    @DisplayName("Should process partial repayment successfully")
    void testProcessRepayment_PartialPayment() {

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepository.findByLoanAndInstallmentNumber(mockLoan, 1))
                .thenReturn(Optional.of(mockRepayment));

        LoanRepayment partialRepayment = LoanRepayment.builder()
                .id(UUID.randomUUID()) // ✅ FIX
                .loan(mockLoan)
                .installmentNumber(1)
                .principalAmount(BigDecimal.valueOf(8333.33))
                .interestAmount(BigDecimal.valueOf(2413.62))
                .totalAmount(BigDecimal.valueOf(10746.95))
                .paidAmount(BigDecimal.valueOf(5000))
                .dueDate(LocalDateTime.now().plusDays(5))
                .status(LoanRepayment.RepaymentStatus.PARTIALLY_PAID)
                .build();

        when(repaymentRepository.save(any())).thenReturn(partialRepayment);
        when(repaymentRepository.countByLoanAndStatus(mockLoan, LoanRepayment.RepaymentStatus.PENDING))
                .thenReturn(60L);

        RepaymentResponse response =
                repaymentService.processRepayment(loanId, 1, BigDecimal.valueOf(5000));

        assertNotNull(response);
        verify(loanRepository).save(any());
        verify(repaymentRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when loan not found")
    void testProcessRepayment_LoanNotFound() {
        when(loanRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () ->
                repaymentService.processRepayment(loanId, 1, BigDecimal.TEN));
    }

    @Test
    @DisplayName("Should throw exception when repayment not found")
    void testProcessRepayment_RepaymentNotFound() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepository.findByLoanAndInstallmentNumber(mockLoan, 99))
                .thenReturn(Optional.empty());

        assertThrows(InvalidRepaymentException.class, () ->
                repaymentService.processRepayment(loanId, 99, BigDecimal.TEN));
    }

    @Test
    @DisplayName("Should get repayment successfully")
    void testGetRepayment_Success() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepository.findByLoanAndInstallmentNumber(mockLoan, 1))
                .thenReturn(Optional.of(mockRepayment));

        RepaymentResponse response =
                repaymentService.getRepayment(loanId, 1);

        assertNotNull(response);
        assertEquals(1, response.getInstallmentNumber());
    }

    @Test
    @DisplayName("Should get all repayments for loan")
    void testGetRepaymentsByLoanId_Success() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));
        when(repaymentRepository.findByLoan(mockLoan))
                .thenReturn(Arrays.asList(mockRepayment));

        List<RepaymentResponse> responses =
                repaymentService.getRepaymentsByLoanId(loanId);

        assertEquals(1, responses.size());
    }

    @Test
    @DisplayName("Should get overdue repayments")
    void testGetOverdueRepayments_Success() {

        LoanRepayment overdue = LoanRepayment.builder()
                .id(UUID.randomUUID()) // ✅ FIX
                .loan(mockLoan)
                .installmentNumber(1)
                .dueDate(LocalDateTime.now().minusDays(5))
                .status(LoanRepayment.RepaymentStatus.OVERDUE)
                .build();

        when(repaymentRepository.findOverdueRepayments())
                .thenReturn(Arrays.asList(overdue));

        List<RepaymentResponse> responses =
                repaymentService.getOverdueRepayments();

        assertEquals(1, responses.size());
    }
}
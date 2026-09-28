package com.microsave.service;

import com.microsave.dto.RepaymentRequest;
import com.microsave.entity.Loan;
import com.microsave.entity.LoanStatus;
import com.microsave.entity.Repayment;
import com.microsave.exception.BusinessRuleException;
import com.microsave.repository.LoanRepository;
import com.microsave.repository.RepaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepaymentService {

    private final RepaymentRepository repaymentRepository;
    private final LoanService loanService;
    private final LoanRepository loanRepository;

    @Transactional
    public Repayment createRepayment(RepaymentRequest request) {
        Loan loan = loanService.getLoanById(request.getLoanId());
        
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessRuleException("Cannot repay an inactive or already paid loan.");
        }
        
        BigDecimal totalRepayments = repaymentRepository.getTotalRepaymentsForLoan(loan.getLoanId());
        if (totalRepayments == null) {
            totalRepayments = BigDecimal.ZERO;
        }
        BigDecimal outstandingAmount = loan.getAmount().subtract(totalRepayments);
        
        if (request.getAmount().compareTo(outstandingAmount) > 0) {
            throw new BusinessRuleException("Repayment exceeds outstanding amount.");
        }
        
        Repayment repayment = new Repayment();
        repayment.setAmount(request.getAmount());
        repayment.setRepaymentDate(request.getRepaymentDate());
        repayment.setLoan(loan);
        
        Repayment savedRepayment = repaymentRepository.save(repayment);
        
        // Update loan status if fully paid
        if (request.getAmount().compareTo(outstandingAmount) == 0) {
            loan.setStatus(LoanStatus.PAID);
            loanRepository.save(loan);
        }
        
        return savedRepayment;
    }

    public List<Repayment> getAllRepayments() {
        return repaymentRepository.findAll();
    }

    public List<Repayment> getRepaymentsByLoan(Long loanId) {
        return repaymentRepository.findByLoanLoanId(loanId);
    }
}

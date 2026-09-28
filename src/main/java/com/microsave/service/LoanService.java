package com.microsave.service;

import com.microsave.dto.GroupPoolResponse;
import com.microsave.dto.LoanRequest;
import com.microsave.entity.Loan;
import com.microsave.entity.LoanStatus;
import com.microsave.entity.Member;
import com.microsave.exception.BusinessRuleException;
import com.microsave.exception.ResourceNotFoundException;
import com.microsave.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final MemberService memberService;
    private final GroupService groupService;

    public Loan createLoan(LoanRequest request) {
        Member member = memberService.getMemberById(request.getMemberId());
        
        // RULE 2: A member with an active unpaid loan cannot take a second loan.
        if (hasActiveLoan(member.getMemberId())) {
            throw new BusinessRuleException("Member already has an active loan.");
        }
        
        // RULE 3: Loan Cannot Exceed Pool
        GroupPoolResponse poolResponse = groupService.getGroupPool(member.getGroup().getGroupId());
        if (request.getAmount().compareTo(poolResponse.getAvailablePool()) > 0) {
            throw new BusinessRuleException("Insufficient group balance for this loan.");
        }
        
        Loan loan = new Loan();
        loan.setAmount(request.getAmount());
        loan.setLoanDate(request.getLoanDate());
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setMember(member);
        
        return loanRepository.save(loan);
    }

    public boolean hasActiveLoan(Long memberId) {
        return loanRepository.existsByMemberMemberIdAndStatus(memberId, LoanStatus.ACTIVE);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + id));
    }

    public List<Loan> getLoansByMember(Long memberId) {
        return loanRepository.findByMemberMemberId(memberId);
    }
}

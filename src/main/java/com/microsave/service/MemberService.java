package com.microsave.service;

import com.microsave.dto.MemberBalanceResponse;
import com.microsave.dto.MemberRequest;
import com.microsave.entity.Contribution;
import com.microsave.entity.Group;
import com.microsave.entity.Loan;
import com.microsave.entity.LoanStatus;
import com.microsave.entity.Member;
import com.microsave.entity.Repayment;
import com.microsave.exception.ResourceNotFoundException;
import com.microsave.repository.ContributionRepository;
import com.microsave.repository.LoanRepository;
import com.microsave.repository.MemberRepository;
import com.microsave.repository.RepaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final GroupService groupService;
    private final ContributionRepository contributionRepository;
    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;

    public Member createMember(MemberRequest request) {
        Group group = groupService.getGroupById(request.getGroupId());
        
        Member member = new Member();
        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setGroup(group);
        
        return memberRepository.save(member);
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));
    }

    public MemberBalanceResponse getMemberBalance(Long memberId) {
        Member member = getMemberById(memberId);
        
        List<Contribution> contributions = contributionRepository.findByMemberMemberId(memberId);
        BigDecimal totalSavings = contributions.stream()
                .map(Contribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        List<Loan> loans = loanRepository.findByMemberMemberId(memberId);
        BigDecimal outstandingLoan = BigDecimal.ZERO;
        
        for (Loan loan : loans) {
            if (loan.getStatus() == LoanStatus.ACTIVE) {
                BigDecimal repaidAmount = repaymentRepository.getTotalRepaymentsForLoan(loan.getLoanId());
                if (repaidAmount == null) {
                    repaidAmount = BigDecimal.ZERO;
                }
                outstandingLoan = outstandingLoan.add(loan.getAmount().subtract(repaidAmount));
            }
        }
        
        return new MemberBalanceResponse(
                member.getMemberId(),
                member.getName(),
                totalSavings,
                outstandingLoan
        );
    }
}

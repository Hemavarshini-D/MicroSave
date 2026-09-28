package com.microsave;

import com.microsave.dto.*;
import com.microsave.entity.Group;
import com.microsave.entity.Loan;
import com.microsave.entity.LoanStatus;
import com.microsave.entity.Member;
import com.microsave.entity.Repayment;
import com.microsave.exception.BusinessRuleException;
import com.microsave.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MicroSaveWorkflowTests {

    @Autowired
    private GroupService groupService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ContributionService contributionService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private RepaymentService repaymentService;

    @Test
    @DisplayName("Complete SHG Lifecycle & Core Business Rules Verification")
    void testCompleteSHGWorkflowAndBusinessRules() {
        // Step 1: Create Group
        GroupRequest groupRequest = new GroupRequest();
        groupRequest.setGroupName("Ooty Women's SHG");
        Group group = groupService.createGroup(groupRequest);
        assertNotNull(group.getGroupId());
        assertEquals("Ooty Women's SHG", group.getGroupName());

        // Step 2: Create Members
        MemberRequest member1Req = new MemberRequest();
        member1Req.setName("Anitha");
        member1Req.setPhone("9876543210");
        member1Req.setGroupId(group.getGroupId());
        Member anitha = memberService.createMember(member1Req);
        assertNotNull(anitha.getMemberId());

        MemberRequest member2Req = new MemberRequest();
        member2Req.setName("Kavitha");
        member2Req.setPhone("9876543211");
        member2Req.setGroupId(group.getGroupId());
        Member kavitha = memberService.createMember(member2Req);
        assertNotNull(kavitha.getMemberId());

        // Step 3: Record Weekly Savings Contributions
        ContributionRequest c1 = new ContributionRequest();
        c1.setMemberId(anitha.getMemberId());
        c1.setAmount(new BigDecimal("30000.00"));
        c1.setContributionDate(LocalDate.now());
        contributionService.createContribution(c1);

        ContributionRequest c2 = new ContributionRequest();
        c2.setMemberId(kavitha.getMemberId());
        c2.setAmount(new BigDecimal("20000.00"));
        c2.setContributionDate(LocalDate.now());
        contributionService.createContribution(c2);

        // Step 4: Verify Group Pool (Total = 50,000, Outstanding = 0, Available = 50,000)
        GroupPoolResponse pool1 = groupService.getGroupPool(group.getGroupId());
        assertEquals(new BigDecimal("50000.00"), pool1.getTotalContributions());
        assertEquals(BigDecimal.ZERO, pool1.getOutstandingLoans());
        assertEquals(new BigDecimal("50000.00"), pool1.getAvailablePool());

        // Step 5: Disburse Valid Loan to Anitha of 20,000
        LoanRequest loanReq = new LoanRequest();
        loanReq.setMemberId(anitha.getMemberId());
        loanReq.setAmount(new BigDecimal("20000.00"));
        loanReq.setLoanDate(LocalDate.now());
        Loan loan = loanService.createLoan(loanReq);
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertEquals(new BigDecimal("20000.00"), loan.getAmount());

        // Step 6: Verify Available Pool decreased (Available = 30,000)
        GroupPoolResponse pool2 = groupService.getGroupPool(group.getGroupId());
        assertEquals(new BigDecimal("50000.00"), pool2.getTotalContributions());
        assertEquals(new BigDecimal("20000.00"), pool2.getOutstandingLoans());
        assertEquals(new BigDecimal("30000.00"), pool2.getAvailablePool());

        // Step 7: RULE 2 — Try Second Active Loan for Anitha (Must REJECT)
        LoanRequest secondLoanReq = new LoanRequest();
        secondLoanReq.setMemberId(anitha.getMemberId());
        secondLoanReq.setAmount(new BigDecimal("5000.00"));
        secondLoanReq.setLoanDate(LocalDate.now());
        BusinessRuleException rule2Ex = assertThrows(BusinessRuleException.class, () -> {
            loanService.createLoan(secondLoanReq);
        });
        assertEquals("Member already has an active loan.", rule2Ex.getMessage());

        // Step 8: RULE 3 — Try Loan Exceeding Available Pool for Kavitha (Pool is 30,000, Request is 35,000)
        LoanRequest excessLoanReq = new LoanRequest();
        excessLoanReq.setMemberId(kavitha.getMemberId());
        excessLoanReq.setAmount(new BigDecimal("35000.00"));
        excessLoanReq.setLoanDate(LocalDate.now());
        BusinessRuleException rule3Ex = assertThrows(BusinessRuleException.class, () -> {
            loanService.createLoan(excessLoanReq);
        });
        assertEquals("Insufficient group balance for this loan.", rule3Ex.getMessage());

        // Step 9: RULE 4 — Partial Repayment of 5,000
        RepaymentRequest rep1 = new RepaymentRequest();
        rep1.setLoanId(loan.getLoanId());
        rep1.setAmount(new BigDecimal("5000.00"));
        rep1.setRepaymentDate(LocalDate.now());
        Repayment repayment1 = repaymentService.createRepayment(rep1);
        assertNotNull(repayment1.getRepaymentId());

        // Check Outstanding Loan is now 15,000 and Available Pool is now 35,000
        GroupPoolResponse pool3 = groupService.getGroupPool(group.getGroupId());
        assertEquals(new BigDecimal("15000.00"), pool3.getOutstandingLoans());
        assertEquals(new BigDecimal("35000.00"), pool3.getAvailablePool());

        // Step 10: Try Excessive Repayment (Outstanding is 15,000, try paying 20,000)
        RepaymentRequest excessRepReq = new RepaymentRequest();
        excessRepReq.setLoanId(loan.getLoanId());
        excessRepReq.setAmount(new BigDecimal("20000.00"));
        excessRepReq.setRepaymentDate(LocalDate.now());
        BusinessRuleException repEx = assertThrows(BusinessRuleException.class, () -> {
            repaymentService.createRepayment(excessRepReq);
        });
        assertEquals("Repayment exceeds outstanding amount.", repEx.getMessage());

        // Step 11: Final Repayment of Remaining 15,000
        RepaymentRequest rep2 = new RepaymentRequest();
        rep2.setLoanId(loan.getLoanId());
        rep2.setAmount(new BigDecimal("15000.00"));
        rep2.setRepaymentDate(LocalDate.now());
        repaymentService.createRepayment(rep2);

        // Verify Loan Status is now PAID
        Loan updatedLoan = loanService.getLoanById(loan.getLoanId());
        assertEquals(LoanStatus.PAID, updatedLoan.getStatus());

        // Step 12: Verify Pool After Full Repayment (Outstanding = 0, Available = 50,000)
        GroupPoolResponse pool4 = groupService.getGroupPool(group.getGroupId());
        assertEquals(BigDecimal.ZERO, pool4.getOutstandingLoans());
        assertEquals(new BigDecimal("50000.00"), pool4.getAvailablePool());

        // Step 13: Verify Anitha Balance (Savings = 30,000, Outstanding = 0)
        MemberBalanceResponse balance = memberService.getMemberBalance(anitha.getMemberId());
        assertEquals(new BigDecimal("30000.00"), balance.getTotalSavings());
        assertEquals(BigDecimal.ZERO, balance.getOutstandingLoan());

        // Step 14: Anitha is now free to take another loan
        LoanRequest newLoanReq = new LoanRequest();
        newLoanReq.setMemberId(anitha.getMemberId());
        newLoanReq.setAmount(new BigDecimal("10000.00"));
        newLoanReq.setLoanDate(LocalDate.now());
        Loan approvedNewLoan = loanService.createLoan(newLoanReq);
        assertEquals(LoanStatus.ACTIVE, approvedNewLoan.getStatus());
        assertEquals(new BigDecimal("10000.00"), approvedNewLoan.getAmount());
    }
}

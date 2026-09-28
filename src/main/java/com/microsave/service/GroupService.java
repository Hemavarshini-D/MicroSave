package com.microsave.service;

import com.microsave.dto.GroupPoolResponse;
import com.microsave.dto.GroupRequest;
import com.microsave.entity.Group;
import com.microsave.exception.ResourceNotFoundException;
import com.microsave.repository.ContributionRepository;
import com.microsave.repository.GroupRepository;
import com.microsave.repository.LoanRepository;
import com.microsave.repository.RepaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final ContributionRepository contributionRepository;
    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;

    public Group createGroup(GroupRequest request) {
        Group group = new Group();
        group.setGroupName(request.getGroupName());
        return groupRepository.save(group);
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group getGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + id));
    }

    public GroupPoolResponse getGroupPool(Long groupId) {
        Group group = getGroupById(groupId);
        
        BigDecimal totalContributions = contributionRepository.getTotalContributionsByGroupId(groupId);
        if (totalContributions == null) {
            totalContributions = BigDecimal.ZERO;
        }
        
        List<com.microsave.entity.Loan> activeLoans = loanRepository.findByMemberGroupGroupIdAndStatus(groupId, com.microsave.entity.LoanStatus.ACTIVE);
        BigDecimal outstandingLoans = BigDecimal.ZERO;
        
        for (com.microsave.entity.Loan loan : activeLoans) {
            BigDecimal totalRepayments = repaymentRepository.getTotalRepaymentsForLoan(loan.getLoanId());
            if (totalRepayments == null) {
                totalRepayments = BigDecimal.ZERO;
            }
            BigDecimal loanOutstanding = loan.getAmount().subtract(totalRepayments);
            outstandingLoans = outstandingLoans.add(loanOutstanding);
        }
        
        BigDecimal availablePool = totalContributions.subtract(outstandingLoans);
        
        return new GroupPoolResponse(
                group.getGroupId(),
                group.getGroupName(),
                totalContributions,
                outstandingLoans,
                availablePool
        );
    }
}

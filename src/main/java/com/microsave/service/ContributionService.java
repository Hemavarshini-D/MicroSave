package com.microsave.service;

import com.microsave.dto.ContributionRequest;
import com.microsave.entity.Contribution;
import com.microsave.entity.Member;
import com.microsave.exception.ResourceNotFoundException;
import com.microsave.repository.ContributionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final MemberService memberService;

    public Contribution createContribution(ContributionRequest request) {
        Member member = memberService.getMemberById(request.getMemberId());
        
        Contribution contribution = new Contribution();
        contribution.setAmount(request.getAmount());
        contribution.setContributionDate(request.getContributionDate());
        contribution.setMember(member);
        
        return contributionRepository.save(contribution);
    }

    public List<Contribution> getAllContributions() {
        return contributionRepository.findAll();
    }

    public List<Contribution> getContributionsByMember(Long memberId) {
        return contributionRepository.findByMemberMemberId(memberId);
    }
}

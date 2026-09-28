package com.microsave.controller;

import com.microsave.dto.ContributionRequest;
import com.microsave.entity.Contribution;
import com.microsave.service.ContributionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contributions")
@RequiredArgsConstructor
public class ContributionController {

    private final ContributionService contributionService;

    @PostMapping
    public ResponseEntity<Contribution> createContribution(@Valid @RequestBody ContributionRequest request) {
        return new ResponseEntity<>(contributionService.createContribution(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Contribution>> getAllContributions() {
        return ResponseEntity.ok(contributionService.getAllContributions());
    }
    
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Contribution>> getContributionsByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(contributionService.getContributionsByMember(memberId));
    }
}

package com.microsave.controller;

import com.microsave.dto.RepaymentRequest;
import com.microsave.entity.Repayment;
import com.microsave.service.RepaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayments")
@RequiredArgsConstructor
public class RepaymentController {

    private final RepaymentService repaymentService;

    @PostMapping
    public ResponseEntity<Repayment> createRepayment(@Valid @RequestBody RepaymentRequest request) {
        return new ResponseEntity<>(repaymentService.createRepayment(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Repayment>> getAllRepayments() {
        return ResponseEntity.ok(repaymentService.getAllRepayments());
    }
    
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<Repayment>> getRepaymentsByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(repaymentService.getRepaymentsByLoan(loanId));
    }
}

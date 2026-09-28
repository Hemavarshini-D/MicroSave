package com.microsave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MemberBalanceResponse {
    private Long memberId;
    private String name;
    private BigDecimal totalSavings;
    private BigDecimal outstandingLoan;
}

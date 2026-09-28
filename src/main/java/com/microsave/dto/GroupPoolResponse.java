package com.microsave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class GroupPoolResponse {
    private Long groupId;
    private String groupName;
    private BigDecimal totalContributions;
    private BigDecimal outstandingLoans;
    private BigDecimal availablePool;
}

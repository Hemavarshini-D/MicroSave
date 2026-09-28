package com.microsave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    private String phone;
    
    @NotNull(message = "Group ID is required")
    private Long groupId;
}

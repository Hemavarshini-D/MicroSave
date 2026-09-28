package com.microsave.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupRequest {
    @NotBlank(message = "Group name is required")
    private String groupName;
}

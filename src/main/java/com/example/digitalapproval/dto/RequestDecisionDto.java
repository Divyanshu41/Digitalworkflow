package com.example.digitalapproval.dto;

import jakarta.validation.constraints.Size;

public record RequestDecisionDto(
        @Size(max = 500, message = "Remarks must be at most 500 characters")
        String remarks
) {
}

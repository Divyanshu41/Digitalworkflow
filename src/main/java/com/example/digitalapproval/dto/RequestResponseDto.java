package com.example.digitalapproval.dto;

import java.time.LocalDateTime;

public record RequestResponseDto(
        Long id,
        String title,
        String description,
        String status,
        String remarks,
        LocalDateTime submittedAt,
        Long userId
) {
}

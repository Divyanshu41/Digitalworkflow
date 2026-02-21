package com.example.digitalapproval.dto;

public record AuthMeResponseDto(
    Long id,
    String email,
    String role
) {
}
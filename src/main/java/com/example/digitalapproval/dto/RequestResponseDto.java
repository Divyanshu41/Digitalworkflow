
package com.example.digitalapproval.dto;

import java.time.LocalDateTime;

public class RequestResponseDto {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String remarks;
    private LocalDateTime submittedAt;
    private Long userId;

    // Phase 4 audit fields
    private LocalDateTime decidedAt;
    private Long decidedById;
    private String decidedByEmail;

    public RequestResponseDto() {
    }

    public RequestResponseDto(
            Long id,
            String title,
            String description,
            String status,
            String remarks,
            LocalDateTime submittedAt,
            Long userId,
            LocalDateTime decidedAt,
            Long decidedById,
            String decidedByEmail) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.remarks = remarks;
        this.submittedAt = submittedAt;
        this.userId = userId;
        this.decidedAt = decidedAt;
        this.decidedById = decidedById;
        this.decidedByEmail = decidedByEmail;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getRemarks() {
        return remarks;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public Long getDecidedById() {
        return decidedById;
    }

    public String getDecidedByEmail() {
        return decidedByEmail;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public void setDecidedById(Long decidedById) {
        this.decidedById = decidedById;
    }

    public void setDecidedByEmail(String decidedByEmail) {
        this.decidedByEmail = decidedByEmail;
    }
}

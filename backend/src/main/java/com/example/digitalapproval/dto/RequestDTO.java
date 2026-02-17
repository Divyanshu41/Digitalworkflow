package com.example.digitalapproval.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.digitalapproval.entity.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RequestDTO {

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotBlank
    private String category;

    @NotNull
    private LocalDate requestedDate;

    private String attachment;
    private String attachmentOriginalName;
    private String attachmentDownloadUrl;

    private Request.Status status;

    private Long userId;

    private String username;

    private Long approverId;

    private String approverName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructors
    public RequestDTO() {}

    public RequestDTO(Request request) {
        this.id = request.getId();
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.category = request.getCategory();
        this.attachment = request.getAttachment();
        this.attachmentOriginalName = request.getAttachmentOriginalName();
        this.status = request.getStatus();
        this.requestedDate = request.getRequestedDate();
        this.userId = request.getUser().getId();
        this.username = request.getUser().getUsername();
        if (request.getApprover() != null) {
            this.approverId = request.getApprover().getId();
            this.approverName = request.getApprover().getUsername();
        }
        this.createdAt = request.getCreatedAt();
        this.updatedAt = request.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }

    public String getAttachmentOriginalName() { return attachmentOriginalName; }
    public void setAttachmentOriginalName(String attachmentOriginalName) { this.attachmentOriginalName = attachmentOriginalName; }

    public String getAttachmentDownloadUrl() { return attachmentDownloadUrl; }
    public void setAttachmentDownloadUrl(String attachmentDownloadUrl) { this.attachmentDownloadUrl = attachmentDownloadUrl; }

    public LocalDate getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDate requestedDate) { this.requestedDate = requestedDate; }

    public Request.Status getStatus() { return status; }
    public void setStatus(Request.Status status) { this.status = status; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
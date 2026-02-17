package com.example.digitalapproval.dto;

import java.time.LocalDateTime;

import com.example.digitalapproval.entity.AuditLog;

public class AuditLogDTO {
    private Long id;
    private Long actorId;
    private String action;
    private String details;
    private LocalDateTime timestamp;

    public AuditLogDTO() {
    }

    public AuditLogDTO(AuditLog log) {
        this.id = log.getId();
        this.actorId = log.getActorId();
        this.action = log.getAction();
        this.details = log.getDetails();
        this.timestamp = log.getTimestamp();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

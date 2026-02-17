package com.example.digitalapproval.dto;

import java.time.LocalDateTime;

import com.example.digitalapproval.entity.Approval;
import com.example.digitalapproval.entity.Request;

public class ApprovalDTO {

    private Long id;
    private Long requestId;
    private String requestTitle;
    private Long approverId;
    private String approverName;
    private Request.Status status;
    private String remarks;
    private LocalDateTime approvedAt;

    // Constructors
    public ApprovalDTO() {}

    public ApprovalDTO(Approval approval) {
        this.id = approval.getId();
        this.requestId = approval.getRequest().getId();
        this.requestTitle = approval.getRequest().getTitle();
        this.approverId = approval.getApprover().getId();
        this.approverName = approval.getApprover().getUsername();
        this.status = approval.getStatus();
        this.remarks = approval.getRemarks();
        this.approvedAt = approval.getApprovedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getRequestTitle() { return requestTitle; }
    public void setRequestTitle(String requestTitle) { this.requestTitle = requestTitle; }

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    public Request.Status getStatus() { return status; }
    public void setStatus(Request.Status status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
}
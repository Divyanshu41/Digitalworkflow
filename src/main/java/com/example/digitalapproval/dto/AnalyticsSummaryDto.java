
package com.example.digitalapproval.dto;

public class AnalyticsSummaryDto {

    private long totalRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;
    private double approvalSuccessRate;

    public AnalyticsSummaryDto(
            long totalRequests,
            long pendingRequests,
            long approvedRequests,
            long rejectedRequests,
            double approvalSuccessRate) {

        this.totalRequests = totalRequests;
        this.pendingRequests = pendingRequests;
        this.approvedRequests = approvedRequests;
        this.rejectedRequests = rejectedRequests;
        this.approvalSuccessRate = approvalSuccessRate;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public long getApprovedRequests() {
        return approvedRequests;
    }

    public long getRejectedRequests() {
        return rejectedRequests;
    }

    public double getApprovalSuccessRate() {
        return approvalSuccessRate;
    }
}

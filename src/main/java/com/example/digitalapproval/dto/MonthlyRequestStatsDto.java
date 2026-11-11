
package com.example.digitalapproval.dto;

public class MonthlyRequestStatsDto {

    private int year;
    private int month;
    private long totalRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;

    public MonthlyRequestStatsDto(
            int year,
            int month,
            long totalRequests,
            long pendingRequests,
            long approvedRequests,
            long rejectedRequests) {

        this.year = year;
        this.month = month;
        this.totalRequests = totalRequests;
        this.pendingRequests = pendingRequests;
        this.approvedRequests = approvedRequests;
        this.rejectedRequests = rejectedRequests;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
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
}

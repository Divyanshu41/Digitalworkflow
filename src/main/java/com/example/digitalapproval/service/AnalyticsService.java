
package com.example.digitalapproval.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.digitalapproval.dto.AnalyticsSummaryDto;
import com.example.digitalapproval.dto.MonthlyRequestStatsDto;
import com.example.digitalapproval.entity.User;
import com.example.digitalapproval.repository.RequestRepository;
import com.example.digitalapproval.repository.UserRepository;

@Service
public class AnalyticsService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    public AnalyticsService(
            RequestRepository requestRepository,
            UserRepository userRepository) {

        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    private Long getAnalyticsUserId(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        boolean isAdmin = hasRole(authentication, "ADMIN");
        boolean isApprover = hasRole(authentication, "APPROVER");

        // ADMIN and APPROVER get system-wide analytics
        if (isAdmin || isApprover) {
            return null;
        }

        // USER gets only personal analytics
        if (hasRole(authentication, "USER")) {

            User user = userRepository.findByEmailWithRole(authentication.getName())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Logged-in user not found"));

            return user.getId();
        }

        throw new AccessDeniedException("You cannot access analytics");
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_" + role));
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getSummary(Authentication authentication) {

        Long userId = getAnalyticsUserId(authentication);

        long total = requestRepository.countTotalRequests(userId);
        long pending = requestRepository.countRequestsByStatus("PENDING", userId);
        long approved = requestRepository.countRequestsByStatus("APPROVED", userId);
        long rejected = requestRepository.countRequestsByStatus("REJECTED", userId);

        long decidedRequests = approved + rejected;

        double successRate = decidedRequests == 0
                ? 0.0
                : (approved * 100.0) / decidedRequests;

        successRate = Math.round(successRate * 100.0) / 100.0;

        return new AnalyticsSummaryDto(
                total,
                pending,
                approved,
                rejected,
                successRate
        );
    }

    @Transactional(readOnly = true)
    public List<MonthlyRequestStatsDto> getMonthlyStats(
            Authentication authentication) {

        Long userId = getAnalyticsUserId(authentication);

        return requestRepository.findMonthlyRequestStats(userId)
                .stream()
                .map(row -> new MonthlyRequestStatsDto(
                        row.getYear(),
                        row.getMonth(),
                        row.getTotalRequests(),
                        row.getPendingRequests(),
                        row.getApprovedRequests(),
                        row.getRejectedRequests()
                ))
                .toList();
    }
}

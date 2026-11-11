
package com.example.digitalapproval.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.digitalapproval.dto.AnalyticsSummaryDto;
import com.example.digitalapproval.dto.MonthlyRequestStatsDto;
import com.example.digitalapproval.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryDto> getSummary(
            Authentication authentication) {

        return ResponseEntity.ok(
                analyticsService.getSummary(authentication)
        );
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyRequestStatsDto>> getMonthlyStats(
            Authentication authentication) {

        return ResponseEntity.ok(
                analyticsService.getMonthlyStats(authentication)
        );
    }
}

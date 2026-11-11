
package com.example.digitalapproval.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.digitalapproval.dto.ApiResponseDto;
import com.example.digitalapproval.dto.RequestCreateDto;
import com.example.digitalapproval.dto.RequestDecisionDto;
import com.example.digitalapproval.dto.RequestResponseDto;
import com.example.digitalapproval.service.RequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<RequestResponseDto> createRequest(
            @Valid @RequestBody RequestCreateDto requestDto,
            Authentication authentication) {

        RequestResponseDto createdRequest =
                requestService.createRequest(requestDto, authentication);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdRequest);
    }

    // Supports date filtering and sorting.
    @GetMapping
    public ResponseEntity<List<RequestResponseDto>> getRequests(
            Authentication authentication,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @RequestParam(defaultValue = "latest")
            String sort) {

        List<RequestResponseDto> requests =
                requestService.getRequestsForCurrentUser(
                        authentication, fromDate, toDate, sort);

        return ResponseEntity.ok(requests);
    }

    // Request details
    @GetMapping("/{id}")
    public ResponseEntity<RequestResponseDto> getRequestDetails(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                requestService.getRequestDetails(id, authentication));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<RequestResponseDto>> getPendingRequests() {

        return ResponseEntity.ok(requestService.getPendingRequests());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponseDto> approveRequest(
            @PathVariable("id") Long requestId,
            @Valid @RequestBody(required = false)
            RequestDecisionDto decisionDto,
            Authentication authentication) {

        String remarks = decisionDto != null
                ? decisionDto.remarks()
                : null;

        requestService.approveRequest(
                requestId, remarks, authentication);

        return ResponseEntity.ok(
                new ApiResponseDto("Request approved successfully"));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponseDto> rejectRequest(
            @PathVariable("id") Long requestId,
            @Valid @RequestBody(required = false)
            RequestDecisionDto decisionDto,
            Authentication authentication) {

        String remarks = decisionDto != null
                ? decisionDto.remarks()
                : null;

        requestService.rejectRequest(
                requestId, remarks, authentication);

        return ResponseEntity.ok(
                new ApiResponseDto("Request rejected successfully"));
    }
}

package com.example.digitalapproval.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<RequestResponseDto> createRequest(@Valid @RequestBody RequestCreateDto requestDto) {
        RequestResponseDto createdRequest = requestService.createRequest(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    @GetMapping
    public ResponseEntity<List<RequestResponseDto>> getAllRequests() {
        List<RequestResponseDto> requests = requestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<RequestResponseDto>> getPendingRequests() {
        List<RequestResponseDto> requests = requestService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponseDto> approveRequest(
        @PathVariable("id") Long requestId,
        @Valid @RequestBody(required = false) RequestDecisionDto decisionDto
    ) {
        String remarks = decisionDto != null ? decisionDto.remarks() : null;
        requestService.approveRequest(requestId, remarks);
        return ResponseEntity.ok(new ApiResponseDto("Request approved successfully"));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponseDto> rejectRequest(
        @PathVariable("id") Long requestId,
        @Valid @RequestBody(required = false) RequestDecisionDto decisionDto
    ) {
        String remarks = decisionDto != null ? decisionDto.remarks() : null;
        requestService.rejectRequest(requestId, remarks);
        return ResponseEntity.ok(new ApiResponseDto("Request rejected successfully"));
    }
}

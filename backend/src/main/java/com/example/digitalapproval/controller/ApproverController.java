package com.example.digitalapproval.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.digitalapproval.dto.RequestDTO;
import com.example.digitalapproval.entity.Request;
import com.example.digitalapproval.service.ApprovalService;
import com.example.digitalapproval.service.EmailService;
import com.example.digitalapproval.service.RequestService;
import com.example.digitalapproval.service.UserDetailsImpl;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/approver")
@PreAuthorize("hasRole('APPROVER') or hasRole('ADMIN')")
public class ApproverController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/requests")
    public ResponseEntity<Page<RequestDTO>> getAssignedRequests(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<RequestDTO> requests = requestService.getRequestsByApprover(userDetails.getId(), pageable);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long requestId,
                                            @RequestParam String remarks,
                                            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Request request = requestService.approveRequest(requestId, userDetails.getId(), remarks);
        approvalService.createApproval(requestId, userDetails.getId(), Request.Status.APPROVED, remarks);
        emailService.sendApprovalNotification(request.getUser().getEmail(), request.getTitle(), "APPROVED");
        return ResponseEntity.ok(requestService.toDto(request));
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId,
                                           @RequestParam String remarks,
                                           Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Request request = requestService.rejectRequest(requestId, userDetails.getId(), remarks);
        approvalService.createApproval(requestId, userDetails.getId(), Request.Status.REJECTED, remarks);
        emailService.sendApprovalNotification(request.getUser().getEmail(), request.getTitle(), "REJECTED");
        return ResponseEntity.ok(requestService.toDto(request));
    }

    @GetMapping("/requests/{requestId}/approvals")
    public ResponseEntity<?> getApprovals(@PathVariable Long requestId) {
        return ResponseEntity.ok(approvalService.getApprovalsByRequest(requestId));
    }
}
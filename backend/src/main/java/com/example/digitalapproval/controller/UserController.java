package com.example.digitalapproval.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.digitalapproval.dto.ApprovalDTO;
import com.example.digitalapproval.dto.RequestDTO;
import com.example.digitalapproval.entity.Request;
import com.example.digitalapproval.service.ApprovalService;
import com.example.digitalapproval.service.RequestService;
import com.example.digitalapproval.service.UserDetailsImpl;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER') or hasRole('APPROVER') or hasRole('ADMIN')")
public class UserController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApprovalService approvalService;

    @PostMapping(value = "/requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RequestDTO> createRequest(@RequestParam("title") String title,
                                                    @RequestParam("description") String description,
                                                    @RequestParam("category") String category,
                                                    @RequestParam("requestedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestedDate,
                                                    @RequestParam(value = "attachment", required = false) MultipartFile attachment,
                                                    Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Request request = requestService.createRequest(title,
                                                       description,
                                                       category,
                                                       requestedDate,
                                                       attachment,
                                                       userDetails.getId());
        return ResponseEntity.ok(requestService.toDto(request));
    }

    @GetMapping("/requests")
    public ResponseEntity<Page<RequestDTO>> getUserRequests(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<RequestDTO> requests = requestService.getRequestsByUser(userDetails.getId(), pageable);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<RequestDTO> getRequest(@PathVariable Long requestId,
                                                 Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(requestService.getRequestForActor(requestId, userDetails.getId()));
    }

    @GetMapping("/requests/{requestId}/approvals")
    public ResponseEntity<List<ApprovalDTO>> getRequestApprovals(@PathVariable Long requestId,
                                                                 Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        requestService.getRequestForActor(requestId, userDetails.getId());
        return ResponseEntity.ok(approvalService.getApprovalsByRequest(requestId));
    }

    @GetMapping("/requests/{requestId}/attachment")
    public ResponseEntity<?> downloadAttachment(@PathVariable Long requestId,
                                                Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        var resource = requestService.getAttachmentForUser(requestId, userDetails.getId());
        if (resource == null) {
            return ResponseEntity.noContent().build();
        }
        Request request = requestService.getRequestById(requestId);
        String filename = StringUtils.hasText(request.getAttachmentOriginalName())
                ? request.getAttachmentOriginalName()
                : "attachment";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
package com.example.digitalapproval.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.digitalapproval.dto.AuditLogDTO;
import com.example.digitalapproval.dto.RequestDTO;
import com.example.digitalapproval.dto.RequestSummaryDTO;
import com.example.digitalapproval.dto.UserDTO;
import com.example.digitalapproval.entity.Request;
import com.example.digitalapproval.entity.Role;
import com.example.digitalapproval.service.AuditLogService;
import com.example.digitalapproval.service.RequestService;
import com.example.digitalapproval.service.UserDetailsImpl;
import com.example.digitalapproval.service.UserService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUserSummaries());
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestParam Role role) {
        UserDTO user = userService.updateUserRole(userId, role);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/requests")
    public ResponseEntity<Page<RequestDTO>> getAllRequests(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RequestDTO> requests = requestService.getAllRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/requests/{requestId}/assign")
    public ResponseEntity<?> assignApprover(@PathVariable Long requestId, @RequestParam Long approverId) {
        return ResponseEntity.ok(requestService.toDto(requestService.assignApprover(requestId, approverId, getCurrentUserId())));
    }

    @GetMapping("/audit")
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogs(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getRecentLogs(pageable));
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<RequestSummaryDTO> getSummary() {
        return ResponseEntity.ok(requestService.getSummary());
    }

    @GetMapping("/reports/requests/export")
    public ResponseEntity<ByteArrayResource> exportRequests(@RequestParam(required = false)
                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                            LocalDate from,
                                                            @RequestParam(required = false)
                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                            LocalDate to,
                                                            @RequestParam(required = false) String status) {
        Request.Status statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = Request.Status.valueOf(status.toUpperCase());
        }

        List<RequestDTO> rows = requestService.getRequestsForReport(from, to, statusEnum);
        StringBuilder builder = new StringBuilder();
        builder.append("Request ID,Title,Requester,Approver,Status,Requested Date,Updated At,Category\n");
        rows.forEach(row -> builder.append(String.join(",",
                quote(row.getId()),
                quote(row.getTitle()),
                quote(row.getUsername()),
                quote(row.getApproverName()),
                quote(String.valueOf(row.getStatus())),
                quote(row.getRequestedDate() != null ? row.getRequestedDate().toString() : ""),
                quote(row.getUpdatedAt() != null ? row.getUpdatedAt().toString() : ""),
                quote(row.getCategory()))).append('\n'));

        byte[] csvBytes = builder.toString().getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(csvBytes);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=approval-requests.csv")
                .header("Content-Type", "text/csv")
                .contentLength(csvBytes.length)
                .body(resource);
    }

    private String quote(Object value) {
        String text = value == null ? "" : value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            text = "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("Unable to determine authenticated admin");
    }
}
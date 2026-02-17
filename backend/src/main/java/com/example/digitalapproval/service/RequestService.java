package com.example.digitalapproval.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.digitalapproval.dto.RequestDTO;
import com.example.digitalapproval.dto.RequestSummaryDTO;
import com.example.digitalapproval.entity.Request;
import com.example.digitalapproval.entity.Role;
import com.example.digitalapproval.entity.User;
import com.example.digitalapproval.repository.RequestRepository;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private FileStorageService fileStorageService;

    public Request createRequest(String title,
                                 String description,
                                 String category,
                                 LocalDate requestedDate,
                                 MultipartFile attachment,
                                 Long userId) {
        User user = userService.findById(userId);
        String storedAttachment = fileStorageService.store(attachment);
        String originalName = attachment != null ? attachment.getOriginalFilename() : null;
        Request request = new Request(title, description, category, requestedDate, storedAttachment, originalName, user);
        Request saved = requestRepository.save(request);
        auditLogService.recordAction(userId, "REQUEST_CREATED", "Request " + saved.getId() + " created");
        return saved;
    }

    public Page<RequestDTO> getRequestsByUser(Long userId, Pageable pageable) {
        User user = userService.findById(userId);
        return requestRepository.findByUser(user, pageable).map(this::mapToDto);
    }

    public Page<RequestDTO> getRequestsByApprover(Long approverId, Pageable pageable) {
        User approver = userService.findById(approverId);
        return requestRepository.findByApprover(approver, pageable).map(this::mapToDto);
    }

    public Page<RequestDTO> getAllRequests(Pageable pageable) {
        return requestRepository.findAll(pageable).map(this::mapToDto);
    }

    public RequestSummaryDTO getSummary() {
        long total = requestRepository.count();
        long pending = requestRepository.countByStatus(Request.Status.PENDING);
        long approved = requestRepository.countByStatus(Request.Status.APPROVED);
        long rejected = requestRepository.countByStatus(Request.Status.REJECTED);
        return new RequestSummaryDTO(total, pending, approved, rejected);
    }

    public Request approveRequest(Long requestId, Long approverId, String remarks) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        validateApproverAccess(request, approverId);
        if (request.getStatus() != Request.Status.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved");
        }
        request.setStatus(Request.Status.APPROVED);
        request.setUpdatedAt(LocalDateTime.now());
        Request updated = requestRepository.save(request);
        auditLogService.recordAction(approverId, "REQUEST_APPROVED", "Request " + updated.getId() + " approved");
        return updated;
    }

    public Request rejectRequest(Long requestId, Long approverId, String remarks) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        validateApproverAccess(request, approverId);
        if (request.getStatus() != Request.Status.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }
        request.setStatus(Request.Status.REJECTED);
        request.setUpdatedAt(LocalDateTime.now());
        Request updated = requestRepository.save(request);
        auditLogService.recordAction(approverId, "REQUEST_REJECTED", "Request " + updated.getId() + " rejected");
        return updated;
    }

    public Request assignApprover(Long requestId, Long approverId, Long actorId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        User approver = userService.findById(approverId);
        if (approver.getRole() != Role.APPROVER && approver.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Selected user does not have approver privileges");
        }
        request.setApprover(approver);
        Request updated = requestRepository.save(request);
        auditLogService.recordAction(actorId, "REQUEST_ASSIGNED", "Request " + updated.getId() + " assigned to user " + approverId);
        return updated;
    }

    public Resource getAttachmentForUser(Long requestId, Long actorId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        User actor = userService.findById(actorId);
        if (!hasRequestAccess(actor, request)) {
            throw new AccessDeniedException("You do not have access to this attachment");
        }
        return fileStorageService.loadAsResource(request.getAttachment());
    }

    public Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
    }

    public RequestDTO getRequestForActor(Long requestId, Long actorId) {
        Request request = getRequestById(requestId);
        User actor = userService.findById(actorId);
        if (!hasRequestAccess(actor, request)) {
            throw new AccessDeniedException("You do not have access to this request");
        }
        return toDto(request);
    }

    public List<RequestDTO> getRequestsForReport(LocalDate from, LocalDate to, Request.Status status) {
        return requestRepository.findAll().stream()
                .filter(request -> status == null || request.getStatus() == status)
                .filter(request -> {
                    LocalDate requested = request.getRequestedDate();
                    if (from != null && (requested == null || requested.isBefore(from))) {
                        return false;
                    }
                    if (to != null && (requested == null || requested.isAfter(to))) {
                        return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Request::getRequestedDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Request::getId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private boolean hasRequestAccess(User actor, Request request) {
        if (actor.getRole() == Role.ADMIN) {
            return true;
        }
        if (request.getUser() != null && request.getUser().getId().equals(actor.getId())) {
            return true;
        }
        return request.getApprover() != null && request.getApprover().getId().equals(actor.getId());
    }

    private void validateApproverAccess(Request request, Long approverId) {
        User approver = userService.findById(approverId);
        if (request.getApprover() == null || !request.getApprover().getId().equals(approver.getId())) {
            throw new AccessDeniedException("Request is not assigned to the current approver");
        }
    }

    public RequestDTO toDto(Request request) {
        return mapToDto(request);
    }

    private RequestDTO mapToDto(Request request) {
        RequestDTO dto = new RequestDTO(request);
        if (request.getAttachment() != null) {
            dto.setAttachmentDownloadUrl("/api/user/requests/" + request.getId() + "/attachment");
        }
        return dto;
    }
}
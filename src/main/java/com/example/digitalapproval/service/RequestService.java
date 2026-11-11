
package com.example.digitalapproval.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.digitalapproval.dto.RequestCreateDto;
import com.example.digitalapproval.dto.RequestResponseDto;
import com.example.digitalapproval.entity.Request;
import com.example.digitalapproval.entity.User;
import com.example.digitalapproval.repository.RequestRepository;
import com.example.digitalapproval.repository.UserRepository;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    public RequestService(
            RequestRepository requestRepository,
            UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RequestResponseDto createRequest(
            RequestCreateDto requestDto,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        Request request = new Request();
        request.setUser(user);
        request.setTitle(requestDto.title());
        request.setDescription(requestDto.description());
        request.setStatus("PENDING");
        request.setSubmittedAt(LocalDateTime.now());

        return mapToResponse(requestRepository.save(request));
    }

    // ADMIN and APPROVER get all requests; USER gets only their own.
    @Transactional(readOnly = true)
    public List<RequestResponseDto> getRequestsForCurrentUser(
            Authentication authentication,
            LocalDate fromDate,
            LocalDate toDate,
            String sortOrder) {

        Long userId = getRequestScopeUserId(authentication);

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "From date cannot be after To date");
        }

        String normalizedSort = sortOrder == null
                ? "latest"
                : sortOrder.trim().toLowerCase();

        Sort sort;

        if ("latest".equals(normalizedSort)) {
            sort = Sort.by(Sort.Direction.DESC, "submittedAt");
        } else if ("oldest".equals(normalizedSort)) {
            sort = Sort.by(Sort.Direction.ASC, "submittedAt");
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sort must be latest or oldest");
        }

        LocalDateTime startDate = fromDate == null
                ? null
                : fromDate.atStartOfDay();

        // Exclusive next-day boundary includes the complete To Date.
        LocalDateTime endDateExclusive = toDate == null
                ? null
                : toDate.plusDays(1).atStartOfDay();

        return requestRepository.findFilteredRequests(
                        userId, startDate, endDateExclusive, sort)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RequestResponseDto getRequestDetails(
            Long requestId,
            Authentication authentication) {

        Long userId = getRequestScopeUserId(authentication);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Request not found"));

        // USER must never read another user's request.
        if (userId != null
                && !userId.equals(request.getUser().getId())) {
            throw new AccessDeniedException(
                    "You cannot view another user's request");
        }

        return mapToResponse(request);
    }

    @Transactional(readOnly = true)
    public List<RequestResponseDto> getPendingRequests() {
        return requestRepository
                .findByStatusOrderBySubmittedAtDesc("PENDING")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public RequestResponseDto approveRequest(
            Long requestId,
            String remarks,
            Authentication authentication) {

        User approver = getApprover(authentication);
        Request request = getPendingRequestOrThrow(requestId);

        request.setStatus("APPROVED");
        request.setRemarks(remarks);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecidedBy(approver);

        return mapToResponse(requestRepository.save(request));
    }

    @Transactional
    public RequestResponseDto rejectRequest(
            Long requestId,
            String remarks,
            Authentication authentication) {

        User approver = getApprover(authentication);
        Request request = getPendingRequestOrThrow(requestId);

        request.setStatus("REJECTED");
        request.setRemarks(remarks);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecidedBy(approver);

        return mapToResponse(requestRepository.save(request));
    }

    private Long getRequestScopeUserId(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        if (hasRole(authentication, "ADMIN")
                || hasRole(authentication, "APPROVER")) {
            return null;
        }

        if (hasRole(authentication, "USER")) {
            return getLoggedInUser(authentication).getId();
        }

        throw new AccessDeniedException("You cannot access requests");
    }

    private User getApprover(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !hasRole(authentication, "APPROVER")) {
            throw new AccessDeniedException(
                    "Only an APPROVER can decide requests");
        }

        return getLoggedInUser(authentication);
    }

    private User getLoggedInUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        return userRepository.findByEmailWithRole(authentication.getName())
                .orElseThrow(() ->
                        new AccessDeniedException("Logged-in user not found"));
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_" + role));
    }

    private Request getPendingRequestOrThrow(Long requestId) {

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Request not found"));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only PENDING requests can be approved or rejected");
        }

        return request;
    }

    private RequestResponseDto mapToResponse(Request request) {

        User decisionUser = request.getDecidedBy();

        return new RequestResponseDto(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getRemarks(),
                request.getSubmittedAt(),
                request.getUser().getId(),
                request.getDecidedAt(),
                decisionUser != null ? decisionUser.getId() : null,
                decisionUser != null ? decisionUser.getEmail() : null
        );
    }
}

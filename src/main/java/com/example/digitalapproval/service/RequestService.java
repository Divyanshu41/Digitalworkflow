
package com.example.digitalapproval.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Create request using the logged-in user's identity
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

        Request savedRequest = requestRepository.save(request);
        return mapToResponse(savedRequest);
    }

    // ADMIN gets all requests; other permitted users get only their own
    @Transactional(readOnly = true)
    public List<RequestResponseDto> getRequestsForCurrentUser(
            Authentication authentication) {

        if (hasRole(authentication, "ADMIN")) {
            return requestRepository.findAllByOrderBySubmittedAtDesc()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        User user = getLoggedInUser(authentication);

        return requestRepository
                .findByUser_IdOrderBySubmittedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // APPROVER and ADMIN can view pending requests
    @Transactional(readOnly = true)
    public List<RequestResponseDto> getPendingRequests() {
        return requestRepository
                .findByStatusOrderBySubmittedAtDesc("PENDING")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public RequestResponseDto approveRequest(Long requestId, String remarks) {
        Request request = getPendingRequestOrThrow(requestId);

        request.setStatus("APPROVED");
        request.setRemarks(remarks);

        return mapToResponse(requestRepository.save(request));
    }

    @Transactional
    public RequestResponseDto rejectRequest(Long requestId, String remarks) {
        Request request = getPendingRequestOrThrow(requestId);

        request.setStatus("REJECTED");
        request.setRemarks(remarks);

        return mapToResponse(requestRepository.save(request));
    }

    private User getLoggedInUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String email = authentication.getName();

        return userRepository.findByEmailWithRole(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Logged-in user not found"));
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_" + role));
    }

    private Request getPendingRequestOrThrow(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Request not found with id: " + requestId));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException(
                    "Only PENDING requests can be approved or rejected");
        }

        return request;
    }

    private RequestResponseDto mapToResponse(Request request) {
        return new RequestResponseDto(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getRemarks(),
                request.getSubmittedAt(),
                request.getUser().getId()
        );
    }
}

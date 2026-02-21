package com.example.digitalapproval.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

    public RequestService(RequestRepository requestRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RequestResponseDto createRequest(RequestCreateDto requestDto) {
        Long userId = Objects.requireNonNull(requestDto.userId(), "User id is required");
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + requestDto.userId()));

        Request request = new Request();
        request.setUser(user);
        request.setTitle(requestDto.title());
        request.setDescription(requestDto.description());
        request.setStatus("PENDING");
        request.setSubmittedAt(LocalDateTime.now());

        Request savedRequest = requestRepository.save(request);
        return mapToResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<RequestResponseDto> getAllRequests() {
        return requestRepository.findAll().stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<RequestResponseDto> getPendingRequests() {
        return requestRepository.findByStatus("PENDING").stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional
    public RequestResponseDto approveRequest(Long requestId, String remarks) {
        Request request = getRequestOrThrow(requestId);
        request.setStatus("APPROVED");
        request.setRemarks(remarks);
        Request savedRequest = requestRepository.save(request);
        return mapToResponse(savedRequest);
    }

    @Transactional
    public RequestResponseDto rejectRequest(Long requestId, String remarks) {
        Request request = getRequestOrThrow(requestId);
        request.setStatus("REJECTED");
        request.setRemarks(remarks);
        Request savedRequest = requestRepository.save(request);
        return mapToResponse(savedRequest);
    }

    private Request getRequestOrThrow(Long requestId) {
        Long nonNullRequestId = Objects.requireNonNull(requestId, "Request id is required");
        return requestRepository.findById(nonNullRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + requestId));
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

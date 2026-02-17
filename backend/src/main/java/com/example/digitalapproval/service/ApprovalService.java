package com.example.digitalapproval.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.digitalapproval.dto.ApprovalDTO;
import com.example.digitalapproval.entity.Approval;
import com.example.digitalapproval.entity.Request;
import com.example.digitalapproval.entity.User;
import com.example.digitalapproval.repository.ApprovalRepository;
import com.example.digitalapproval.repository.RequestRepository;

@Service
public class ApprovalService {

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserService userService;

    public Approval createApproval(Long requestId, Long approverId, Request.Status status, String remarks) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        User approver = userService.findById(approverId);
        Approval approval = new Approval(request, approver, status, remarks);
        return approvalRepository.save(approval);
    }

    public List<ApprovalDTO> getApprovalsByRequest(Long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        return approvalRepository.findByRequest(request).stream().map(ApprovalDTO::new).collect(Collectors.toList());
    }
}